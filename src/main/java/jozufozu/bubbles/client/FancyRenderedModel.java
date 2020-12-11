package jozufozu.bubbles.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import jozufozu.bubbles.client.shader.Shader;
import jozufozu.bubbles.client.shader.ShaderWrappedRenderLayer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class FancyRenderedModel extends Model {
    protected static final RenderState.TransparencyState TRANSLUCENT_TRANSPARENCY = new RenderState.TransparencyState("translucent_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });
    protected static final RenderState.AlphaState DEFAULT_ALPHA = new RenderState.AlphaState(0.003921569F);

    private static RenderType makeRenderType(ResourceLocation texture) {
        RenderType.State state = RenderType.State.getBuilder()
                                                 .texture(new RenderState.TextureState(texture, false, false))
                                                 .transparency(TRANSLUCENT_TRANSPARENCY)
                                                 .diffuseLighting(new RenderState.DiffuseLightingState(true))
                                                 .alpha(DEFAULT_ALPHA)
                                                 .cull(new RenderState.CullState(false))
                                                 .lightmap(new RenderState.LightmapState(true))
                                                 .overlay(new RenderState.OverlayState(false))
                                                 .build(true);
        RenderType normal = RenderType.makeType("bubbles:bubble", DefaultVertexFormats.ENTITY, 7, 256, true, true, state);

        return new ShaderWrappedRenderLayer(Shader.BUBBLE, null, normal);
    }

    protected TexturedQuad[] quads;

    public FancyRenderedModel() {
        super(FancyRenderedModel::makeRenderType);
    }

    @Override
    public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        MatrixStack.Entry last = matrixStackIn.getLast();
        Matrix4f matrix = last.getMatrix();
        Matrix3f normalMat = last.getNormal();

        float[] us = new float[]{1f, 0f, 0f, 1f};
        float[] vs = new float[]{1f, 1f, 0f, 0f};

        for (TexturedQuad quad : this.quads) {
            for (int i = 0; i < 4; ++i) {
                PositionNormalVertex vertex = quad.vertexPositions[i];

                Vector3f quadNormal = vertex.normal.copy();
                quadNormal.transform(normalMat);
                float nx = quadNormal.getX();
                float ny = quadNormal.getY();
                float nz = quadNormal.getZ();

                float x = vertex.position.getX();
                float y = vertex.position.getY();
                float z = vertex.position.getZ();
                Vector4f vector4f = new Vector4f(x, y, z, 1.0F);
                vector4f.transform(matrix);
                bufferIn.addVertex(vector4f.getX(), vector4f.getY(), vector4f.getZ(), red, green, blue, alpha, us[i], vs[i], packedOverlayIn, packedLightIn, nx, ny, nz);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class TexturedQuad {
        public final PositionNormalVertex[] vertexPositions;

        public TexturedQuad(PositionNormalVertex[] positionsIn) {
            this.vertexPositions = positionsIn;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class PositionNormalVertex {
        public final Vector3f position;
        public final Vector3f normal;

        public PositionNormalVertex(float x, float y, float z, float nx, float ny, float nz) {
            this(new Vector3f(x, y, z), new Vector3f(nx, ny, nz));
        }


        public PositionNormalVertex(Vector3f position, Vector3f normal) {
            normal.normalize();
            this.position = position;
            this.normal = normal;
        }
    }
}

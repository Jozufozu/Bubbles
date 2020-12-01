package red4.bubbles.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import red4.bubbles.Bubbles;
import red4.bubbles.client.shader.Shader;
import red4.bubbles.client.shader.ShaderHelper;
import red4.bubbles.client.shader.ShaderWrappedRenderLayer;
import red4.bubbles.entity.BubbleEntity;

@Mod.EventBusSubscriber(modid = Bubbles.MODID)
public class BubbleRenderer extends EntityRenderer<BubbleEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("bubbles:textures/entity/bubble.png");

    private final BubbleModel bubbleModel = new BubbleModel();

    @SubscribeEvent
    public static void renderBubbles(RenderWorldLastEvent event) {
        MatrixStack matrixStack = event.getMatrixStack();

        WorldRenderer worldRenderer = event.getContext();

        ClientWorld world = Minecraft.getInstance().world;

        ActiveRenderInfo info = worldRenderer.renderManager.info;

        float partialTicks = event.getPartialTicks();

        Vector3d vector3d = info.getProjectedView();
        double d0 = vector3d.getX();
        double d1 = vector3d.getY();
        double d2 = vector3d.getZ();

        IRenderTypeBuffer.Impl renderTypeBuffers = worldRenderer.renderTypeTextures.getBufferSource();

        for (Entity entity : world.getAllEntities()) {
            if (entity instanceof BubbleEntity) {
                renderEntity(worldRenderer.renderManager, entity, d0, d1, d2, partialTicks, matrixStack, renderTypeBuffers);
            }
        }

        renderTypeBuffers.finish();
    }

    private static void renderEntity(EntityRendererManager renderManager, Entity entityIn, double camX, double camY, double camZ, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn) {
        double d0 = MathHelper.lerp(partialTicks, entityIn.lastTickPosX, entityIn.getPosX());
        double d1 = MathHelper.lerp(partialTicks, entityIn.lastTickPosY, entityIn.getPosY());
        double d2 = MathHelper.lerp(partialTicks, entityIn.lastTickPosZ, entityIn.getPosZ());
        float f = MathHelper.lerp(partialTicks, entityIn.prevRotationYaw, entityIn.rotationYaw);
        renderManager.renderEntityStatic(entityIn, d0 - camX, d1 - camY, d2 - camZ, f, partialTicks, matrixStackIn, bufferIn, renderManager.getPackedLight(entityIn, partialTicks));
    }

    public BubbleRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public boolean shouldRender(BubbleEntity livingEntityIn, ClippingHelper camera, double camX, double camY, double camZ) {
        return false;
    }

    @Override
    public void render(BubbleEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {

        matrixStackIn.push();

        float size = (float) entityIn.getBoundingBox().getXSize();

        matrixStackIn.scale(size, size, size);

        IVertexBuilder ivertexbuilder = bufferIn.getBuffer(this.bubbleModel.getRenderType(this.getEntityTexture(entityIn)));
        this.bubbleModel.render(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        matrixStackIn.pop();

        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    @Override
    public ResourceLocation getEntityTexture(BubbleEntity entity) {
        return TEXTURE;
    }

    public static class BubbleModel extends Model {
        protected static final RenderState.TransparencyState TRANSLUCENT_TRANSPARENCY = new RenderState.TransparencyState("translucent_transparency", () -> {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        }, () -> {
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        });
        protected static final RenderState.AlphaState DEFAULT_ALPHA = new RenderState.AlphaState(0.003921569F);

        private final TexturedQuad[] quads;

        private static RenderType makeRenderType(ResourceLocation texture) {
            RenderType.State state = RenderType.State.getBuilder()
                                                                .texture(new RenderState.TextureState(texture, false, false))
                                                                .transparency(TRANSLUCENT_TRANSPARENCY)
                                                                .diffuseLighting(new RenderState.DiffuseLightingState(true))
                                                                .alpha(DEFAULT_ALPHA)
                                                                .cull(new RenderState.CullState(false))
                                                                .lightmap(new RenderState.LightmapState(true))
                                                                .overlay(new RenderState.OverlayState(true))
                                                                .build(true);
            RenderType normal = RenderType.makeType("bubbles:bubble", DefaultVertexFormats.ENTITY, 7, 256, true, true, state);

            return new ShaderWrappedRenderLayer(Shader.BUBBLE, null, normal);
        }

        public BubbleModel() {
            super(BubbleModel::makeRenderType);

            this.textureHeight = 16;
            this.textureWidth = 16;

            this.quads = new TexturedQuad[6];

            float xMin = -0.5f;
            float yMin = 0f;
            float zMin = -0.5f;
            float xMax = 0.5f;
            float yMax = 1f;
            float zMax = 0.5f;

            PositionNormalVertex vertex000 = new PositionNormalVertex(xMin, yMin, zMin, -1f, -1f, -1f);
            PositionNormalVertex vertex100 = new PositionNormalVertex(xMax, yMin, zMin, 1f, -1f, -1f);
            PositionNormalVertex vertex110 = new PositionNormalVertex(xMax, yMax, zMin, 1f, 1f, -1f);
            PositionNormalVertex vertex010 = new PositionNormalVertex(xMin, yMax, zMin, -1f, 1f, -1f);
            PositionNormalVertex vertex001 = new PositionNormalVertex(xMin, yMin, zMax, -1f, -1f, 1f);
            PositionNormalVertex vertex101 = new PositionNormalVertex(xMax, yMin, zMax, 1f, -1f, 1f);
            PositionNormalVertex vertex111 = new PositionNormalVertex(xMax, yMax, zMax, 1f, 1f, 1f);
            PositionNormalVertex vertex011 = new PositionNormalVertex(xMin, yMax, zMax, -1f, 1f, 1f);

            this.quads[2] = new TexturedQuad(new PositionNormalVertex[]{vertex101, vertex001, vertex000, vertex100});
            this.quads[3] = new TexturedQuad(new PositionNormalVertex[]{vertex110, vertex010, vertex011, vertex111});
            this.quads[1] = new TexturedQuad(new PositionNormalVertex[]{vertex000, vertex001, vertex011, vertex010});
            this.quads[4] = new TexturedQuad(new PositionNormalVertex[]{vertex100, vertex000, vertex010, vertex110});
            this.quads[0] = new TexturedQuad(new PositionNormalVertex[]{vertex101, vertex100, vertex110, vertex111});
            this.quads[5] = new TexturedQuad(new PositionNormalVertex[]{vertex001, vertex101, vertex111, vertex011});

        }

        @Override
        public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
            MatrixStack.Entry last = matrixStackIn.getLast();
            Matrix4f matrix = last.getMatrix();
            Matrix3f normalMat = last.getNormal();

            float[] us = new float[]{1f, 0f, 0f, 1f};
            float[] vs = new float[]{0f, 0f, 1f, 1f};

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
    }

    @OnlyIn(Dist.CLIENT)
    static class TexturedQuad {
        public final PositionNormalVertex[] vertexPositions;

        public TexturedQuad(PositionNormalVertex[] positionsIn) {
            this.vertexPositions = positionsIn;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class PositionNormalVertex {
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

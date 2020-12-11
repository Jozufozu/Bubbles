package red4.bubbles.client.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import red4.bubbles.client.EntityRendererWithBubbleParts;
import red4.bubbles.client.FancyRenderedModel;
import red4.bubbles.entity.BubbleStandEntity;

public class BubbleStandRenderer extends EntityRendererWithBubbleParts<BubbleStandEntity> {
    public static final ResourceLocation STAND_BASE = new ResourceLocation("bubbles:textures/entity/bubble_stand.png");
    public static final ResourceLocation BUBBLE_RING = new ResourceLocation("bubbles:textures/entity/bubble_ring.png");
    public static final ResourceLocation FILM = new ResourceLocation("bubbles:textures/entity/bubble_film.png");

    public BubbleStandModel base;
    public BubbleRingModel ring;
    public FilmModel film;

    public BubbleStandRenderer(EntityRendererManager renderManager) {
        super(renderManager);

        this.base = new BubbleStandModel();
        this.ring = new BubbleRingModel();
        this.film = new BubbleStandRenderer.FilmModel();
    }

    @Override
    public void render(BubbleStandEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);

        matrixStackIn.push();

        matrixStackIn.rotate(new Quaternion(Vector3f.YP, -entityYaw, true));
        //matrixStackIn.rotate(new Quaternion(Vector3f.ZP, pitch, true));

        IVertexBuilder standBuffer = bufferIn.getBuffer(this.base.getRenderType(STAND_BASE));
        this.base.render(matrixStackIn, standBuffer, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        IVertexBuilder ringBuffer = bufferIn.getBuffer(this.ring.getRenderType(BUBBLE_RING));
        this.ring.render(matrixStackIn, ringBuffer, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        matrixStackIn.pop();
    }

    @Override
    public void renderBubbleParts(BubbleStandEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        matrixStackIn.push();
        float pitch = MathHelper.lerp(partialTicks, entityIn.prevRotationPitch, entityIn.rotationPitch);

        matrixStackIn.translate(0, 0.5, 0);
        matrixStackIn.rotate(new Quaternion(Vector3f.YP, -entityYaw, true));
        matrixStackIn.rotate(new Quaternion(Vector3f.ZP, pitch, true));
        IVertexBuilder ivertexbuilder = bufferIn.getBuffer(this.film.getRenderType(FILM));
        this.film.render(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        matrixStackIn.pop();
    }

    @Override
    public ResourceLocation getEntityTexture(BubbleStandEntity entity) {
        return STAND_BASE;
    }

    public static class FilmModel extends FancyRenderedModel {

        public FilmModel() {
            this.textureHeight = 16;
            this.textureWidth = 16;

            this.quads = new TexturedQuad[1];

            float xMin = -0.125f;
            float yMin = -0.125f;
            float xMax = 0.125f;
            float yMax = 0.125f;

            PositionNormalVertex vertex000 = new PositionNormalVertex(xMin, yMin, 0f, -1f, -1f, -1f);
            PositionNormalVertex vertex100 = new PositionNormalVertex(xMax, yMin, 0f, 1f, -1f, -1f);
            PositionNormalVertex vertex110 = new PositionNormalVertex(xMax, yMax, 0f, 1f, 1f, -1f);
            PositionNormalVertex vertex010 = new PositionNormalVertex(xMin, yMax, 0f, -1f, 1f, -1f);

            this.quads[0] = new TexturedQuad(new PositionNormalVertex[]{vertex100, vertex000, vertex010, vertex110});
        }
    }
}

package jozufozu.bubbles.client.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import jozufozu.bubbles.client.EntityRendererWithBubbleParts;
import jozufozu.bubbles.client.FancyRenderedModel;
import jozufozu.bubbles.entity.BubbleEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;

public class BubbleRenderer extends EntityRendererWithBubbleParts<BubbleEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("bubbles:textures/entity/bubble.png");

    private final BubbleModel bubbleModel = new BubbleModel();

    public BubbleRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public ResourceLocation getEntityTexture(BubbleEntity entity) {
        return TEXTURE;
    }

    @Override
    public void renderBubbleParts(BubbleEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        matrixStackIn.push();

        float size = (float) entityIn.getBoundingBox().getXSize();

        matrixStackIn.scale(size, size, size);

        IVertexBuilder ivertexbuilder = bufferIn.getBuffer(this.bubbleModel.getRenderType(this.getEntityTexture(entityIn)));
        this.bubbleModel.render(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        matrixStackIn.pop();
    }

    public static class BubbleModel extends FancyRenderedModel {

        public BubbleModel() {
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
    }
}

package red4.bubbles.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Pose;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import red4.bubbles.entity.BubbleEntity;

public class BubbleRenderer extends EntityRenderer<BubbleEntity> {

    private final BubbleModel bubbleModel = new BubbleModel();

    public BubbleRenderer(EntityRendererManager renderManager) {
        super(renderManager);
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
        return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
    }

    public static class BubbleModel extends Model {
        private final ModelRenderer bubble;

        public BubbleModel() {
            super(RenderType::getEntityCutoutNoCull);

            bubble = new ModelRenderer(this, 0, 0);
            bubble.addBox(-8, 0, -8, 16, 16, 16);
        }

        @Override
        public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
            bubble.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
        }
    }
}

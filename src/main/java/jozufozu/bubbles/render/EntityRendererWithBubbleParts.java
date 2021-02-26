package jozufozu.bubbles.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;

public abstract class EntityRendererWithBubbleParts<T extends Entity> extends EntityRenderer<T> {
    protected EntityRendererWithBubbleParts(EntityRendererManager renderManager) {
        super(renderManager);
    }

    public boolean shouldRenderBubbleParts(T entity, ClippingHelper camera, double camX, double camY, double camZ) {
        return super.shouldRender(entity, camera, camX, camY, camZ);
    }

    public abstract void renderBubbleParts(T entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn);
}

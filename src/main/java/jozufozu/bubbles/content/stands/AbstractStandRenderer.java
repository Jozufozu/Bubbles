package jozufozu.bubbles.content.stands;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import jozufozu.bubbles.content.stands.model.BubbleStandModel;
import jozufozu.bubbles.render.EntityRendererWithBubbleParts;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

public abstract class AbstractStandRenderer<S extends AbstractStandEntity> extends EntityRendererWithBubbleParts<S> {
    public static final ResourceLocation STAND_BASE = new ResourceLocation("bubbles:textures/entity/bubble_stand.png");

    public BubbleStandModel base;

    public AbstractStandRenderer(EntityRendererManager renderManager) {
        super(renderManager);

        this.base = new BubbleStandModel();
    }

    @Override
    public void render(S stand, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        super.render(stand, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);

        matrixStackIn.push();

        applyRotation(stand, entityYaw, partialTicks, matrixStackIn);

        IVertexBuilder standBuffer = bufferIn.getBuffer(this.base.getRenderType(STAND_BASE));
        this.base.render(matrixStackIn, standBuffer, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        matrixStackIn.push();

        double length = MathHelper.lerp(partialTicks, stand.lastTickLength, stand.getLength());

        matrixStackIn.translate(0, length, 0);

        renderAttachment(stand, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);

        matrixStackIn.pop();
        matrixStackIn.pop();
    }

    protected abstract void renderAttachment(S stand, float entityYaw, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight);

    @Override
    public void renderBubbleParts(S stand, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {

    }

    protected void applyRotation(S entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn) {
        Direction orientation = entityIn.getOrientation();
        matrixStackIn.rotate(orientation.getRotation());

        if (orientation.getAxis() == Direction.Axis.X) entityYaw += 90; // TODO: this is a horrible hack that i need to fix
        matrixStackIn.rotate(new Quaternion(Vector3f.YP, -entityYaw, true));
    }

    @Override
    public ResourceLocation getEntityTexture(AbstractStandEntity entity) {
        return STAND_BASE;
    }

}

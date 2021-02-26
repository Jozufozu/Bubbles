package jozufozu.bubbles.content.stands;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import jozufozu.bubbles.content.stands.model.BubbleRingModel;
import jozufozu.bubbles.content.stands.model.FilmModel;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class WandStandRenderer extends AbstractStandRenderer<WandStandEntity> {
    public static final ResourceLocation BUBBLE_RING = new ResourceLocation("bubbles:textures/entity/bubble_ring.png");
    public static final ResourceLocation FILM = new ResourceLocation("bubbles:textures/entity/bubble_film.png");

    public BubbleRingModel ring;
    public FilmModel film;

    public WandStandRenderer(EntityRendererManager renderManager) {
        super(renderManager);
        this.ring = new BubbleRingModel();
        this.film = new FilmModel();
    }

    @Override
    protected void renderAttachment(WandStandEntity stand, float entityYaw, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        IVertexBuilder ringBuffer = buffer.getBuffer(this.ring.getRenderType(BUBBLE_RING));
        this.ring.render(matrixStack, ringBuffer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public void renderBubbleParts(WandStandEntity stand, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        matrixStackIn.push();

        applyRotation(stand, entityYaw, partialTicks, matrixStackIn);
        double length = MathHelper.lerp(partialTicks, stand.lastTickLength, stand.getLength());

        matrixStackIn.translate(0, length, 0);

        IVertexBuilder ivertexbuilder = bufferIn.getBuffer(this.film.getRenderType(FILM));
        this.film.render(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        matrixStackIn.pop();
    }
}

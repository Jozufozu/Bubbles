package jozufozu.bubbles.content.stands;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import jozufozu.bubbles.content.stands.model.CraftingLatticeModel;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;

public class CraftingStandRenderer extends AbstractStandRenderer<CraftingStandEntity> {
    public static final ResourceLocation CRAFTING_LATTICE = new ResourceLocation("bubbles:textures/entity/crafting_lattice.png");


    public CraftingLatticeModel lattice;

    public CraftingStandRenderer(EntityRendererManager renderManager) {
        super(renderManager);
        this.lattice = new CraftingLatticeModel();

    }

    @Override
    protected void renderAttachment(CraftingStandEntity stand, float entityYaw, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        IVertexBuilder ringBuffer = buffer.getBuffer(this.lattice.getRenderType(CRAFTING_LATTICE));
        this.lattice.render(matrixStack, ringBuffer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }

}

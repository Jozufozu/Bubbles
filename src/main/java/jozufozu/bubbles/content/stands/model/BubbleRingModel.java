package jozufozu.bubbles.content.stands.model;// Made with Blockbench 3.7.4
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import jozufozu.bubbles.content.stands.AbstractStandEntity;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class BubbleRingModel extends EntityModel<AbstractStandEntity> {
	private final ModelRenderer bubble_ring;

	public BubbleRingModel() {
		texWidth = 16;
		texHeight = 16;

		bubble_ring = new ModelRenderer(this);
		bubble_ring.setPos(0.0F, 0.0F, 0.0F);

		bubble_ring.texOffs(4, 4).addBox(2.0F, -2.0F, -0.5F, 1.0F, 4.0F, 1.0F, 0.0F, false);
		bubble_ring.texOffs(0, 4).addBox(-3.0F, -2.0F, -0.5F, 1.0F, 4.0F, 1.0F, 0.0F, false);
		bubble_ring.texOffs(0, 2).addBox(-3.0F, -3.0F, -0.5F, 6.0F, 1.0F, 1.0F, 0.0F, false);
		bubble_ring.texOffs(0, 0).addBox(-3.0F, 2.0F, -0.5F, 6.0F, 1.0F, 1.0F, 0.0F, false);
	}

	@Override
	public void setupAnim(AbstractStandEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch){
		//previously the render function, render code was moved to a method below
	}

	@Override
	public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){
		bubble_ring.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}
}
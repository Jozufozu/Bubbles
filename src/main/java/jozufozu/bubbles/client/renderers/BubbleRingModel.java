package jozufozu.bubbles.client.renderers;// Made with Blockbench 3.7.4
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import jozufozu.bubbles.entity.BubbleStandEntity;

public class BubbleRingModel extends EntityModel<BubbleStandEntity> {
	private final ModelRenderer bubble_ring;
	private final ModelRenderer west_r1;

	public BubbleRingModel() {
		textureWidth = 16;
		textureHeight = 16;

		bubble_ring = new ModelRenderer(this);
		bubble_ring.setRotationPoint(0.0F, 8.0F, 0.0F);
		

		west_r1 = new ModelRenderer(this);
		west_r1.setRotationPoint(0.0F, 0.0F, 0.0F);
		bubble_ring.addChild(west_r1);
		west_r1.setTextureOffset(4, 4).addBox(2.0F, -2.0F, -0.5F, 1.0F, 4.0F, 1.0F, 0.0F, false);
		west_r1.setTextureOffset(0, 4).addBox(-3.0F, -2.0F, -0.5F, 1.0F, 4.0F, 1.0F, 0.0F, false);
		west_r1.setTextureOffset(0, 2).addBox(-3.0F, -3.0F, -0.5F, 6.0F, 1.0F, 1.0F, 0.0F, false);
		west_r1.setTextureOffset(0, 0).addBox(-3.0F, 2.0F, -0.5F, 6.0F, 1.0F, 1.0F, 0.0F, false);
	}

	@Override
	public void setRotationAngles(BubbleStandEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch){
		//previously the render function, render code was moved to a method below
	}

	@Override
	public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){
		bubble_ring.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
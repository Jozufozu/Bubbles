package jozufozu.bubbles.client.renderers;// Made with Blockbench 3.7.4
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import jozufozu.bubbles.entity.BubbleStandEntity;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class CraftingLatticeModel extends EntityModel<BubbleStandEntity> {
	private final ModelRenderer bb_main;

	public CraftingLatticeModel() {
		textureWidth = 16;
		textureHeight = 16;

		bb_main = new ModelRenderer(this);
		bb_main.setRotationPoint(0.0F, 24.0F, 0.0F);
		bb_main.setTextureOffset(4, 4).addBox(-1.75F, -3.0F, -0.75F, 1.0F, 6.0F, 1.0F, 0.0F, false);
		bb_main.setTextureOffset(0, 4).addBox(0.75F, -3.0F, -0.75F, 1.0F, 6.0F, 1.0F, 0.0F, false);
		bb_main.setTextureOffset(0, 2).addBox(-3.0F, 0.75F, -0.25F, 6.0F, 1.0F, 1.0F, 0.0F, false);
		bb_main.setTextureOffset(0, 0).addBox(-3.0F, -1.75F, -0.25F, 6.0F, 1.0F, 1.0F, 0.0F, false);
	}

	@Override
	public void setRotationAngles(BubbleStandEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch){
		//previously the render function, render code was moved to a method below
	}

	@Override
	public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){
		bb_main.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
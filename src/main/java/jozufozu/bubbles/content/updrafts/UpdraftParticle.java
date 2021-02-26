package jozufozu.bubbles.content.updrafts;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public abstract class UpdraftParticle extends SpriteTexturedParticle {
    protected final IAnimatedSprite spriteWithAge;

    protected final boolean mirrored;

    protected UpdraftParticle(IAnimatedSprite spriteWithAge, ClientWorld world, double x, double y, double z) {
        super(world, x, y, z);
        this.spriteWithAge = spriteWithAge;
        this.canCollide = false;
        mirrored = this.rand.nextBoolean();
    }

    public void tick() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.motionY += 0.005D;
        if (this.age++ >= this.maxAge) {
            this.setExpired();
        } else {
            this.selectSpriteWithAge(this.spriteWithAge);
            this.move(this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.85;
            this.motionZ *= 0.85;

            if (this.world.getBlockState(new BlockPos(this.posX, this.posY, this.posZ)).isSolid()) {
                this.setExpired();
            }
        }
    }

    public void renderParticle(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks) {
        Vector3d cameraPos = renderInfo.getProjectedView();
        float x = (float)(MathHelper.lerp(partialTicks, this.prevPosX, this.posX) - cameraPos.getX());
        float y = (float)(MathHelper.lerp(partialTicks, this.prevPosY, this.posY) - cameraPos.getY());
        float z = (float)(MathHelper.lerp(partialTicks, this.prevPosZ, this.posZ) - cameraPos.getZ());

        Quaternion quaternion = new Quaternion(0.0F, 0.0F, 0.0F, 1.0F);

        quaternion.multiply(Vector3f.YP.rotation((float) Math.atan2(x, z)));

        Vector3f[] quad = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
        float scale = this.getScale(partialTicks);

        for (int i = 0; i < 4; ++i) {
            Vector3f vector3f = quad[i];
            vector3f.transform(quaternion);
            vector3f.mul(scale);
            vector3f.add(x, y, z);
        }

        float minU = this.getMinU();
        float maxU = this.getMaxU();
        float minV = this.getMinV();
        float maxV = this.getMaxV();
        int light = this.getBrightnessForRender(partialTicks);

        if (mirrored) {
            float t = minU;
            minU = maxU;
            maxU = t;
        }

        buffer.pos(quad[0].getX(), quad[0].getY(), quad[0].getZ()).tex(maxU, maxV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(light).endVertex();
        buffer.pos(quad[1].getX(), quad[1].getY(), quad[1].getZ()).tex(maxU, minV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(light).endVertex();
        buffer.pos(quad[2].getX(), quad[2].getY(), quad[2].getZ()).tex(minU, minV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(light).endVertex();
        buffer.pos(quad[3].getX(), quad[3].getY(), quad[3].getZ()).tex(minU, maxV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(light).endVertex();
    }
}

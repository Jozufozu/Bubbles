package jozufozu.bubbles.client.particles;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SmallUpdraftParticle extends UpdraftParticle {

    private SmallUpdraftParticle(ClientWorld world, double x, double y, double z, double motionX, double motionY, double motionZ, IAnimatedSprite spriteWithAge) {
        super(spriteWithAge, world, x, y, z);
        this.setSize(0.02F, 0.02F);
        this.particleAlpha = 0.4f + this.rand.nextFloat() * 0.2f;
        this.particleScale *= 0.7 + this.rand.nextFloat() * 0.6F;
        this.motionX = motionX * 0.2F + (Math.random() * 2.0D - 1.0D) * 0.02F;
        this.motionY = motionY + (Math.random() * 2.0D - 1.0D) * 0.02F;
        this.motionZ = motionZ * 0.2F + (Math.random() * 2.0D - 1.0D) * 0.02F;
        this.maxAge = (int)(10.0D / (Math.random() * 0.8D + 0.2D));
        this.selectSpriteWithAge(spriteWithAge);
    }

    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite spriteSet) {
            this.spriteSet = spriteSet;
        }

        public Particle makeParticle(BasicParticleType typeIn, ClientWorld worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new SmallUpdraftParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, spriteSet);
        }
    }
}

package jozufozu.bubbles.content.updrafts;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class UpdraftSwirlParticle extends UpdraftParticle {
    private UpdraftSwirlParticle(ClientWorld world, double x, double y, double z, double motionX, double motionY, double motionZ, IAnimatedSprite spriteWithAge) {
        super(spriteWithAge, world, x, y, z);
        this.setSize(0.02F, 0.02F);
        this.alpha = 0.8f + this.random.nextFloat() * 0.2f;
        this.quadSize *= 3 + this.random.nextFloat() * 0.6F;
        this.xd = motionX * (double)0.2F + (Math.random() * 2.0D - 1.0D) * (double)0.02F;
        this.yd = motionY + (Math.random() * 2.0D - 1.0D) * (double)0.02F;
        this.zd = motionZ * (double)0.2F + (Math.random() * 2.0D - 1.0D) * (double)0.02F;
        this.lifetime = 28;
        this.setSpriteFromAge(spriteWithAge);
    }

    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite spriteSet) {
            this.spriteSet = spriteSet;
        }

        public Particle createParticle(BasicParticleType typeIn, ClientWorld worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new UpdraftSwirlParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, spriteSet);
        }
    }
}

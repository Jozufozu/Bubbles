package jozufozu.bubbles;

import jozufozu.bubbles.Bubbles;
import jozufozu.bubbles.content.updrafts.SmallUpdraftParticle;
import jozufozu.bubbles.content.updrafts.UpdraftSwirlParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Bubbles.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AllParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Bubbles.MODID);

    public static final BasicParticleType UPDRAFT_SMALL = new BasicParticleType(true);
    public static final BasicParticleType UPDRAFT_SWIRL = new BasicParticleType(true);

    static {
        PARTICLES.register("updraft_small", () -> UPDRAFT_SMALL);
        PARTICLES.register("updraft_swirl", () -> UPDRAFT_SWIRL);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void registerFactories(ParticleFactoryRegisterEvent evt) {
        Minecraft.getInstance().particles.registerFactory(UPDRAFT_SMALL, SmallUpdraftParticle.Factory::new);
        Minecraft.getInstance().particles.registerFactory(UPDRAFT_SWIRL, UpdraftSwirlParticle.Factory::new);
    }
}

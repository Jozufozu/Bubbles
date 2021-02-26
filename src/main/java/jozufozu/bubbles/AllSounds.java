package jozufozu.bubbles;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class AllSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Bubbles.MODID);

    public static final RegistryObject<SoundEvent> SOUL_BURN = SOUNDS.register("soul_burn", () -> new SoundEvent(new ResourceLocation("bubbles:soul_burn")));
    // should be played with pitch +/- 0.1
    public static final RegistryObject<SoundEvent> BELLOWS_BLOW = SOUNDS.register("bellows_blow", () -> new SoundEvent(new ResourceLocation("bubbles:bellows_blow")));
}

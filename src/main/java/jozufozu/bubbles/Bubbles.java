package jozufozu.bubbles;

import jozufozu.bubbles.block.ModBlocks;
import jozufozu.bubbles.client.particles.ModParticles;
import jozufozu.bubbles.client.renderers.BubbleRenderer;
import jozufozu.bubbles.client.renderers.BubbleStandRenderer;
import jozufozu.bubbles.client.shader.ShaderHelper;
import jozufozu.bubbles.entity.BubbleEntity;
import jozufozu.bubbles.entity.BubbleStandEntity;
import jozufozu.bubbles.entity.Serializers;
import jozufozu.bubbles.entity.behavior.Behaviors;
import jozufozu.bubbles.items.BubbleStandItem;
import jozufozu.bubbles.items.BubbleWandItem;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Bubbles.MODID)
public class Bubbles {
    public static final String MODID = "bubbles";
    public static final Logger LOGGER = LogManager.getLogger();

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Bubbles.MODID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Bubbles.MODID);

    // should be played with pitch +/- 0.1
    public static final RegistryObject<SoundEvent> BELLOWS_BLOW = SOUNDS.register("bellows_blow", () -> new SoundEvent(new ResourceLocation("bubbles:bellows_blow")));
    public static final RegistryObject<SoundEvent> SOUL_BURN = SOUNDS.register("soul_burn", () -> new SoundEvent(new ResourceLocation("bubbles:soul_burn")));

    public static final RegistryObject<Item> BUBBLE_STAND = ITEMS.register("bubble_stand", BubbleStandItem::new);
    public static final RegistryObject<Item> BUBBLE_WAND = ITEMS.register("bubble_wand", BubbleWandItem::new);


    public Bubbles() {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ShaderHelper::initShaders);

        Behaviors.initBehaviors();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(EntityType.class, this::registerEntities);

        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModBlocks.BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        SOUNDS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModParticles.PARTICLES.register(FMLJavaModLoadingContext.get().getModEventBus());
        Serializers.SERIALIZERS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private void registerEntities(final RegistryEvent.Register<EntityType<?>> event) {
        event.getRegistry().register(BubbleEntity.BUBBLE);
        event.getRegistry().register(BubbleStandEntity.BUBBLE_STAND);
    }

    private void setup(final FMLCommonSetupEvent event) { }

    private void doClientStuff(final FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(BubbleEntity.BUBBLE, BubbleRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(BubbleStandEntity.BUBBLE_STAND, BubbleStandRenderer::new);

        RenderTypeLookup.setRenderLayer(ModBlocks.BLAZING_SOUL_FIRE.get(), RenderType.getCutout());
    }

    private void enqueueIMC(final InterModEnqueueEvent event) { }

    private void processIMC(final InterModProcessEvent event) { }
}

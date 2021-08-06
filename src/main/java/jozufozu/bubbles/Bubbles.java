package jozufozu.bubbles;

import jozufozu.bubbles.content.core.BubbleRenderer;
import jozufozu.bubbles.content.stands.CraftingStandRenderer;
import jozufozu.bubbles.content.stands.WandStandRenderer;
import jozufozu.bubbles.render.BubblesClient;
import jozufozu.bubbles.render.shader.ShaderHelper;
import jozufozu.bubbles.content.core.behavior.Behaviors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Bubbles.MODID)
public class Bubbles {
    public static final String MODID = "bubbles";
    public static final Logger LOGGER = LogManager.getLogger();


    public Bubbles() {
        IEventBus bus = FMLJavaModLoadingContext.get()
                .getModEventBus();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> bus.addListener(BubblesClient::flwInit));

        Behaviors.initBehaviors();

        bus.addListener(this::setup);
        bus.addListener(this::enqueueIMC);
        bus.addListener(this::processIMC);
        bus.addListener(this::doClientStuff);

        bus.addGenericListener(EntityType.class, AllEntityTypes::registerEntities);

        AllItems.ITEMS.register(bus);
        AllBlocks.BLOCKS.register(bus);
        AllSounds.SOUNDS.register(bus);
        AllParticles.PARTICLES.register(bus);
    }

    private void setup(final FMLCommonSetupEvent event) { }

    private void doClientStuff(final FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(AllEntityTypes.BUBBLE, BubbleRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(AllEntityTypes.BUBBLE_STAND, WandStandRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(AllEntityTypes.CRAFTING_STAND, CraftingStandRenderer::new);

        RenderTypeLookup.setRenderLayer(AllBlocks.BLAZING_SOUL_FIRE.get(), RenderType.cutout());
    }

    private void enqueueIMC(final InterModEnqueueEvent event) { }

    private void processIMC(final InterModProcessEvent event) { }
}

package jozufozu.bubbles;

import jozufozu.bubbles.block.SoapBlock;
import jozufozu.bubbles.client.renderers.BubbleRenderer;
import jozufozu.bubbles.client.renderers.BubbleStandRenderer;
import jozufozu.bubbles.client.shader.ShaderHelper;
import jozufozu.bubbles.entity.Behaviors;
import jozufozu.bubbles.entity.BubbleStandEntity;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
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
import jozufozu.bubbles.block.BellowsBlock;
import jozufozu.bubbles.entity.BubbleEntity;
import jozufozu.bubbles.items.BubbleStandItem;
import jozufozu.bubbles.items.BubbleWandItem;

@Mod(Bubbles.MODID)
public class Bubbles {
    public static final String MODID = "bubbles";
    public static final Logger LOGGER = LogManager.getLogger();

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Bubbles.MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Bubbles.MODID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Bubbles.MODID);

    // should never be played with randomized pitch
    public static final RegistryObject<SoundEvent> BELLOWS_BLOW = SOUNDS.register("bellows_blow", () -> new SoundEvent(new ResourceLocation("bubbles:bellows_blow")));

    public static final RegistryObject<Block> BELLOW = BLOCKS.register("bellow", BellowsBlock::new);
    public static final RegistryObject<Item> BELLOW_ITEM = ITEMS.register("bellow", () -> new BlockItem(BELLOW.get(), new Item.Properties().group(ItemGroup.MISC)));

    public static final RegistryObject<Block> SOAP_BLOCK = BLOCKS.register("soap_block", SoapBlock::new);
    public static final RegistryObject<Item> SOAP_BLOCK_ITEM = ITEMS.register("soap_block", () -> new BlockItem(SOAP_BLOCK.get(), new Item.Properties().group(ItemGroup.MISC)));

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
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        SOUNDS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private void registerEntities(final RegistryEvent.Register<EntityType<?>> event) {
        event.getRegistry().register(BubbleEntity.BUBBLE);
        event.getRegistry().register(BubbleStandEntity.BUBBLE_STAND);
    }

    private void setup(final FMLCommonSetupEvent event) { }

    private void doClientStuff(final FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(BubbleEntity.BUBBLE, BubbleRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(BubbleStandEntity.BUBBLE_STAND, BubbleStandRenderer::new);
    }

    private void enqueueIMC(final InterModEnqueueEvent event) { }

    private void processIMC(final InterModProcessEvent event) { }
}

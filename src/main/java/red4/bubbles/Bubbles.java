package red4.bubbles;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
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
import red4.bubbles.block.BellowsBlock;
import red4.bubbles.block.BubbleBlowerBlock;
import red4.bubbles.client.BubbleRenderer;
import red4.bubbles.client.shader.ShaderHelper;
import red4.bubbles.entity.Behaviors;
import red4.bubbles.entity.BubbleEntity;

@Mod(Bubbles.MODID)
public class Bubbles {
    public static final String MODID = "bubbles";
    public static final Logger LOGGER = LogManager.getLogger();

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Bubbles.MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Bubbles.MODID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Bubbles.MODID);

    public static final RegistryObject<SoundEvent> BELLOWS_BLOW = SOUNDS.register("bellows_blow", () -> new SoundEvent(new ResourceLocation("bubbles:bellows_blow")));

    public static final RegistryObject<Block> BUBBLE_BLOWER = BLOCKS.register("blower", () -> new BubbleBlowerBlock(AbstractBlock.Properties.create(Material.IRON)));
    public static final RegistryObject<Item> BLOWER_ITEM = ITEMS.register("blower", () -> new BlockItem(BUBBLE_BLOWER.get(), new Item.Properties().group(ItemGroup.MISC)));

    public static final RegistryObject<Block> BELLOW = BLOCKS.register("bellow", BellowsBlock::new);
    public static final RegistryObject<Item> BELLOW_ITEM = ITEMS.register("bellow", () -> new BlockItem(BELLOW.get(), new Item.Properties().group(ItemGroup.MISC)));


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
    }

    private void setup(final FMLCommonSetupEvent event) { }

    private void doClientStuff(final FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(BubbleEntity.BUBBLE, BubbleRenderer::new);
    }

    private void enqueueIMC(final InterModEnqueueEvent event) { }

    private void processIMC(final InterModProcessEvent event) { }
}

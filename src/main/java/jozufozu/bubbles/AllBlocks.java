package jozufozu.bubbles;

import jozufozu.bubbles.content.BellowsBlock;
import jozufozu.bubbles.content.BubblePlate;
import jozufozu.bubbles.content.SoapBlock;
import jozufozu.bubbles.content.updrafts.BlazingSoulFireBlock;
import jozufozu.bubbles.content.updrafts.UpdraftBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class AllBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Bubbles.MODID);

    public static final RegistryObject<Block> BELLOW = BLOCKS.register("bellow", BellowsBlock::new);
    public static final RegistryObject<Item> BELLOW_ITEM = AllItems.ITEMS.register("bellow", () -> new BlockItem(BELLOW.get(), new Item.Properties().group(ItemGroup.MISC)));
    public static final RegistryObject<Block> SOAP_BLOCK = BLOCKS.register("soap_block", SoapBlock::new);
    public static final RegistryObject<Item> SOAP_BLOCK_ITEM = AllItems.ITEMS.register("soap_block", () -> new BlockItem(SOAP_BLOCK.get(), new Item.Properties().group(ItemGroup.MISC)));

    public static final RegistryObject<Block> UPDRAFT = BLOCKS.register("updraft", UpdraftBlock::new);

    public static final RegistryObject<Block> BLAZING_SOUL_FIRE = BLOCKS.register("blazing_soul_fire", BlazingSoulFireBlock::new);

    public static final RegistryObject<Block> BUBBLE_PLATE = BLOCKS.register("bubble_plate", BubblePlate::new);
    public static final RegistryObject<Item> BUBBLE_PLATE_ITEM = AllItems.ITEMS.register("bubble_plate", () -> new BlockItem(BUBBLE_PLATE.get(), new Item.Properties().group(ItemGroup.MISC)));
}

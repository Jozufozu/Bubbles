package jozufozu.bubbles.block;

import jozufozu.bubbles.Bubbles;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.PushReaction;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Bubbles.MODID);

    public static final RegistryObject<Block> BELLOW = BLOCKS.register("bellow", BellowsBlock::new);
    public static final RegistryObject<Item> BELLOW_ITEM = Bubbles.ITEMS.register("bellow", () -> new BlockItem(BELLOW.get(), new Item.Properties().group(ItemGroup.MISC)));
    public static final RegistryObject<Block> SOAP_BLOCK = BLOCKS.register("soap_block", SoapBlock::new);
    public static final RegistryObject<Item> SOAP_BLOCK_ITEM = Bubbles.ITEMS.register("soap_block", () -> new BlockItem(SOAP_BLOCK.get(), new Item.Properties().group(ItemGroup.MISC)));

    public static final RegistryObject<Block> UPDRAFT = BLOCKS.register("updraft", UpdraftBlock::new);

    public static final RegistryObject<Block> UPDRAFT_SOURCE = BLOCKS.register("updraft_source", UpdraftSource::new);
    public static final RegistryObject<Item> UPDRAFT_SOURCE_ITEM = Bubbles.ITEMS.register("updraft_source", () -> new BlockItem(UPDRAFT_SOURCE.get(), new Item.Properties().group(ItemGroup.MISC)));

    public static final RegistryObject<Block> BUBBLE_PLATE = BLOCKS.register("bubble_plate", BubblePlate::new);
    public static final RegistryObject<Item> BUBBLE_PLATE_ITEM = Bubbles.ITEMS.register("bubble_plate", () -> new BlockItem(BUBBLE_PLATE.get(), new Item.Properties().group(ItemGroup.MISC)));
}

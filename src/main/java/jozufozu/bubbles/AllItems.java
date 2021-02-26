package jozufozu.bubbles;

import jozufozu.bubbles.content.BubbleWandItem;
import jozufozu.bubbles.content.stands.BubbleStandItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class AllItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Bubbles.MODID);

    public static final RegistryObject<Item> BUBBLE_WAND = ITEMS.register("bubble_wand", BubbleWandItem::new);
    public static final RegistryObject<Item> CRAFTING_STAND = ITEMS.register("crafting_stand", () -> new BubbleStandItem(AllEntityTypes.CRAFTING_STAND));
    public static final RegistryObject<Item> WAND_STAND = ITEMS.register("wand_stand", () -> new BubbleStandItem(AllEntityTypes.BUBBLE_STAND));
}

package jozufozu.bubbles.content;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class BubbleWandItem extends Item {
    public BubbleWandItem() {
        super(new Item.Properties().tab(ItemGroup.TAB_MISC));
    }

    @Override
    public void onUseTick(World worldIn, LivingEntity livingEntityIn, ItemStack stack, int count) {
        super.onUseTick(worldIn, livingEntityIn, stack, count);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        return super.useOn(context);
    }

    @Override
    public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
        return super.use(worldIn, playerIn, handIn);
    }
}

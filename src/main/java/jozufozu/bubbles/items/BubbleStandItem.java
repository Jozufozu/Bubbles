package jozufozu.bubbles.items;

import jozufozu.bubbles.entity.BubbleStandEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class BubbleStandItem extends Item {
    public BubbleStandItem() {
        super(new Properties().group(ItemGroup.MISC));
    }

    public ActionResultType onItemUse(ItemUseContext context) {
        Direction orientation = context.getFace();
        World world = context.getWorld();
        ItemStack itemstack = context.getItem();

        Vector3d hitVec = context.getHitVec();

        int xOffset = orientation.getXOffset();
        int yOffset = orientation.getYOffset();
        int zOffset = orientation.getZOffset();
        int xCenter = Math.abs(xOffset);
        int yCenter = Math.abs(yOffset);
        int zCenter = Math.abs(zOffset);

        double x = getWholePart(hitVec.x) + xCenter * getFractionalPart(hitVec.x) + 0.5 * (1 - xCenter) * Math.signum(hitVec.x) + xOffset * 0.01;
        double y = getWholePart(hitVec.y) + yCenter * getFractionalPart(hitVec.y) + 0.5 * (1 - yCenter) * Math.signum(hitVec.y) + yOffset * 0.01;
        double z = getWholePart(hitVec.z) + zCenter * getFractionalPart(hitVec.z) + 0.5 * (1 - zCenter) * Math.signum(hitVec.z) + zOffset * 0.01;

        AxisAlignedBB box = BubbleStandEntity.calculateBoundingBox(orientation, new Vector3d(x, y, z), BubbleStandEntity.DEFAULT_LENGTH);

        if (world.hasNoCollisions(null, box, entity -> true) && world.getEntitiesWithinAABBExcludingEntity(null, box).isEmpty()) {
            if (!world.isRemote) {
                BubbleStandEntity entity = new BubbleStandEntity(world, x, y, z, orientation);

                entity.rotationYaw = (float) MathHelper.floor((MathHelper.wrapDegrees(context.getPlacementYaw() - 180.0F) + 22.5F) / 90.0F) * 90.0F;

                world.addEntity(entity);
                world.playSound(null, entity.getPosX(), entity.getPosY(), entity.getPosZ(), SoundEvents.ENTITY_ARMOR_STAND_PLACE, SoundCategory.BLOCKS, 0.75F, 0.8F);
            }

            itemstack.shrink(1);
            return ActionResultType.func_233537_a_(world.isRemote);
        }

        return ActionResultType.FAIL;
    }

    private static double getFractionalPart(double n) {
        if (n > 0) {
            return n - Math.floor(n);
        } else {
            return ((n - Math.ceil(n)) * -1);
        }
    }

    private static double getWholePart(double n) {
        if (n > 0) {
            return Math.floor(n);
        } else {
            return Math.ceil(n);
        }
    }
}

package jozufozu.bubbles.content.stands;

import net.minecraft.entity.EntityType;
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

import net.minecraft.item.Item.Properties;

public class BubbleStandItem extends Item {

    public final EntityType<? extends AbstractStandEntity> type;

    public BubbleStandItem(EntityType<? extends AbstractStandEntity> type) {
        super(new Properties().tab(ItemGroup.TAB_MISC));

        this.type = type;
    }

    public ActionResultType useOn(ItemUseContext context) {
        Direction orientation = context.getClickedFace();
        World world = context.getLevel();
        ItemStack itemstack = context.getItemInHand();

        Vector3d hitVec = context.getClickLocation();

        int xOffset = orientation.getStepX();
        int yOffset = orientation.getStepY();
        int zOffset = orientation.getStepZ();
        int xCenter = Math.abs(xOffset);
        int yCenter = Math.abs(yOffset);
        int zCenter = Math.abs(zOffset);

        double x = getWholePart(hitVec.x) + xCenter * getFractionalPart(hitVec.x) + 0.5 * (1 - xCenter) * Math.signum(hitVec.x) + xOffset * 0.01;
        double y = getWholePart(hitVec.y) + yCenter * getFractionalPart(hitVec.y) + 0.5 * (1 - yCenter) * Math.signum(hitVec.y) + yOffset * 0.01;
        double z = getWholePart(hitVec.z) + zCenter * getFractionalPart(hitVec.z) + 0.5 * (1 - zCenter) * Math.signum(hitVec.z) + zOffset * 0.01;

        AxisAlignedBB box = AbstractStandEntity.calculateBoundingBox(orientation, new Vector3d(x, y, z));

        if (world.noCollision(null, box, entity -> true) && world.getEntities(null, box).isEmpty()) {
            if (!world.isClientSide) {
                AbstractStandEntity entity = type.create(world);

                entity.setPos(x, y, z);
                entity.setOrientation(orientation);

                entity.yRot = (float) MathHelper.floor((MathHelper.wrapDegrees(context.getRotation() - 180.0F) + 22.5F) / 90.0F) * 90.0F;

                world.addFreshEntity(entity);
                world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ARMOR_STAND_PLACE, SoundCategory.BLOCKS, 0.75F, 0.8F);
            }

            itemstack.shrink(1);
            return ActionResultType.sidedSuccess(world.isClientSide);
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

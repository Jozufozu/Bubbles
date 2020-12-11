package red4.bubbles.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.ICollisionReader;
import net.minecraft.world.IEntityReader;
import net.minecraft.world.border.WorldBorder;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;

public class CollisionUtil {
    public static Iterator<Collider.BlockCollider> makeBlockColliderIterator(ICollisionReader world, @Nullable Entity entity, AxisAlignedBB box) {
        return new ColliderIterator(world, entity, box);
    }

    public static Iterator<Collider.EntityCollider> makeEntityColliderIterator(IEntityReader world, @Nullable Entity entity, AxisAlignedBB box) {
        if (box.getAverageEdgeLength() < 1.0E-7D) {
            return Collections.emptyIterator();
        } else {
            AxisAlignedBB marginallyLarger = box.grow(1.0E-7D);
            return world.getEntitiesInAABBexcluding(entity, marginallyLarger, null).stream().map(Collider.EntityCollider::new).iterator();
        }
    }

    @Nullable
    public static Collider.WorldBorderCollider checkWorldBorder(Entity entity, ICollisionReader world) {
        WorldBorder worldborder = world.getWorldBorder();
        AxisAlignedBB axisalignedbb = entity.getBoundingBox();
        if (!checkWorldBorderCollisionWithBox(worldborder, axisalignedbb)) {
            VoxelShape shape = worldborder.getShape();
            if (!checkMarginallySmallerBox(shape, axisalignedbb) && checkMarginallyLargerBox(shape, axisalignedbb)) {
                return new Collider.WorldBorderCollider(worldborder, shape);
            }
        }

        return null;
    }

    private static boolean checkMarginallyLargerBox(VoxelShape shape, AxisAlignedBB box) {
        return VoxelShapes.compare(shape, VoxelShapes.create(box.grow(1.0E-7D)), IBooleanFunction.AND);
    }

    private static boolean checkMarginallySmallerBox(VoxelShape shape, AxisAlignedBB box) {
        return VoxelShapes.compare(shape, VoxelShapes.create(box.shrink(1.0E-7D)), IBooleanFunction.AND);
    }

    private static boolean checkWorldBorderCollisionWithBox(WorldBorder border, AxisAlignedBB box) {
        double d0 = MathHelper.floor(border.minX());
        double d1 = MathHelper.floor(border.minZ());
        double d2 = MathHelper.ceil(border.maxX());
        double d3 = MathHelper.ceil(border.maxZ());
        return box.minX > d0 && box.minX < d2 && box.minZ > d1 && box.minZ < d3 && box.maxX > d0 && box.maxX < d2 && box.maxZ > d1 && box.maxZ < d3;
    }
}

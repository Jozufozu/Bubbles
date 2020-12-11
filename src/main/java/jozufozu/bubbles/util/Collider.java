package jozufozu.bubbles.util;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.border.WorldBorder;

public abstract class Collider {

    public final VoxelShape collider;

    public Collider(VoxelShape collider) {
        this.collider = collider;
    }

    public static class BlockCollider extends Collider {
        public final BlockState state;
        public final BlockPos pos;

        public BlockCollider(BlockState state, VoxelShape collider, BlockPos pos) {
            super(collider);
            this.state = state;
            this.pos = pos;
        }
    }

    public static class EntityCollider extends Collider {
        public final Entity entity;

        public EntityCollider(Entity entity) {
            super(VoxelShapes.create(entity.getBoundingBox()));
            this.entity = entity;
        }
    }

    public static class WorldBorderCollider extends Collider {
        public final WorldBorder border;

        public WorldBorderCollider(WorldBorder border, VoxelShape collider) {
            super(collider);
            this.border = border;
        }
    }
}

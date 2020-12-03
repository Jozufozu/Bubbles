package red4.bubbles.util;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
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

        public BlockCollider(BlockState state, VoxelShape collider) {
            super(collider);
            this.state = state;
        }
    }

    public static class EntityCollider extends Collider {
        public final Entity state;

        public EntityCollider(Entity entity) {
            super(VoxelShapes.create(entity.getBoundingBox()));
            this.state = entity;
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

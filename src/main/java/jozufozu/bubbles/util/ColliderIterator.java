package jozufozu.bubbles.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.CubeCoordinateIterator;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ICollisionReader;

import javax.annotation.Nullable;
import java.util.Iterator;

public class ColliderIterator implements Iterator<Collider.BlockCollider> {
    private final AxisAlignedBB aabb;
    private final ISelectionContext context;
    private final CubeCoordinateIterator cubeCoordinateIterator;
    private final BlockPos.Mutable mutablePos;
    private final VoxelShape checkShape;
    private final ICollisionReader world;

    private Collider.BlockCollider next;

    public ColliderIterator(ICollisionReader world, @Nullable Entity entity, AxisAlignedBB aabb) {
        this.context = entity == null ? ISelectionContext.empty() : ISelectionContext.of(entity);
        this.mutablePos = new BlockPos.Mutable();
        this.checkShape = VoxelShapes.create(aabb);
        this.world = world;
        this.aabb = aabb;
        int minX = MathHelper.floor(aabb.minX - 1.0E-7D) - 1;
        int minY = MathHelper.floor(aabb.minY - 1.0E-7D) - 1;
        int minZ = MathHelper.floor(aabb.minZ - 1.0E-7D) - 1;
        int maxX = MathHelper.floor(aabb.maxX + 1.0E-7D) + 1;
        int maxY = MathHelper.floor(aabb.maxY + 1.0E-7D) + 1;
        int maxZ = MathHelper.floor(aabb.maxZ + 1.0E-7D) + 1;
        this.cubeCoordinateIterator = new CubeCoordinateIterator(minX, minY, minZ, maxX, maxY, maxZ);

        this.next = this.nextInternal();
    }

    @Nullable
    private Collider.BlockCollider nextInternal() {
        return nextBlock();
    }

    @Nullable
    private Collider.BlockCollider nextBlock() {
        while (true) {
            if (this.cubeCoordinateIterator.advance()) {
                int x = this.cubeCoordinateIterator.nextX();
                int y = this.cubeCoordinateIterator.nextY();
                int z = this.cubeCoordinateIterator.nextZ();
                int boundariesTouched = this.cubeCoordinateIterator.getNextType();
                if (boundariesTouched == 3) {
                    continue;
                }

                IBlockReader chunkReader = this.chunkReader(x, z);
                if (chunkReader == null) {
                    continue;
                }

                this.mutablePos.set(x, y, z);
                BlockState state = chunkReader.getBlockState(this.mutablePos);

                if (state.is(Blocks.AIR)) continue;
                //            if (boundariesTouched == 1 && !state.isCollisionShapeLargerThanFullBlock() || boundariesTouched == 2 && !state.isIn(Blocks.MOVING_PISTON)) {
                //               continue;
                //            }

                VoxelShape voxelshape = state.getCollisionShape(this.world, this.mutablePos, this.context);
                if (voxelshape == VoxelShapes.block()) {
                    if (!this.aabb.intersects(x, y, z, (double)x + 1.0D, (double)y + 1.0D, (double)z + 1.0D)) {
                        continue;
                    }

                    return new Collider.BlockCollider(state, voxelshape.move(x, y, z), this.mutablePos.immutable());
                }

                VoxelShape translated = voxelshape.move(x, y, z);
                if (!VoxelShapes.joinIsNotEmpty(translated, this.checkShape, IBooleanFunction.AND)) {
                    continue;
                }

                return new Collider.BlockCollider(state, voxelshape, this.mutablePos.immutable());
            }

            return null;
        }
    }

    @Nullable
    private IBlockReader chunkReader(int x, int z) {
        return this.world.getChunkForCollisions(x >> 4, z >> 4);
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public Collider.BlockCollider next() {
        Collider.BlockCollider next = this.next;

        this.next = nextInternal();

        return next;
    }
}

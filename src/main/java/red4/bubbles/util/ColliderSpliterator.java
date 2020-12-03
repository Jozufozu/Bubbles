package red4.bubbles.util;

import java.util.*;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.collect.Iterators;
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
import net.minecraft.world.IBiomeReader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IEntityReader;
import net.minecraft.world.border.WorldBorder;

import javax.annotation.Nullable;

public class ColliderSpliterator extends AbstractSpliterator<Collider> {
   @Nullable
   private final Entity entity;
   private final AxisAlignedBB aabb;
   private final ISelectionContext context;
   private final CubeCoordinateIterator cubeCoordinateIterator;
   private final Iterator<Collider.EntityCollider> entityIterator;
   private final BlockPos.Mutable mutablePos;
   private final VoxelShape checkShape;
   private final IBiomeReader world;
   private boolean entityIsNotNull;

   private static Iterator<Collider.EntityCollider> makeEntityColliderIterator(IEntityReader world, @Nullable Entity entity, AxisAlignedBB box) {
      if (box.getAverageEdgeLength() < 1.0E-7D) {
         return Collections.emptyIterator();
      } else {
         AxisAlignedBB marginallyLarger = box.grow(1.0E-7D);
         return world.getEntitiesInAABBexcluding(entity, marginallyLarger, null).stream().map(Collider.EntityCollider::new).iterator();
      }
   }

   public ColliderSpliterator(IBiomeReader world, @Nullable Entity entity, AxisAlignedBB aabb) {
      super(Long.MAX_VALUE, Spliterator.NONNULL | Spliterator.IMMUTABLE);
      this.context = entity == null ? ISelectionContext.dummy() : ISelectionContext.forEntity(entity);
      this.mutablePos = new BlockPos.Mutable();
      this.checkShape = VoxelShapes.create(aabb);
      this.world = world;
      this.entityIsNotNull = entity != null;
      this.entity = entity;
      this.aabb = aabb;
      int minX = MathHelper.floor(aabb.minX - 1.0E-7D) - 1;
      int minY = MathHelper.floor(aabb.minY - 1.0E-7D) - 1;
      int minZ = MathHelper.floor(aabb.minZ - 1.0E-7D) - 1;
      int maxX = MathHelper.floor(aabb.maxX + 1.0E-7D) + 1;
      int maxY = MathHelper.floor(aabb.maxY + 1.0E-7D) + 1;
      int maxZ = MathHelper.floor(aabb.maxZ + 1.0E-7D) + 1;
      this.cubeCoordinateIterator = new CubeCoordinateIterator(minX, minY, minZ, maxX, maxY, maxZ);
      this.entityIterator = makeEntityColliderIterator(world, entity, aabb);
   }

   public boolean tryAdvance(Consumer<? super Collider> consumer) {
      return this.entityIsNotNull && (this.checkWorldBorder(consumer) || this.checkEntities(consumer) || this.checkBlocks(consumer));
   }

   boolean checkEntities(Consumer<? super Collider> consumer) {
      if (entityIterator.hasNext()) {
         consumer.accept(entityIterator.next());
         return true;
      } else {
         return false;
      }
   }

   boolean checkBlocks(Consumer<? super Collider> consumer) {
      while (true) {
         if (this.cubeCoordinateIterator.hasNext()) {
            int x = this.cubeCoordinateIterator.getX();
            int y = this.cubeCoordinateIterator.getY();
            int z = this.cubeCoordinateIterator.getZ();
            int boundariesTouched = this.cubeCoordinateIterator.numBoundariesTouched();
            if (boundariesTouched == 3) {
               continue;
            }

            IBlockReader chunkReader = this.chunkReader(x, z);
            if (chunkReader == null) {
               continue;
            }

            this.mutablePos.setPos(x, y, z);
            BlockState state = chunkReader.getBlockState(this.mutablePos);
            if (boundariesTouched == 1 && !state.isCollisionShapeLargerThanFullBlock() || boundariesTouched == 2 && !state.isIn(Blocks.MOVING_PISTON)) {
               continue;
            }

            VoxelShape voxelshape = state.getCollisionShape(this.world, this.mutablePos, this.context);
            if (voxelshape == VoxelShapes.fullCube()) {
               if (!this.aabb.intersects(x, y, z, (double)x + 1.0D, (double)y + 1.0D, (double)z + 1.0D)) {
                  continue;
               }

               consumer.accept(new Collider.BlockCollider(state, voxelshape.withOffset(x, y, z)));
               return true;
            }

            VoxelShape translated = voxelshape.withOffset(x, y, z);
            if (!VoxelShapes.compare(translated, this.checkShape, IBooleanFunction.AND)) {
               continue;
            }

            consumer.accept(new Collider.BlockCollider(state, voxelshape));
            return true;
         }

         return false;
      }
   }

   @Nullable
   private IBlockReader chunkReader(int x, int z) {
      return this.world.getBlockReader(x >> 4, z >> 4);
   }

   boolean checkWorldBorder(Consumer<? super Collider> consumer) {
      Objects.requireNonNull(this.entity);
      this.entityIsNotNull = false;
      WorldBorder worldborder = this.world.getWorldBorder();
      AxisAlignedBB axisalignedbb = this.entity.getBoundingBox();
      if (!checkWorldBorderCollisionWithBox(worldborder, axisalignedbb)) {
         VoxelShape shape = worldborder.getShape();
         if (!checkMarginallySmallerBox(shape, axisalignedbb) && checkMarginallyLargerBox(shape, axisalignedbb)) {
            consumer.accept(new Collider.WorldBorderCollider(worldborder, shape));
            return true;
         }
      }

      return false;
   }

   private static boolean checkMarginallyLargerBox(VoxelShape shape, AxisAlignedBB box) {
      return VoxelShapes.compare(shape, VoxelShapes.create(box.grow(1.0E-7D)), IBooleanFunction.AND);
   }

   private static boolean checkMarginallySmallerBox(VoxelShape shape, AxisAlignedBB box) {
      return VoxelShapes.compare(shape, VoxelShapes.create(box.shrink(1.0E-7D)), IBooleanFunction.AND);
   }

   public static boolean checkWorldBorderCollisionWithBox(WorldBorder border, AxisAlignedBB box) {
      double d0 = MathHelper.floor(border.minX());
      double d1 = MathHelper.floor(border.minZ());
      double d2 = MathHelper.ceil(border.maxX());
      double d3 = MathHelper.ceil(border.maxZ());
      return box.minX > d0 && box.minX < d2 && box.minZ > d1 && box.minZ < d3 && box.maxX > d0 && box.maxX < d2 && box.maxZ > d1 && box.maxZ < d3;
   }
}

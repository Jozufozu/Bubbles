package jozufozu.bubbles.block;

import jozufozu.bubbles.Bubbles;
import jozufozu.bubbles.entity.BubbleEntity;
import jozufozu.bubbles.entity.BubbleStandEntity;
import jozufozu.bubbles.util.ShapeUtil;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class BellowsBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;

    public static final VoxelShape SHAPE_NORTH;
    public static final VoxelShape SHAPE_SOUTH;
    public static final VoxelShape SHAPE_EAST;
    public static final VoxelShape SHAPE_WEST;

    static {
        VoxelShape bottom = VoxelShapes.create(0d, 0d, 1d / 16d, 1d, 2d / 16d, 1d);
        VoxelShape lip = VoxelShapes.create(0d, 2d / 16d, 1d / 16d, 1d, 4d / 16d, 3d / 16d);

        VoxelShape base = VoxelShapes.combine(bottom, lip, IBooleanFunction.OR);

        VoxelShape nozzleBase = VoxelShapes.create(6d / 16d, 0d, 0d, 10d / 16d, 4d / 16d, 1d / 16d);
        VoxelShape nozzleSpout = VoxelShapes.create(7d / 16d, 1d / 16d, -2d / 16d, 9d / 16d, 3d / 16d, 0d);

        VoxelShape nozzle = VoxelShapes.combine(nozzleSpout, nozzleBase, IBooleanFunction.OR);

        SHAPE_NORTH = VoxelShapes.combineAndSimplify(base, nozzle, IBooleanFunction.OR);
        SHAPE_SOUTH = ShapeUtil.rotateY(SHAPE_NORTH, 180);
        SHAPE_WEST = ShapeUtil.rotateY(SHAPE_NORTH, 90);
        SHAPE_EAST = ShapeUtil.rotateY(SHAPE_NORTH, 270);
    }

    public BellowsBlock() {
        super(AbstractBlock.Properties.create(Material.WOOD));

        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH).with(TRIGGERED, false));
    }

    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        boolean powered = worldIn.isBlockPowered(pos);
        boolean triggered = state.get(TRIGGERED);
        if (powered && !triggered) {
            worldIn.getPendingBlockTicks().scheduleTick(pos, this, 2);
            worldIn.setBlockState(pos, state.with(TRIGGERED, true), 3);
        } else if (!powered && triggered) {
            worldIn.setBlockState(pos, state.with(TRIGGERED, false), 3);
        }
    }

    @Override
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        if (!state.get(TRIGGERED)) {

        }
    }

    @Override
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        Direction direction = state.get(FACING);

        int dX = direction.getXOffset();
        int dZ = direction.getZOffset();

        worldIn.playSound(null, pos, Bubbles.BELLOWS_BLOW.get(), SoundCategory.BLOCKS, 1.0f, 0.95f + 0.1f * rand.nextFloat());

        AxisAlignedBB push = getPushZone(state, pos);

        double scale = 0.01;
        BubbleEntity.PushForce force = new BubbleEntity.PushForce(15, new Vector3d(dX * scale, 0, dZ * scale));

        for (BubbleEntity bubble : worldIn.getEntitiesWithinAABB(BubbleEntity.class, push)) {
            bubble.addForce(force.copy());
        }

        AxisAlignedBB front = new AxisAlignedBB(pos.offset(direction));
        for (BubbleStandEntity stand : worldIn.getEntitiesWithinAABB(BubbleStandEntity.class, front)) {

            if (stand.getAttachmentBox().intersects(front))
                stand.blowFrom(pos, dX, dZ, force.copy());
        }
    }

    public AxisAlignedBB getPushZone(BlockState state, BlockPos pos) {
        Direction direction = state.get(FACING);

        return new AxisAlignedBB(pos.offset(direction, 2)).grow(1, 0.5, 1);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Direction direction = context.getPlacementHorizontalFacing().getOpposite();

        return this.getDefaultState().with(FACING, direction);
    }

    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, TRIGGERED);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        Direction direction = state.get(FACING);

        if (direction == Direction.NORTH) return SHAPE_NORTH;
        if (direction == Direction.SOUTH) return SHAPE_SOUTH;
        if (direction == Direction.EAST) return SHAPE_EAST;
        if (direction == Direction.WEST) return SHAPE_WEST;

        return VoxelShapes.empty();
    }

    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public boolean isTransparent(BlockState state) {
        return true;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side) {
        return true;
    }

    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.toRotation(state.get(FACING)));
    }
}

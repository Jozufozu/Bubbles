package jozufozu.bubbles.block;

import jozufozu.bubbles.Bubbles;
import jozufozu.bubbles.entity.BubbleEntity;
import jozufozu.bubbles.entity.BubbleStandEntity;
import jozufozu.bubbles.util.ShapeUtil;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
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
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.Random;

public class BellowsBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;

    public static final VoxelShape[] UNPRESSED_SHAPES = new VoxelShape[4];
    public static final VoxelShape[] PRESSED_SHAPES = new VoxelShape[4];
    // public static final AxisAlignedBB DETECTION_AABB = new AxisAlignedBB(0d, 4d / 16d, 0d, 1d, 14d / 16d, 1d);

    static {
        VoxelShape bottom = Block.makeCuboidShape(0, 0, 1, 16, 2, 16);
        VoxelShape lip = Block.makeCuboidShape(0, 2, 1, 16, 4, 3);

        VoxelShape base = VoxelShapes.combine(bottom, lip, IBooleanFunction.OR);

        VoxelShape nozzleBase = Block.makeCuboidShape(6, 0, 0, 10, 4, 1);
        VoxelShape nozzleSpout = Block.makeCuboidShape(7, 1, -2, 9, 3, 0);

        VoxelShape nozzle = VoxelShapes.combine(nozzleSpout, nozzleBase, IBooleanFunction.OR);

        VoxelShape northBase = VoxelShapes.combine(base, nozzle, IBooleanFunction.OR);

        VoxelShape unpressedAccordion = Block.makeCuboidShape(0, 2, 3, 16, 10, 16);
        VoxelShape unpressedNorth = VoxelShapes.combineAndSimplify(northBase, unpressedAccordion, IBooleanFunction.OR);
        UNPRESSED_SHAPES[0] = ShapeUtil.rotateY(unpressedNorth, 180);
        UNPRESSED_SHAPES[1] = ShapeUtil.rotateY(unpressedNorth, 90);
        UNPRESSED_SHAPES[2] = unpressedNorth;
        UNPRESSED_SHAPES[3] = ShapeUtil.rotateY(unpressedNorth, 270);

        VoxelShape pressedAccordion = Block.makeCuboidShape(0, 2, 3, 16, 4, 16);
        VoxelShape pressedNorth = VoxelShapes.combineAndSimplify(northBase, pressedAccordion, IBooleanFunction.OR);
        PRESSED_SHAPES[0] = ShapeUtil.rotateY(pressedNorth, 180);
        PRESSED_SHAPES[1] = ShapeUtil.rotateY(pressedNorth, 90);
        PRESSED_SHAPES[2] = pressedNorth;
        PRESSED_SHAPES[3] = ShapeUtil.rotateY(pressedNorth, 270);
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
    public void tick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
        blow(state, world, pos, rand);
    }

    private void blow(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
        world.playSound(null, pos, Bubbles.BELLOWS_BLOW.get(), SoundCategory.BLOCKS, 0.8f, 0.95f + 0.1f * rand.nextFloat());

        Direction direction = state.get(FACING);

        BlockPos inFront = pos.offset(direction);
        BlockState stateInFront = world.getBlockState(inFront);
        if (stateInFront.isIn(Blocks.SOUL_FIRE) || stateInFront.isIn(ModBlocks.BLAZING_SOUL_FIRE.get())) {
            world.playSound(null, inFront, Bubbles.SOUL_BURN.get(), SoundCategory.BLOCKS, 0.4f, 0.8f + 0.8f * rand.nextFloat());
            world.setBlockState(inFront, ModBlocks.BLAZING_SOUL_FIRE.get().getDefaultState());

            return;
        }

        int dX = direction.getXOffset();
        int dZ = direction.getZOffset();

        AxisAlignedBB push = getPushZone(state, pos);

        double scale = 0.01;
        BubbleEntity.PushForce force = new BubbleEntity.PushForce(15, dX * scale, 0, dZ * scale);

        for (BubbleEntity bubble : world.getEntitiesWithinAABB(BubbleEntity.class, push)) {
            bubble.addForce(force.copy());
        }

        AxisAlignedBB front = new AxisAlignedBB(inFront);
        for (BubbleStandEntity stand : world.getEntitiesWithinAABB(BubbleStandEntity.class, front)) {

            if (stand.getAttachmentBox().intersects(front))
                stand.blowFrom(pos, dX, dZ, force.copy());
        }
    }

    public static AxisAlignedBB getPushZone(BlockState state, BlockPos pos) {
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
        int i = state.get(FACING).getHorizontalIndex();

        return state.get(TRIGGERED) ? PRESSED_SHAPES[i] : UNPRESSED_SHAPES[i];
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

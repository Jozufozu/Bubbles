package jozufozu.bubbles.content;

import jozufozu.bubbles.AllBlocks;
import jozufozu.bubbles.AllSounds;
import jozufozu.bubbles.content.stands.AbstractStandEntity;
import jozufozu.bubbles.content.core.BubbleEntity;
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
        VoxelShape bottom = Block.box(0, 0, 1, 16, 2, 16);
        VoxelShape lip = Block.box(0, 2, 1, 16, 4, 3);

        VoxelShape base = VoxelShapes.joinUnoptimized(bottom, lip, IBooleanFunction.OR);

        VoxelShape nozzleBase = Block.box(6, 0, 0, 10, 4, 1);
        VoxelShape nozzleSpout = Block.box(7, 1, -2, 9, 3, 0);

        VoxelShape nozzle = VoxelShapes.joinUnoptimized(nozzleSpout, nozzleBase, IBooleanFunction.OR);

        VoxelShape northBase = VoxelShapes.joinUnoptimized(base, nozzle, IBooleanFunction.OR);

        VoxelShape unpressedAccordion = Block.box(0, 2, 3, 16, 10, 16);
        VoxelShape unpressedNorth = VoxelShapes.join(northBase, unpressedAccordion, IBooleanFunction.OR);
        UNPRESSED_SHAPES[0] = ShapeUtil.rotateY(unpressedNorth, 180);
        UNPRESSED_SHAPES[1] = ShapeUtil.rotateY(unpressedNorth, 90);
        UNPRESSED_SHAPES[2] = unpressedNorth;
        UNPRESSED_SHAPES[3] = ShapeUtil.rotateY(unpressedNorth, 270);

        VoxelShape pressedAccordion = Block.box(0, 2, 3, 16, 4, 16);
        VoxelShape pressedNorth = VoxelShapes.join(northBase, pressedAccordion, IBooleanFunction.OR);
        PRESSED_SHAPES[0] = ShapeUtil.rotateY(pressedNorth, 180);
        PRESSED_SHAPES[1] = ShapeUtil.rotateY(pressedNorth, 90);
        PRESSED_SHAPES[2] = pressedNorth;
        PRESSED_SHAPES[3] = ShapeUtil.rotateY(pressedNorth, 270);
    }

    public BellowsBlock() {
        super(AbstractBlock.Properties.of(Material.WOOD));

        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TRIGGERED, false));
    }

    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        boolean powered = worldIn.hasNeighborSignal(pos);
        boolean triggered = state.getValue(TRIGGERED);
        if (powered && !triggered) {
            worldIn.getBlockTicks().scheduleTick(pos, this, 2);
            worldIn.setBlock(pos, state.setValue(TRIGGERED, true), 3);
        } else if (!powered && triggered) {
            worldIn.setBlock(pos, state.setValue(TRIGGERED, false), 3);
        }
    }

    @Override
    public void tick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
        blow(state, world, pos, rand);
    }

    private void blow(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
        world.playSound(null, pos, AllSounds.BELLOWS_BLOW.get(), SoundCategory.BLOCKS, 0.8f, 0.95f + 0.1f * rand.nextFloat());

        Direction direction = state.getValue(FACING);

        BlockPos inFront = pos.relative(direction);
        BlockState stateInFront = world.getBlockState(inFront);
        if (stateInFront.is(Blocks.SOUL_FIRE) || stateInFront.is(AllBlocks.BLAZING_SOUL_FIRE.get())) {
            world.playSound(null, inFront, AllSounds.SOUL_BURN.get(), SoundCategory.BLOCKS, 0.4f, 0.8f + 0.8f * rand.nextFloat());
            world.setBlockAndUpdate(inFront, AllBlocks.BLAZING_SOUL_FIRE.get().defaultBlockState());

            return;
        }

        int dX = direction.getStepX();
        int dZ = direction.getStepZ();

        AxisAlignedBB push = getPushZone(state, pos);

        double scale = 0.01;
        BubbleEntity.PushForce force = new BubbleEntity.PushForce(15, dX * scale, 0, dZ * scale);

        for (BubbleEntity bubble : world.getEntitiesOfClass(BubbleEntity.class, push)) {
            bubble.addForce(force.copy());
        }

        AxisAlignedBB front = new AxisAlignedBB(inFront);
        for (AbstractStandEntity stand : world.getEntitiesOfClass(AbstractStandEntity.class, front)) {

            if (stand.getAttachmentBox().intersects(front))
                stand.blowFrom(pos, dX, dZ, force.copy());
        }
    }

    public static AxisAlignedBB getPushZone(BlockState state, BlockPos pos) {
        Direction direction = state.getValue(FACING);

        return new AxisAlignedBB(pos.relative(direction, 2)).inflate(1, 0.5, 1);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Direction direction = context.getHorizontalDirection().getOpposite();

        return this.defaultBlockState().setValue(FACING, direction);
    }

    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, TRIGGERED);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        int i = state.getValue(FACING).get2DDataValue();

        return state.getValue(TRIGGERED) ? PRESSED_SHAPES[i] : UNPRESSED_SHAPES[i];
    }

    public BlockRenderType getRenderShape(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side) {
        return true;
    }

    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }
}

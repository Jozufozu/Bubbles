package jozufozu.bubbles.content;

import jozufozu.bubbles.content.core.BubbleEntity;
import jozufozu.bubbles.util.ShapeUtil;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class BubblePlate extends Block implements ISafeBlock {
    protected static final VoxelShape[] UNPRESSED_SHAPES = new VoxelShape[6];
    protected static final VoxelShape[] PRESSED_SHAPES = new VoxelShape[6];
    protected static final AxisAlignedBB DETECTION_AABB = new AxisAlignedBB(0.0625, 0.0625, 0.0625, 0.9375, 0.9375, 0.9375);

    static {
        VoxelShape pressedNorth = Block.box(1, 1, 0, 15, 15, 1);
        PRESSED_SHAPES[0] = Block.box(1, 0, 1, 15, 1, 15);
        PRESSED_SHAPES[1] = Block.box(1, 15D, 1, 15, 16, 15);
        PRESSED_SHAPES[2] = pressedNorth;
        PRESSED_SHAPES[3] = ShapeUtil.rotateY(pressedNorth, 180);
        PRESSED_SHAPES[4] = ShapeUtil.rotateY(pressedNorth, 90);
        PRESSED_SHAPES[5] = ShapeUtil.rotateY(pressedNorth, 270);

        VoxelShape unpressedNorth = Block.box(1, 1, 0, 15, 15, 2);
        UNPRESSED_SHAPES[0] = Block.box(1, 0, 1, 15, 2, 15);
        UNPRESSED_SHAPES[1] = Block.box(1, 14, 1, 15, 16, 15);
        UNPRESSED_SHAPES[2] = unpressedNorth;
        UNPRESSED_SHAPES[3] = ShapeUtil.rotateY(unpressedNorth, 180);
        UNPRESSED_SHAPES[4] = ShapeUtil.rotateY(unpressedNorth, 90);
        UNPRESSED_SHAPES[5] = ShapeUtil.rotateY(unpressedNorth, 270);
    }

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final DirectionProperty SIDE = DirectionProperty.create("side", dir -> true);

    public BubblePlate() {
        super(AbstractBlock.Properties.of(SoapBlock.SOAP).friction(0.99f));

        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, Boolean.FALSE).setValue(SIDE, Direction.DOWN));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(POWERED, SIDE);
    }

    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        int index = state.getValue(SIDE).get3DDataValue();
        return this.getRedstoneStrength(state) > 0 ? PRESSED_SHAPES[index] : UNPRESSED_SHAPES[index];
    }

    /**
     * Return true if an entity can be spawned inside the block (used to get the player's bed spawn location)
     */
    public boolean isPossibleToRespawnInThis() {
        return true;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.defaultBlockState().setValue(SIDE, context.getClickedFace().getOpposite());
    }

    /**
     * Update the provided state given the provided neighbor facing and neighbor state, returning a new state.
     * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
     * returns its solidified counterpart.
     * Note that this method should ideally consider only the specific face passed in.
     */
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        return !stateIn.canSurvive(worldIn, currentPos) ? Blocks.AIR.defaultBlockState() : stateIn;
    }

    public boolean canSurvive(BlockState state, IWorldReader worldIn, BlockPos pos) {
        Direction direction = state.getValue(SIDE);
        BlockPos blockpos = pos.relative(direction);
        return canSupportCenter(worldIn, blockpos, direction.getOpposite());
    }

    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        int i = this.getRedstoneStrength(state);
        if (i > 0) {
            this.updateState(worldIn, pos, state, i);
        }
    }

    public void entityInside(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        if (!worldIn.isClientSide) {
            int i = this.getRedstoneStrength(state);
            if (i == 0) {
                this.updateState(worldIn, pos, state, i);
            }
        }
    }

    /**
     * Updates the pressure plate when stepped on
     */
    protected void updateState(World worldIn, BlockPos pos, BlockState state, int oldRedstoneStrength) {
        int i = this.computeRedstoneStrength(worldIn, pos);
        boolean flag = oldRedstoneStrength > 0;
        boolean flag1 = i > 0;
        if (oldRedstoneStrength != i) {
            BlockState blockstate = this.setRedstoneStrength(state, i);
            worldIn.setBlock(pos, blockstate, 2);
            this.updateNeighbors(worldIn, pos, state);
            worldIn.setBlocksDirty(pos, state, blockstate);
        }

        if (!flag1 && flag) {
            this.playClickOffSound(worldIn, pos);
        } else if (flag1 && !flag) {
            this.playClickOnSound(worldIn, pos);
        }

        if (flag1) {
            worldIn.getBlockTicks().scheduleTick(pos.immutable(), this, 20);
        }
    }

    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!isMoving && !state.is(newState.getBlock())) {
            if (this.getRedstoneStrength(state) > 0) {
                this.updateNeighbors(worldIn, pos, state);
            }

            super.onRemove(state, worldIn, pos, newState, isMoving);
        }
    }

    protected void updateNeighbors(World worldIn, BlockPos pos, BlockState state) {
        worldIn.updateNeighborsAt(pos, this);
        worldIn.updateNeighborsAt(pos.relative(state.getValue(SIDE)), this);
    }

    public int getSignal(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        return this.getRedstoneStrength(blockState);
    }

    public int getDirectSignal(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        return side == blockState.getValue(SIDE).getOpposite() ? this.getRedstoneStrength(blockState) : 0;
    }

    public boolean isSignalSource(BlockState state) {
        return true;
    }

    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    protected int getRedstoneStrength(BlockState state) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    protected BlockState setRedstoneStrength(BlockState state, int strength) {
        return state.setValue(POWERED, strength > 0);
    }

    protected void playClickOnSound(IWorld worldIn, BlockPos pos) {
        if (this.material != Material.WOOD && this.material != Material.NETHER_WOOD) {
            worldIn.playSound(null, pos, SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.6F);
        } else {
            worldIn.playSound(null, pos, SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.8F);
        }

    }

    protected void playClickOffSound(IWorld worldIn, BlockPos pos) {
        if (this.material != Material.WOOD && this.material != Material.NETHER_WOOD) {
            worldIn.playSound(null, pos, SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF, SoundCategory.BLOCKS, 0.3F, 0.5F);
        } else {
            worldIn.playSound(null, pos, SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_OFF, SoundCategory.BLOCKS, 0.3F, 0.7F);
        }

    }

    protected int computeRedstoneStrength(World worldIn, BlockPos pos) {
        AxisAlignedBB axisalignedbb = DETECTION_AABB.move(pos);
        List<? extends Entity> list = worldIn.getEntitiesOfClass(BubbleEntity.class, axisalignedbb);

        return list.isEmpty() ? 0 : 15;
    }
}

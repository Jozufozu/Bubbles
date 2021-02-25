package jozufozu.bubbles.block;

import jozufozu.bubbles.entity.BubbleEntity;
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
        VoxelShape pressedNorth = Block.makeCuboidShape(1, 1, 0, 15, 15, 1);
        PRESSED_SHAPES[0] = Block.makeCuboidShape(1, 0, 1, 15, 1, 15);
        PRESSED_SHAPES[1] = Block.makeCuboidShape(1, 15D, 1, 15, 16, 15);
        PRESSED_SHAPES[2] = pressedNorth;
        PRESSED_SHAPES[3] = ShapeUtil.rotateY(pressedNorth, 180);
        PRESSED_SHAPES[4] = ShapeUtil.rotateY(pressedNorth, 90);
        PRESSED_SHAPES[5] = ShapeUtil.rotateY(pressedNorth, 270);

        VoxelShape unpressedNorth = Block.makeCuboidShape(1, 1, 0, 15, 15, 2);
        UNPRESSED_SHAPES[0] = Block.makeCuboidShape(1, 0, 1, 15, 2, 15);
        UNPRESSED_SHAPES[1] = Block.makeCuboidShape(1, 14, 1, 15, 16, 15);
        UNPRESSED_SHAPES[2] = unpressedNorth;
        UNPRESSED_SHAPES[3] = ShapeUtil.rotateY(unpressedNorth, 180);
        UNPRESSED_SHAPES[4] = ShapeUtil.rotateY(unpressedNorth, 90);
        UNPRESSED_SHAPES[5] = ShapeUtil.rotateY(unpressedNorth, 270);
    }

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final DirectionProperty SIDE = DirectionProperty.create("side", dir -> true);

    public BubblePlate() {
        super(AbstractBlock.Properties.create(SoapBlock.SOAP).slipperiness(0.99f));

        this.setDefaultState(this.stateContainer.getBaseState().with(POWERED, Boolean.FALSE).with(SIDE, Direction.DOWN));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(POWERED, SIDE);
    }

    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        int index = state.get(SIDE).getIndex();
        return this.getRedstoneStrength(state) > 0 ? PRESSED_SHAPES[index] : UNPRESSED_SHAPES[index];
    }

    /**
     * Return true if an entity can be spawned inside the block (used to get the player's bed spawn location)
     */
    public boolean canSpawnInBlock() {
        return true;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(SIDE, context.getFace().getOpposite());
    }

    /**
     * Update the provided state given the provided neighbor facing and neighbor state, returning a new state.
     * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
     * returns its solidified counterpart.
     * Note that this method should ideally consider only the specific face passed in.
     */
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        return !stateIn.isValidPosition(worldIn, currentPos) ? Blocks.AIR.getDefaultState() : stateIn;
    }

    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        Direction direction = state.get(SIDE);
        BlockPos blockpos = pos.offset(direction);
        return hasEnoughSolidSide(worldIn, blockpos, direction.getOpposite());
    }

    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        int i = this.getRedstoneStrength(state);
        if (i > 0) {
            this.updateState(worldIn, pos, state, i);
        }
    }

    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        if (!worldIn.isRemote) {
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
            worldIn.setBlockState(pos, blockstate, 2);
            this.updateNeighbors(worldIn, pos, state);
            worldIn.markBlockRangeForRenderUpdate(pos, state, blockstate);
        }

        if (!flag1 && flag) {
            this.playClickOffSound(worldIn, pos);
        } else if (flag1 && !flag) {
            this.playClickOnSound(worldIn, pos);
        }

        if (flag1) {
            worldIn.getPendingBlockTicks().scheduleTick(pos.toImmutable(), this, 20);
        }
    }

    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!isMoving && !state.isIn(newState.getBlock())) {
            if (this.getRedstoneStrength(state) > 0) {
                this.updateNeighbors(worldIn, pos, state);
            }

            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    protected void updateNeighbors(World worldIn, BlockPos pos, BlockState state) {
        worldIn.notifyNeighborsOfStateChange(pos, this);
        worldIn.notifyNeighborsOfStateChange(pos.offset(state.get(SIDE)), this);
    }

    public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        return this.getRedstoneStrength(blockState);
    }

    public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        return side == blockState.get(SIDE).getOpposite() ? this.getRedstoneStrength(blockState) : 0;
    }

    public boolean canProvidePower(BlockState state) {
        return true;
    }

    public PushReaction getPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    protected int getRedstoneStrength(BlockState state) {
        return state.get(POWERED) ? 15 : 0;
    }

    protected BlockState setRedstoneStrength(BlockState state, int strength) {
        return state.with(POWERED, strength > 0);
    }

    protected void playClickOnSound(IWorld worldIn, BlockPos pos) {
        if (this.material != Material.WOOD && this.material != Material.NETHER_WOOD) {
            worldIn.playSound(null, pos, SoundEvents.BLOCK_STONE_PRESSURE_PLATE_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.6F);
        } else {
            worldIn.playSound(null, pos, SoundEvents.BLOCK_WOODEN_PRESSURE_PLATE_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.8F);
        }

    }

    protected void playClickOffSound(IWorld worldIn, BlockPos pos) {
        if (this.material != Material.WOOD && this.material != Material.NETHER_WOOD) {
            worldIn.playSound(null, pos, SoundEvents.BLOCK_STONE_PRESSURE_PLATE_CLICK_OFF, SoundCategory.BLOCKS, 0.3F, 0.5F);
        } else {
            worldIn.playSound(null, pos, SoundEvents.BLOCK_WOODEN_PRESSURE_PLATE_CLICK_OFF, SoundCategory.BLOCKS, 0.3F, 0.7F);
        }

    }

    protected int computeRedstoneStrength(World worldIn, BlockPos pos) {
        AxisAlignedBB axisalignedbb = DETECTION_AABB.offset(pos);
        List<? extends Entity> list = worldIn.getEntitiesWithinAABB(BubbleEntity.class, axisalignedbb);

        return list.isEmpty() ? 0 : 15;
    }
}

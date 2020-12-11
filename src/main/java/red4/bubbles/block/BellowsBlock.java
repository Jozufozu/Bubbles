package red4.bubbles.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
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
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import red4.bubbles.Bubbles;
import red4.bubbles.entity.BubbleEntity;

import javax.annotation.Nullable;
import java.util.Random;

public class BellowsBlock extends Block {
    public static final DirectionProperty FACING_EXCEPT_UP = DirectionProperty.create("facing", (direction) -> direction != Direction.DOWN);
    public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;

    public static final VoxelShape SHAPE = VoxelShapes.create(3d / 16d, 0d, 1d / 16d, 13d / 16d, 1d / 16d, 1d);

    public BellowsBlock() {
        super(AbstractBlock.Properties.create(Material.WOOD));

        this.setDefaultState(this.stateContainer.getBaseState().with(FACING_EXCEPT_UP, Direction.NORTH).with(TRIGGERED, false));
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
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        Direction direction = state.get(FACING_EXCEPT_UP);

        int dX = direction.getXOffset();
        int dY = direction.getYOffset();
        int dZ = direction.getZOffset();

        worldIn.playSound(null, pos, Bubbles.BELLOWS_BLOW.get(), SoundCategory.BLOCKS, 1.0f, 1.0f);

        AxisAlignedBB push = getPushZone(state, pos);

        for (BubbleEntity bubble : worldIn.getEntitiesWithinAABB(BubbleEntity.class, push)) {
            double scale = 0.01;

            bubble.addForce(new BubbleEntity.PushForce(15, new Vector3d(dX * scale, 0, dZ * scale)));
        }
    }

    public AxisAlignedBB getPushZone(BlockState state, BlockPos pos) {
        Direction direction = state.get(FACING_EXCEPT_UP);

        return new AxisAlignedBB(pos.offset(direction, 3)).grow(2, 1, 2);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Direction direction = context.getNearestLookingDirection().getOpposite();

        if (direction == Direction.DOWN) {
            direction = Direction.UP;
        }

        return this.getDefaultState().with(FACING_EXCEPT_UP, direction);
    }

    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING_EXCEPT_UP, TRIGGERED);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
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
        return state.with(FACING_EXCEPT_UP, rot.rotate(state.get(FACING_EXCEPT_UP)));
    }

    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.toRotation(state.get(FACING_EXCEPT_UP)));
    }
}

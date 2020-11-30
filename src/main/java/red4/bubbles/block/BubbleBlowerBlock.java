package red4.bubbles.block;

import net.minecraft.block.*;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IPosition;
import net.minecraft.dispenser.Position;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import red4.bubbles.entity.BubbleEntity;

import java.util.Random;

public class BubbleBlowerBlock extends Block {
    public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
    public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;

    public BubbleBlowerBlock(Properties properties) {
        super(properties);

        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH).with(TRIGGERED, false));
    }

    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        boolean powered = worldIn.isBlockPowered(pos) || worldIn.isBlockPowered(pos.up());
        boolean triggered = state.get(TRIGGERED);
        if (powered && !triggered) {
            worldIn.getPendingBlockTicks().scheduleTick(pos, this, 4);
            worldIn.setBlockState(pos, state.with(TRIGGERED, true), 4);
        } else if (!powered && triggered) {
            worldIn.setBlockState(pos, state.with(TRIGGERED, false), 4);
        }
    }

    @Override
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        Direction direction = state.get(FACING);
        double x = pos.getX() + 0.9D * (double)direction.getXOffset() + 0.5;
        double y = pos.getY() + 0.9D * (double)direction.getYOffset() + 0.25;
        double z = pos.getZ() + 0.9D * (double)direction.getZOffset() + 0.5;

        worldIn.playEvent(1001, pos, 0);
        worldIn.addEntity(new BubbleEntity(worldIn, x, y, z, direction, 0.2));
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite());
    }

    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, TRIGGERED);
    }

    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.toRotation(state.get(FACING)));
    }
}

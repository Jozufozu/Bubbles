package red4.bubbles.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import red4.bubbles.entity.BubbleEntity;

import java.util.Random;

public class BellowsBlock extends Block {
    public static final DirectionProperty FACING_EXCEPT_UP = DirectionProperty.create("facing", (direction) -> direction != Direction.UP);
    public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;

    public BellowsBlock(Properties properties) {
        super(properties);
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
        Direction direction = state.get(FACING_EXCEPT_UP);

        int dX = direction.getXOffset();
        int dY = direction.getYOffset();
        int dZ = direction.getZOffset();

        BlockPos inFront = pos.offset(direction);
        BlockPos theRest = inFront.offset(direction, 3);

        AxisAlignedBB push = new AxisAlignedBB(inFront, theRest);

        for (BubbleEntity bubble : worldIn.getEntitiesWithinAABB(BubbleEntity.class, push)) {
            Vector3d motion = bubble.getMotion();

            bubble.setMotion(motion.add(dX, 0, dZ));
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Direction direction = context.getNearestLookingDirection().getOpposite();


        return this.getDefaultState().with(FACING_EXCEPT_UP, direction);
    }

    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING_EXCEPT_UP, TRIGGERED);
    }

    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(FACING_EXCEPT_UP, rot.rotate(state.get(FACING_EXCEPT_UP)));
    }

    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.toRotation(state.get(FACING_EXCEPT_UP)));
    }
}

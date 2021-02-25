package jozufozu.bubbles.block;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.Random;

public class BlazingSoulFireBlock extends AbstractFireBlock {

    public static final IntegerProperty BURN_TIME = BlockStateProperties.AGE_0_7;

    public BlazingSoulFireBlock() {
        super(AbstractBlock.Properties.create(Material.FIRE, MaterialColor.LIGHT_BLUE).doesNotBlockMovement().zeroHardnessAndResistance().setLightLevel((state) -> 10).sound(SoundType.CLOTH), 3.0f);
        this.setDefaultState(this.getStateContainer().getBaseState().with(BURN_TIME, 0));
    }

    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        BlockPos up = pos.up();

        if (!worldIn.getBlockState(up).isIn(ModBlocks.UPDRAFT.get())) {
            UpdraftBlock.placeUpdraft(worldIn, up);
        }

        int burn = state.get(BURN_TIME);
        if (burn == 7) {
            worldIn.setBlockState(pos, Blocks.SOUL_FIRE.getDefaultState());
            worldIn.getPendingBlockTicks().scheduleTick(up, ModBlocks.UPDRAFT.get(), 1);
        } else {
            worldIn.setBlockState(pos, state.with(BURN_TIME, burn + 1), 22);
            worldIn.getPendingBlockTicks().scheduleTick(pos, this, 20);
        }
    }

    /**
     * Update the provided state given the provided neighbor facing and neighbor state, returning a new state.
     * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
     * returns its solidified counterpart.
     * Note that this method should ideally consider only the specific face passed in.
     */
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (facing == Direction.UP && facingState.isIn(Blocks.AIR)) {
            worldIn.getPendingBlockTicks().scheduleTick(currentPos, this, 20);
        }

        return this.isValidPosition(stateIn, worldIn, currentPos) ? stateIn : Blocks.AIR.getDefaultState();
    }

    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        worldIn.getPendingBlockTicks().scheduleTick(pos, this, 20);
    }

    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        return SoulFireBlock.shouldLightSoulFire(worldIn.getBlockState(pos.down()).getBlock());
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(BURN_TIME);
    }

    protected boolean canBurn(BlockState state) {
        return true;
    }
}

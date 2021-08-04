package jozufozu.bubbles.content.updrafts;

import jozufozu.bubbles.AllBlocks;
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

    public static final IntegerProperty BURN_TIME = BlockStateProperties.AGE_7;

    public BlazingSoulFireBlock() {
        super(AbstractBlock.Properties.of(Material.FIRE, MaterialColor.COLOR_LIGHT_BLUE).noCollission().instabreak().lightLevel((state) -> 10).sound(SoundType.WOOL), 3.0f);
        this.registerDefaultState(this.getStateDefinition().any().setValue(BURN_TIME, 0));
    }

    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        BlockPos up = pos.above();

        if (!worldIn.getBlockState(up).is(AllBlocks.UPDRAFT.get())) {
            UpdraftBlock.placeUpdraft(worldIn, up);
        }

        int burn = state.getValue(BURN_TIME);
        if (burn == 7) {
            worldIn.setBlockAndUpdate(pos, Blocks.SOUL_FIRE.defaultBlockState());
            worldIn.getBlockTicks().scheduleTick(up, AllBlocks.UPDRAFT.get(), 1);
        } else {
            worldIn.setBlock(pos, state.setValue(BURN_TIME, burn + 1), 22);
            worldIn.getBlockTicks().scheduleTick(pos, this, 20);
        }
    }

    /**
     * Update the provided state given the provided neighbor facing and neighbor state, returning a new state.
     * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
     * returns its solidified counterpart.
     * Note that this method should ideally consider only the specific face passed in.
     */
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (facing == Direction.UP && facingState.is(Blocks.AIR)) {
            worldIn.getBlockTicks().scheduleTick(currentPos, this, 20);
        }

        return this.canSurvive(stateIn, worldIn, currentPos) ? stateIn : Blocks.AIR.defaultBlockState();
    }

    public void onPlace(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        worldIn.getBlockTicks().scheduleTick(pos, this, 20);
    }

    public boolean canSurvive(BlockState state, IWorldReader worldIn, BlockPos pos) {
        return SoulFireBlock.canSurviveOnBlock(worldIn.getBlockState(pos.below()).getBlock());
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BURN_TIME);
    }

    protected boolean canBurn(BlockState state) {
        return true;
    }
}

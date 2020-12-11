package red4.bubbles.block;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.common.extensions.IForgeBlock;

public interface ISafeBlock extends IForgeBlock {
    default boolean isBlockSafe(BlockState state, IWorldReader world, BlockPos pos) {
        return true;
    }
}

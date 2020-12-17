package jozufozu.bubbles.block;

import java.util.Random;

import jozufozu.bubbles.entity.BubbleEntity;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class UpdraftBlock extends Block {
    public static final Material MATERIAL = new Material(MaterialColor.AIR, false, false, false, false, false, true, PushReaction.DESTROY);

    public static final BooleanProperty DOWNDRAFT = BooleanProperty.create("downdraft");

    public UpdraftBlock() {
        super(Properties.create(MATERIAL).doesNotBlockMovement().noDrops());
        this.setDefaultState(this.stateContainer.getBaseState().with(DOWNDRAFT, Boolean.FALSE));
    }

    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        if (entityIn instanceof BubbleEntity || entityIn instanceof ItemEntity) {
            Vector3d vector3d = entityIn.getMotion();
            double d0;
            if (state.get(DOWNDRAFT)) {
                d0 = Math.max(-0.3D, vector3d.y - 0.03D);
            } else {
                d0 = Math.min(0.5D, vector3d.y + 0.04D);
            }

            entityIn.setMotion(vector3d.x, d0, vector3d.z);
            entityIn.fallDistance = 0.0F;
        } else if (entityIn instanceof LivingEntity && ((LivingEntity) entityIn).isElytraFlying()) {
            Vector3d vector3d = entityIn.getMotion();
            double d0;
            if (state.get(DOWNDRAFT)) {
                d0 = Math.max(-0.5D, vector3d.y - 0.04D);
            } else {
                d0 = Math.min(0.8D, vector3d.y + 0.08D);
            }

            entityIn.setMotion(vector3d.x, d0, vector3d.z);
        }
    }

    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        worldIn.getPendingBlockTicks().scheduleTick(pos, this, 1);
    }

    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        placeUpdraft(worldIn, pos.up(), isDowndraft(worldIn, pos));

        if (!state.isValidPosition(worldIn, pos)) {
            worldIn.setBlockState(pos, Blocks.AIR.getDefaultState());
        }
    }

    public static void placeUpdraft(IWorld world, BlockPos pos, boolean drag) {
        if (canHoldUpdraft(world, pos)) {
            world.setBlockState(pos, ModBlocks.UPDRAFT.get().getDefaultState().with(DOWNDRAFT, drag), 2);
        }
    }

    public static boolean canHoldUpdraft(IWorld world, BlockPos pos) {
        return world.isAirBlock(pos);
    }

    private static boolean isDowndraft(IBlockReader reader, BlockPos pos) {
        BlockState blockstate = reader.getBlockState(pos);
        if (blockstate.isIn(ModBlocks.UPDRAFT.get())) {
            return blockstate.get(DOWNDRAFT);
        } else {
            return false;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        double d0 = pos.getX();
        double d1 = pos.getY();
        double d2 = pos.getZ();
        if (stateIn.get(DOWNDRAFT)) {
            worldIn.addOptionalParticle(ParticleTypes.CURRENT_DOWN, d0 + 0.5D, d1 + 0.8D, d2, 0.0D, 0.0D, 0.0D);
            if (rand.nextInt(200) == 0) {
                worldIn.playSound(d0, d1, d2, SoundEvents.BLOCK_BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, SoundCategory.BLOCKS, 0.2F + rand.nextFloat() * 0.2F, 0.9F + rand.nextFloat() * 0.15F, false);
            }
        } else {
            worldIn.addParticle(ParticleTypes.POOF, d0 + 0.5D, d1, d2 + 0.5D, 0.0D, 0.06D, 0.0D);
            worldIn.addParticle(ParticleTypes.POOF, d0 + (double)rand.nextFloat(), d1 + (double)rand.nextFloat(), d2 + (double)rand.nextFloat(), 0.0D, 0.06D, 0.0D);
            if (rand.nextInt(200) == 0) {
                worldIn.playSound(d0, d1, d2, SoundEvents.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundCategory.BLOCKS, 0.2F + rand.nextFloat() * 0.2F, 0.9F + rand.nextFloat() * 0.15F, false);
            }
        }

    }

    /**
     * Update the provided state given the provided neighbor facing and neighbor state, returning a new state.
     * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
     * returns its solidified counterpart.
     * Note that this method should ideally consider only the specific face passed in.
     */
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (!stateIn.isValidPosition(worldIn, currentPos)) {
            worldIn.getPendingBlockTicks().scheduleTick(currentPos, this, 1);
        } else {
            if (facing == Direction.DOWN) {
                worldIn.setBlockState(currentPos, ModBlocks.UPDRAFT.get().getDefaultState().with(DOWNDRAFT, isDowndraft(worldIn, facingPos)), 2);
            } else if (facing == Direction.UP && !facingState.isIn(ModBlocks.UPDRAFT.get()) && canHoldUpdraft(worldIn, facingPos)) {
                worldIn.getPendingBlockTicks().scheduleTick(currentPos, this, 5);
            }
        }

        return stateIn;
    }

    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        BlockState blockstate = worldIn.getBlockState(pos.down());
        return blockstate.isIn(ModBlocks.UPDRAFT.get()) || blockstate.isIn(ModBlocks.UPDRAFT_SOURCE.get());
    }

    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return VoxelShapes.empty();
    }

    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(DOWNDRAFT);
    }
}

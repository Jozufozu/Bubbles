package jozufozu.bubbles.entity;

import jozufozu.bubbles.util.Collider;
import jozufozu.bubbles.util.CollisionUtil;
import net.minecraft.block.Block;
import net.minecraft.command.arguments.EntityAnchorArgument;
import net.minecraft.entity.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import jozufozu.bubbles.block.ISafeBlock;

import java.util.*;

public class BubbleEntity extends Entity {
    @SuppressWarnings("unchecked")
    public static final EntityType<BubbleEntity> BUBBLE = (EntityType<BubbleEntity>) EntityType.Builder.create(BubbleEntity::new, EntityClassification.MISC)
                                                                                                       .size(0.5f, 0.5f)
                                                                                                       .build("bubbles:bubble")
                                                                                                       .setRegistryName("bubbles:bubble");

    private final ArrayList<PushForce> forces = new ArrayList<>();

    public BubbleEntity(World worldIn) {
        this(BUBBLE, worldIn);
    }

    public BubbleEntity(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    public BubbleEntity(World worldIn, double x, double y, double z, Direction dir, double speed) {
        super(BUBBLE, worldIn);

        this.setPosition(x, y, z);
        this.setMotion(speed * dir.getXOffset(), speed * dir.getYOffset(), speed * dir.getZOffset());

        this.lookAt(EntityAnchorArgument.Type.FEET, this.getPositionVec().add(this.getMotion()));

        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
    }

    @Override
    public void tick() {
        super.tick();

        Vector3d motion = this.getMotion();

        double dx = motion.x;
        double dy = motion.y;
        double dz = motion.z;

        for (PushForce force : forces) {
            dx += force.x;
            dy += force.y;
            dz += force.z;

            force.time--;
        }

        forces.removeIf(PushForce::expired);

        // only fall when there is no horizontal motion
        if (Math.abs(dx) < 1e-5 && Math.abs(dz) <= 1e-5 && this.passengers.size() > 0) {
            dx = dz = 0.0;

            double downForce = 0.001;

            dy -= downForce;
        } else {
            dy = 0.0;
        }

        motion = new Vector3d(dx, dy, dz);
        this.setMotion(motion);

        this.moveAndCollide(motion);

        this.recalculateSize();
    }

    private void moveAndCollide(Vector3d motion) {
        AxisAlignedBB box = this.getBoundingBox();

        ArrayList<Collider.BlockCollider> blocks = new ArrayList<>();
        AxisAlignedBB checkZone = box.union(box.offset(motion));
        CollisionUtil.makeBlockColliderIterator(world, this, checkZone).forEachRemaining(blocks::add);

        // this bubble should pop if it ends up inside a block somehow
        if (!world.isRemote) {
            VoxelShape ourShape = VoxelShapes.create(box);

            for (Collider.BlockCollider block : blocks) {
                if (VoxelShapes.compare(block.collider, ourShape, IBooleanFunction.AND)) {
                    this.pop();
                    return;
                }
            }
        }

        ArrayList<Collider.EntityCollider> entities = new ArrayList<>();
        CollisionUtil.makeEntityColliderIterator(world, this, checkZone).forEachRemaining(entities::add);

        double minX = box.minX;
        double minY = box.minY;
        double minZ = box.minZ;
        double maxX = box.maxX;
        double maxY = box.maxY;
        double maxZ = box.maxZ;

        int steps = 20;
        double stepX = motion.x / ((double) steps);
        double stepY = motion.y / ((double) steps);
        double stepZ = motion.z / ((double) steps);

        boolean hasRider = this.passengers.size() > 0;

        main: for (int i = 0; i < steps; i++) {
            minX += stepX;
            minY += stepY;
            minZ += stepZ;
            maxX += stepX;
            maxY += stepY;
            maxZ += stepZ;

            ListIterator<Collider.BlockCollider> scanBlocks = blocks.listIterator();
            while (scanBlocks.hasNext()) {
                Collider.BlockCollider collider = scanBlocks.next();

                Block block = collider.state.getBlock();
                List<AxisAlignedBB> bbs = collider.collider.toBoundingBoxList();

                final boolean isSafe;
                if (block instanceof ISafeBlock) {
                    isSafe = ((ISafeBlock) block).isBlockSafe(collider.state, world, collider.pos);
                } else {
                    isSafe = false;
                }

                boolean collided = false;

                for (AxisAlignedBB bb : bbs) {
                    if (bb.intersects(minX, minY, minZ, maxX, maxY, maxZ)) {

                        // if this is touching something dangerous it should pop immediately, 
                        // but we don't trust the client to make the call
                        if (!isSafe) {
                            if (!world.isRemote) this.pop();
                            break main;
                        }

                        collided = true;

                        double nudgeX = getNudge(minX, maxX, stepX, bb.minX, bb.maxX) * 1.01;
                        double nudgeY = getNudge(minY, maxY, stepY, bb.minY, bb.maxY) * 1.01;
                        double nudgeZ = getNudge(minZ, maxZ, stepZ, bb.minZ, bb.maxZ) * 1.01;

                        if (Math.abs(nudgeX) > 1e-7) {
                            minX -= nudgeX;
                            maxX -= nudgeX;
                            stepX = 0;
                        }
                        if (Math.abs(nudgeY) > 1e-7) {
                            minY -= nudgeY;
                            maxY -= nudgeY;
                            stepY = 0;
                        }
                        if (Math.abs(nudgeZ) > 1e-7) {
                            minZ -= nudgeZ;
                            maxZ -= nudgeZ;
                            stepZ = 0;
                        }
                    }
                }

                if (collided) {
                    scanBlocks.remove();
                }
            }


            ListIterator<Collider.EntityCollider> scanEntities = entities.listIterator();

            while (scanEntities.hasNext()) {
                Collider.EntityCollider entityCollider = scanEntities.next();

                Entity entity = entityCollider.entity;

                if (entity instanceof BubbleEntity || entity.getLowestRidingEntity() instanceof BubbleEntity) {
                    scanEntities.remove();
                    continue;
                }

                if (!world.isRemote) {
                    AxisAlignedBB entityBoundingBox = entity.getBoundingBox();

                    if (!hasRider && entityBoundingBox.intersects(minX - 0.2, minY - 0.2, minZ - 0.2, maxX + 0.2, maxY + 0.2, maxZ + 0.2)) {
                        if (entity.startRiding(this)) {
                            Behaviors.doMountBehavior(this, entity);

                            hasRider = true;

                            scanEntities.remove();
                        }
                    }
                }
            }
        }

        this.setMotion(stepX * steps, stepY * steps, stepZ * steps);

        this.setPosition((minX + maxX) * 0.5, minY, (minZ + maxZ) * 0.5);
    }

    /**
     * Tells you how far you have to move along an axis to get outside of a collider.
     */
    private static double getNudge(double min, double max, double step, double colliderMin, double colliderMax) {
        if (step > 0.0 && max >= colliderMin) {
            double nudge = max - colliderMin;

            if (nudge < step) {
                return nudge;
            }
        } else if (step < 0.0 && min <= colliderMax) {
            double nudge = min - colliderMax;

            if (nudge > step) {
                return nudge;
            }
        }
        return 0.0;
    }

    public void addForce(PushForce force) {
        this.forces.add(force);
    }

    public void recalculateSize() {
        List<Entity> passengers = this.passengers;

        if (!passengers.isEmpty()) {
            AxisAlignedBB entityBoundingBox = passengers.get(0).getBoundingBox();

            double xSize = entityBoundingBox.getXSize();
            double ySize = entityBoundingBox.getYSize();
            double zSize = entityBoundingBox.getZSize();

            float size = (float) Math.max(xSize, Math.max(ySize, zSize));

            size = Math.max(size + 0.6f, size * 1.25f);

            this.size = new EntitySize(size, size, false);
        } else {
            this.size = BubbleEntity.BUBBLE.getSize();
        }

        double radius = (double) this.size.width / 2.0D;
        this.setBoundingBox(new AxisAlignedBB(this.getPosX() - radius, this.getPosY(), this.getPosZ() - radius, this.getPosX() + radius, this.getPosY() + (double)this.size.height, this.getPosZ() + radius));
    }

    @Override
    public EntitySize getSize(Pose poseIn) {
        return this.size;
    }

    @Override
    public double getMountedYOffset() {
        return 0.05;
    }

    @Override
    public void updatePassenger(Entity passenger) {
        if (this.isPassenger(passenger)) {
            double y = this.getPosY() + this.getMountedYOffset();
            passenger.setPosition(this.getPosX(), y, this.getPosZ());
        }

        Behaviors.doTickBehavior(this, passenger);
    }

    @Override
    protected boolean canBeRidden(Entity entityIn) {
        return true;
    }

    @Override
    public boolean canBeRiddenInWater(Entity rider) {
        return true;
    }

    public void pop() {
        this.remove();
        for (Entity passenger : this.getPassengers()) {
            passenger.dismount();
        }
    }

    @Override
    protected void removePassenger(Entity passenger) {
        if (this.isAlive()) {
            this.remove();
        }

        Behaviors.doDismountBehavior(this, passenger);
        super.removePassenger(passenger);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        this.pop();
        return true;
    }

    @Override
    public boolean canBeAttackedWithItem() {
        return true;
    }

    @Override
    public boolean hitByEntity(Entity entityIn) {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    protected void registerData() {
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {

    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {

    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    public static class PushForce {
        public int time;
        public double x;
        public double y;
        public double z;

        public PushForce(int time, Vector3d force) {
            this(time, force.x, force.y, force.z);
        }

        public PushForce(int time, double x, double y, double z) {
            this.time = time;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public boolean expired() {
            return time < 0;
        }
    }
}

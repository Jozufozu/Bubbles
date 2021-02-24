package jozufozu.bubbles.entity;

import jozufozu.bubbles.entity.behavior.Behaviors;
import jozufozu.bubbles.util.Collider;
import jozufozu.bubbles.util.CollisionUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.network.NetworkHooks;
import jozufozu.bubbles.block.ISafeBlock;

import javax.annotation.Nullable;
import java.util.*;

public class BubbleEntity extends Entity {
    @SuppressWarnings("unchecked")
    public static final EntityType<BubbleEntity> BUBBLE = (EntityType<BubbleEntity>) EntityType.Builder.create(BubbleEntity::new, EntityClassification.MISC)
                                                                                                       .size(0.5f, 0.5f)
                                                                                                       .setShouldReceiveVelocityUpdates(true)
                                                                                                       .build("bubbles:bubble")
                                                                                                       .setRegistryName("bubbles:bubble");

    public static final DataParameter<Float> EMPTY_SIZE = EntityDataManager.createKey(BubbleEntity.class, DataSerializers.FLOAT);

    private final ArrayList<PushForce> forces = new ArrayList<>();

    public BubbleEntity(World worldIn) {
        this(BUBBLE, worldIn);
    }

    public BubbleEntity(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    public BubbleEntity(World worldIn, double x, double y, double z, @Nullable PushForce spawnForce) {
        this(BUBBLE, worldIn);

        this.setPosition(x, y, z);
        if (spawnForce != null) this.forces.add(spawnForce);

        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
    }

    @Override
    protected void registerData() {
        this.dataManager.register(EMPTY_SIZE, BUBBLE.getWidth());
    }

    public boolean containsEntity() {
        return passengers.size() > 0;
    }

    @Nullable
    public Entity getContainedEntity() {
        return this.containsEntity() ? this.passengers.get(0) : null;
    }

    public float getEmptySize() {
        return this.dataManager.get(EMPTY_SIZE);
    }

    public void setEmptySize(float size) {
        this.dataManager.set(EMPTY_SIZE, size);
    }

    @Override
    public void tick() {
        super.tick();

        Vector3d motion = this.applyForces();

        this.moveAndCollide(motion);
        this.doBlockCollisions();

        if (this.getPosY() > world.getHeight()) this.pop();

        this.recalculateSize();
    }

    public void pop() {
        this.remove();
        for (Entity passenger : this.getPassengers()) {
            passenger.dismount();
        }
    }

    private Vector3d applyForces() {
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

        boolean noHorizontal = Math.abs(dx) + Math.abs(dz) < 1e-4;
        if (noHorizontal && dy > -0.1) {
            dy -= 0.005;
        } else if (dy > 0.03) {
            dy -= 0.01;
        } else if (!noHorizontal) {
            dy = 0;
        }

        return new Vector3d(dx, dy, dz);
    }

    protected void handleBubbleStandCollision(BubbleStandEntity stand) {
        Entity containedEntity = this.getContainedEntity();
        if (containedEntity == null) return;

        AxisAlignedBB attachmentBox = stand.getAttachmentBox();

        if (attachmentBox.intersects(containedEntity.getBoundingBox())) {
            // do interesting things
        }
    }


    protected boolean canCollideWithEntity(Entity entity) {
        return true;
    }

    protected void onCollideWithEntity(Entity entity) {
        if (entity.startRiding(this)) {
            Behaviors.doMountBehavior(this, entity);

            this.recalculateSize();
        }
    }


    public boolean canCombine(BubbleEntity other) {
        return false;
        //        Entity thisEntity = this.getContainedEntity();
        //        Entity otherEntity = other.getContainedEntity();
        //        if (thisEntity instanceof ItemEntity && otherEntity instanceof ItemEntity) {
        //            ItemStack thisItem = ((ItemEntity) thisEntity).getItem();
        //            ItemStack otherItem = ((ItemEntity) otherEntity).getItem();
        //
        //            if (ItemEntity.canMergeStacks(thisItem, otherItem)) {
        //                ItemStack copy = otherItem.copy();
        //                ItemStack merged = ItemEntity.mergeStacks(thisItem, copy, 64);
        //
        //                if (copy.isEmpty()) {
        //                    if (!world.isRemote) {
        //                        otherEntity.remove();
        //                        ((ItemEntity) thisEntity).setItem(merged);
        //                    }
        //
        //                    return true;
        //                }
        //            }
        //        }
        //
        //        return thisEntity == null && otherEntity == null;
    }

    public void combineAll(ArrayList<BubbleEntity> other) {

        //        Vector3d otherPos = other.getPositionVec();
        //        Vector3d otherMotion = other.getMotion();
        //
        //        BubbleEntity entity = BUBBLE.create(world);
        //
        //        if (entity == null) entity = this;
        //
        //        double thisSize = this.getEmptySize();
        //        double otherSize = other.getEmptySize();
        //
        //        double size = Math.cbrt(thisSize * thisSize * thisSize + otherSize * otherSize * otherSize);
        //
        //        entity.setEmptySize((float) size);
        //        entity.setMotion(motion.x + otherMotion.x, motion.y + otherMotion.y, motion.z + otherMotion.z);
        //        entity.setPosition((position.x + otherPos.x) * 0.5, (position.y + otherPos.y) * 0.5, (position.z + otherPos.z) * 0.5);
        //        entity.forces.addAll(other.forces);
        //
        //        other.remove();
        //
        //        if (entity != this) {
        //            this.remove();
        //            entity.forces.addAll(this.forces);
        //
        //            world.addEntity(entity);
        //        }
    }

    private void moveAndCollide(Vector3d motion) {
        AxisAlignedBB box = this.getBoundingBox();

        ArrayList<Collider.BlockCollider> blocks = new ArrayList<>();
        AxisAlignedBB checkZone = box.union(box.offset(motion)).grow(0.5);
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

        ArrayList<BubbleEntity> combineWith = new ArrayList<>();

        main: for (int i = 0; i < steps; i++) {
            minX += stepX;
            minY += stepY;
            minZ += stepZ;
            maxX += stepX;
            maxY += stepY;
            maxZ += stepZ;

            ListIterator<Collider.EntityCollider> scanEntities = entities.listIterator();

            while (scanEntities.hasNext()) {
                Collider.EntityCollider entityCollider = scanEntities.next();

                Entity entity = entityCollider.entity;

                if (entity.getRidingEntity() instanceof BubbleEntity) {
                    scanEntities.remove();
                    continue;
                }

                AxisAlignedBB entityBoundingBox = entity.getBoundingBox();

                if (entityBoundingBox.intersects(minX - 0.2, minY - 0.2, minZ - 0.2, maxX + 0.2, maxY + 0.2, maxZ + 0.2)) {
                    if (entity instanceof BubbleEntity) {
                        BubbleEntity other = (BubbleEntity) entity;
                        if (this.canCombine(other)) combineWith.add(other);
                    } else if (entity instanceof BubbleStandEntity) {
                        handleBubbleStandCollision((BubbleStandEntity) entity);
                    } else if (!world.isRemote && canCollideWithEntity(entity)) {
                        onCollideWithEntity(entity);
                    }

                    scanEntities.remove();
                }
            }

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

                        double nudgeX = getNudge(minX, maxX, stepX, bb.minX, bb.maxX) * 1.06;
                        double nudgeY = getNudge(minY, maxY, stepY, bb.minY, bb.maxY) * 1.06;
                        double nudgeZ = getNudge(minZ, maxZ, stepZ, bb.minZ, bb.maxZ) * 1.06;

                        if (!Double.isNaN(nudgeX)) {
                            minX -= nudgeX;
                            maxX -= nudgeX;
                            stepX = 0;
                        }
                        if (!Double.isNaN(nudgeY)) {
                            minY -= nudgeY;
                            maxY -= nudgeY;
                            stepY = 0;
                        }
                        if (!Double.isNaN(nudgeZ)) {
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
        }

        this.setMotion(stepX * steps, stepY * steps, stepZ * steps);

        this.setPosition((minX + maxX) * 0.5, minY, (minZ + maxZ) * 0.5);

        if (!combineWith.isEmpty()) this.combineAll(combineWith);
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
        return Double.NaN;
    }

    public void addForce(PushForce force) {
        if (this.ticksExisted > 0) this.forces.add(force);
    }

    public void recalculateSize() {
        List<Entity> passengers = this.passengers;

        float potentialSize = 0;

        if (!passengers.isEmpty()) {
            AxisAlignedBB entityBoundingBox = passengers.get(0).getBoundingBox();

            double xSize = entityBoundingBox.getXSize();
            double ySize = entityBoundingBox.getYSize();
            double zSize = entityBoundingBox.getZSize();

            double size = Math.max(xSize, Math.max(ySize, zSize));

            potentialSize = (float) Math.max(size + 0.6, size * 1.25);
        }

        float emptySize = dataManager == null ? BUBBLE.getWidth() : this.getEmptySize();
        potentialSize = Math.max(emptySize, potentialSize);

        this.size = new EntitySize(potentialSize, potentialSize, false);

        double radius = (double) this.size.width / 2.0D;
        this.setBoundingBox(new AxisAlignedBB(this.getPosX() - radius, this.getPosY(), this.getPosZ() - radius, this.getPosX() + radius, this.getPosY() + (double)this.size.height, this.getPosZ() + radius));
    }

    @Override
    public void setPosition(double x, double y, double z) {
        this.setRawPosition(x, y, z);
        this.recalculateSize();
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

    @Override
    protected void removePassenger(Entity passenger) {
        if (this.isAlive()) {
            this.remove();
        }

        Behaviors.doDismountBehavior(this, passenger);
        super.removePassenger(passenger);
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        if (EMPTY_SIZE.equals(key)) this.recalculateSize();
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        this.pop();
        return false;
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
    protected void readAdditional(CompoundNBT compound) {
        setEmptySize(compound.getFloat("emptySize"));

        this.forces.clear();
        ListNBT list = compound.getList("forces", Constants.NBT.TAG_COMPOUND);
        for (INBT force : list) this.forces.add(new PushForce((CompoundNBT) force));
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        compound.putFloat("emptySize", this.getEmptySize());

        ListNBT forces = new ListNBT();
        for (PushForce force : this.forces) forces.add(force.serializeNBT());
        compound.put("forces", forces);
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    public static class PushForce implements INBTSerializable<CompoundNBT> {
        public int time;
        public double x;
        public double y;
        public double z;

        public PushForce(CompoundNBT nbt) {
            this.deserializeNBT(nbt);
        }

        public PushForce(PushForce other) {
            this.time = other.time;
            this.x = other.x;
            this.y = other.y;
            this.z = other.z;
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

        public PushForce copy() {
            return new PushForce(this);
        }

        @Override
        public CompoundNBT serializeNBT() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt("time", time);
            nbt.putDouble("x", x);
            nbt.putDouble("y", y);
            nbt.putDouble("z", z);
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            time = nbt.getInt("time");
            x = nbt.getDouble("x");
            y = nbt.getDouble("y");
            z = nbt.getDouble("z");
        }
    }
}

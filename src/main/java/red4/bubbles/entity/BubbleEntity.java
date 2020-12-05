package red4.bubbles.entity;

import net.minecraft.command.arguments.EntityAnchorArgument;
import net.minecraft.entity.*;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

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
            dx += force.force.x;
            dy += force.force.y;
            dz += force.force.z;

            force.time--;
        }

        forces.removeIf(PushForce::expired);

        // only fall when there is no horizontal motion
        if (Math.abs(dx) < 1e-5 && Math.abs(dy) <= 1e-5) {
            dx = dz = 0.0;

            double volume = 0.0;

            for (Entity passenger : getPassengers()) {
                AxisAlignedBB passengerSize = passenger.getBoundingBox();

                volume += passengerSize.getXSize() * passengerSize.getYSize() * passengerSize.getZSize();
            }

            double downForce = 0.03 * volume;;

            dy -= downForce;
        } else {
            dy = 0.0;
        }

        motion = new Vector3d(dx, dy, dz);
        this.setMotion(motion);

        Vector3d pos = this.getPositionVec();
        Vector3d target = pos.add(motion);

        RayTraceResult raytraceresult = this.world.rayTraceBlocks(new RayTraceContext(pos, target, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this));

        boolean shouldPop = false;

        if (raytraceresult.getType() != RayTraceResult.Type.MISS) {
            target = raytraceresult.getHitVec();
            shouldPop = true;
        }

        this.setPosition(target.x, target.y, target.z);

        if (!world.isRemote) {
            for (Entity entity : world.getEntitiesWithinAABBExcludingEntity(this, this.getBoundingBox().grow(0.25))) {
                if (entity instanceof BubbleEntity || entity.getRidingEntity() instanceof BubbleEntity) continue;

                Vector3d entityPos = entity.getPositionVec();

                if (entity.startRiding(this)) {
                    Behaviors.doMountBehavior(this, entity);

                    this.setPosition(entityPos.x, this.getPosY(), entityPos.z);
                }
            }

            if (!world.hasNoCollisions(this) || shouldPop) {
                this.pop();
            }
        }
        this.recalculateSize();
    }

    public void addForce(PushForce force) {
        this.forces.add(force);
    }

    public void recalculateSize() {
        List<Entity> passengers = this.getPassengers();

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
        public final Vector3d force;

        public PushForce(int time, Vector3d force) {
            this.time = time;
            this.force = force;
        }

        public boolean expired() {
            return time < 0;
        }
    }
}

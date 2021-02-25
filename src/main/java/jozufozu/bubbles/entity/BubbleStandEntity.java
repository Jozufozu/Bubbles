package jozufozu.bubbles.entity;

import jozufozu.bubbles.Bubbles;
import jozufozu.bubbles.entity.behavior.StandAttachment;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.Optional;

public class BubbleStandEntity extends Entity {
    @SuppressWarnings("unchecked")
    public static final EntityType<BubbleStandEntity> BUBBLE_STAND = (EntityType<BubbleStandEntity>) EntityType.Builder.create(BubbleStandEntity::new, EntityClassification.MISC)
                                                                                                       .size(0.5f, 0.5f)
                                                                                                       .build("bubbles:bubble_stand")
                                                                                                       .setRegistryName("bubbles:bubble_stand");

    private static final DataParameter<Direction> ORIENTATION = EntityDataManager.createKey(BubbleStandEntity.class, DataSerializers.DIRECTION);
    private static final DataParameter<Float> LENGTH = EntityDataManager.createKey(BubbleStandEntity.class, DataSerializers.FLOAT);
    public static final float DEFAULT_LENGTH = 0.5f;
    private static final DataParameter<StandAttachment> ATTACHMENT = EntityDataManager.createKey(BubbleStandEntity.class, Serializers.STAND_ATTACHMENT);

    @Nullable
    private PlayerEntity altering;
    public float lastTickLength;

    public BubbleStandEntity(World worldIn) {
        this(BUBBLE_STAND, worldIn);
    }

    public BubbleStandEntity(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);

        this.recalculateSize();
    }

    public BubbleStandEntity(World world, double x, double y, double z, Direction orientation) {
        this(world);

        this.setOrientation(orientation);
        this.setPosition(x, y, z);
    }

    @Override
    protected void registerData() {
        this.dataManager.register(ORIENTATION, Direction.UP);
        this.dataManager.register(LENGTH, DEFAULT_LENGTH);
        this.dataManager.register(ATTACHMENT, StandAttachment.WAND);
    }

    public Direction getOrientation() {
        return this.dataManager.get(ORIENTATION);
    }

    public void setOrientation(Direction orientation) {
        this.dataManager.set(ORIENTATION, orientation);
    }

    public StandAttachment getAttachment() {
        return this.dataManager.get(ATTACHMENT);
    }

    public void setAttachment(StandAttachment attachment) {
        this.dataManager.set(ATTACHMENT, attachment);
    }

    public float getLength() {
        return this.dataManager.get(LENGTH);
    }

    public void setLength(float length) {
        this.dataManager.set(LENGTH, length);
    }

    @Override
    public ActionResultType processInitialInteract(PlayerEntity player, Hand hand) {
        if (player.isSneaking() && this.altering == null) {
            this.altering = player;

            return ActionResultType.SUCCESS;
        }

        if (this.altering == player) {
            this.altering = null;

            return ActionResultType.SUCCESS;
        }

        return ActionResultType.PASS;
    }

    @Override
    public ItemStack getPickedResult(RayTraceResult target) {
        return new ItemStack(Bubbles.BUBBLE_STAND.get());
    }

    @Override
    public void tick() {
        super.tick();

        this.lastTickLength = this.getLength();

        if (altering != null) {
            if (altering.isSneaking())
                adjustLength(altering);
            else
                altering = null;
        } else if (getLength() > 2.5) {
            setLength(2.5f);
        }

        if (!world.isRemote && world.isAirBlock(this.getPosition().offset(this.getOrientation().getOpposite()))) {
            this.remove();
        }
    }

    protected void adjustLength(PlayerEntity altering) {
        Direction orientation = this.getOrientation();
        int xMul = 1 - Math.abs(orientation.getXOffset());
        int yMul = 1 - Math.abs(orientation.getYOffset());
        int zMul = 1 - Math.abs(orientation.getZOffset());

        Vector3d eyePos = altering.getEyePosition(1f);
        Vector3d thisPos = this.getPositionVec();

        Vector3d planeNormal = eyePos.subtract(thisPos)
                                     .mul(xMul, yMul, zMul)
                                     .normalize();

        Vector3d look = altering.getLookVec();

        rayPlaneIntersection(eyePos, look, thisPos, planeNormal)
                .ifPresent(this::setLengthFromPlaneIntersection);
    }

    private void setLengthFromPlaneIntersection(Vector3d intersection) {
        Direction orientation = this.getOrientation();

        int xOffset = orientation.getXOffset();
        int yOffset = orientation.getYOffset();
        int zOffset = orientation.getZOffset();

        Vector3d to = intersection.subtract(this.getPositionVec());

        double length = to.x * xOffset + to.y * yOffset + to.z * zOffset;

        if (length > 2.5) {
            if (length < 2.75) length = 2.5;
            else length = 2.5 + Math.log10(length - 1.5);
        } else {
            length = Math.round(length / 0.5) * 0.5;
        }

        length = Math.max(length, 0.5);

        length = Math.min(length, 3.5);

        this.setLength((float) length);
    }

    /**
     * Calculates the position of intersection for a ray and a plane.
     * @param rayPos The origin of the ray.
     * @param rayDir The direction of the ray.
     * @param planePos A point contained within the plane.
     * @param planeNorm The normal of the plane.
     * @return The position where the given ray intersects the given plane, if any.
     */
    public static Optional<Vector3d> rayPlaneIntersection(Vector3d rayPos, Vector3d rayDir, Vector3d planePos, Vector3d planeNorm) {
        double denom = rayDir.dotProduct(planeNorm);

        if (Math.abs(denom) > 1e-6) {
            Vector3d diff = planePos.subtract(rayPos);
            double t = diff.dotProduct(planeNorm) / denom;

            return Optional.of(rayPos.add(rayDir.x * t, rayDir.y * t, rayDir.z * t));
        }

        return Optional.empty();
    }

    @Override
    public void recalculateSize() {
        if (this.dataManager == null) return; // this gets called once before data is registered

        Direction orientation = this.getOrientation();
        Vector3d pos = this.getPositionVec();

        this.setBoundingBox(calculateBoundingBox(orientation, pos, this.getLength()));
    }

    public AxisAlignedBB getAttachmentBox() {
        double halfAttachmentSize = 3d / 16d + 0.25; // expanded a bit to leave room for error

        Vector3d pos = this.getAttachmentPosition();

        return new AxisAlignedBB(pos.x - halfAttachmentSize,
                                 pos.y - halfAttachmentSize,
                                 pos.z - halfAttachmentSize,
                                 pos.x + halfAttachmentSize,
                                 pos.y + halfAttachmentSize,
                                 pos.z + halfAttachmentSize);
    }

    public Vector3d getAttachmentPosition() {
        Direction orientation = this.getOrientation();
        int x = orientation.getXOffset();
        int y = orientation.getYOffset();
        int z = orientation.getZOffset();

        double length = this.getLength();

        return this.getPositionVec().add(x * length, y * length, z * length);
    }

    public static AxisAlignedBB calculateBoundingBox(Direction orientation, Vector3d pos, float length) {
        int xOffset = orientation.getXOffset();
        int yOffset = orientation.getYOffset();
        int zOffset = orientation.getZOffset();

        int xGrow = 1 - Math.abs(xOffset);
        int yGrow = 1 - Math.abs(yOffset);
        int zGrow = 1 - Math.abs(zOffset);

        double radius = 3d / 16d;
        double size = 3d / 16d + length;

        return new AxisAlignedBB(pos.x - radius * xGrow,
                                 pos.y - radius * yGrow,
                                 pos.z - radius * zGrow,
                                 pos.x + radius * xGrow + size * xOffset,
                                 pos.y + radius * yGrow + size * yOffset,
                                 pos.z + radius * zGrow + size * zOffset);
    }

    @Override
    public void setPosition(double x, double y, double z) {
        this.setRawPosition(x, y, z);
        this.recalculateSize();
    }

    public void notifyDataManagerChange(DataParameter<?> key) {
        if (ORIENTATION.equals(key) || LENGTH.equals(key)) {
            this.recalculateSize();
        }
    }

    @Override
    protected float getEyeHeight(Pose poseIn, EntitySize sizeIn) {
        return 0;
    }

    @Override
    protected AxisAlignedBB getBoundingBox(Pose pose) {
        return this.getBoundingBox();
    }

    public void blowFrom(BlockPos pos, double dx, double dz, BubbleEntity.PushForce force) {
        Vector3d lookVec = this.getLookVec();

        double lookX = lookVec.x;
        double lookZ = lookVec.z;

        if (Math.abs(lookX * dx + lookZ * dz) > 1e-3) {
            double radius = 0.25;

            Vector3d spawnPos = this.getAttachmentPosition();
            BubbleEntity entity = new BubbleEntity(world, spawnPos.x + dx * radius, spawnPos.y - radius, spawnPos.z + dz * radius, force);

            world.addEntity(entity);
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        this.remove();
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
        Direction orientation = Direction.byName(compound.getString("orientation"));
        if (orientation != null) this.setOrientation(orientation);

        this.setLength(compound.getFloat("length"));
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        compound.putString("orientation", this.getOrientation().getName2());
        compound.putFloat("length", this.getLength());
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}

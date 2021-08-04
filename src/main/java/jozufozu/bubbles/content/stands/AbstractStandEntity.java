package jozufozu.bubbles.content.stands;

import jozufozu.bubbles.content.core.BubbleEntity;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
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
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.Optional;

public abstract class AbstractStandEntity extends Entity {

    private static final DataParameter<Direction> ORIENTATION = EntityDataManager.defineId(AbstractStandEntity.class, DataSerializers.DIRECTION);
    private static final DataParameter<Float> LENGTH = EntityDataManager.defineId(AbstractStandEntity.class, DataSerializers.FLOAT);
    public static final float DEFAULT_LENGTH = 0.5f;

    @Nullable
    private PlayerEntity altering;
    public float lastTickLength;


    public AbstractStandEntity(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);

        this.refreshDimensions();
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(ORIENTATION, Direction.UP);
        this.entityData.define(LENGTH, DEFAULT_LENGTH);
    }

    public Direction getOrientation() {
        return this.entityData.get(ORIENTATION);
    }

    public void setOrientation(Direction orientation) {
        this.entityData.set(ORIENTATION, orientation);
    }

    public float getLength() {
        return this.entityData.get(LENGTH);
    }

    public void setLength(float length) {
        this.entityData.set(LENGTH, length);
    }

    @Override
    public ActionResultType interact(PlayerEntity player, Hand hand) {
        if (player.isShiftKeyDown() && this.altering == null) {
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
    public void tick() {
        super.tick();

        this.lastTickLength = this.getLength();

        if (altering != null) {
            if (altering.isShiftKeyDown())
                adjustLength(altering);
            else
                altering = null;
        } else if (getLength() > 2.5) {
            setLength(2.5f);
        }

        if (!level.isClientSide && level.isEmptyBlock(this.blockPosition().relative(this.getOrientation().getOpposite()))) {
            this.remove();
        }
    }

    protected void adjustLength(PlayerEntity altering) {
        Direction orientation = this.getOrientation();
        int xMul = 1 - Math.abs(orientation.getStepX());
        int yMul = 1 - Math.abs(orientation.getStepY());
        int zMul = 1 - Math.abs(orientation.getStepZ());

        Vector3d eyePos = altering.getEyePosition(1f);
        Vector3d thisPos = this.position();

        Vector3d planeNormal = eyePos.subtract(thisPos)
                                     .multiply(xMul, yMul, zMul)
                                     .normalize();

        Vector3d look = altering.getLookAngle();

        rayPlaneIntersection(eyePos, look, thisPos, planeNormal)
                .ifPresent(this::setLengthFromPlaneIntersection);
    }

    private void setLengthFromPlaneIntersection(Vector3d intersection) {
        Direction orientation = this.getOrientation();

        int xOffset = orientation.getStepX();
        int yOffset = orientation.getStepY();
        int zOffset = orientation.getStepZ();

        Vector3d to = intersection.subtract(this.position());

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
        double denom = rayDir.dot(planeNorm);

        if (Math.abs(denom) > 1e-6) {
            Vector3d diff = planePos.subtract(rayPos);
            double t = diff.dot(planeNorm) / denom;

            return Optional.of(rayPos.add(rayDir.x * t, rayDir.y * t, rayDir.z * t));
        }

        return Optional.empty();
    }

    @Override
    public void refreshDimensions() {
        if (this.entityData == null) return; // this gets called once before data is registered

        Direction orientation = this.getOrientation();
        Vector3d pos = this.position();

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
        int x = orientation.getStepX();
        int y = orientation.getStepY();
        int z = orientation.getStepZ();

        double length = this.getLength();

        return this.position().add(x * length, y * length, z * length);
    }

    public static AxisAlignedBB calculateBoundingBox(Direction orientation, Vector3d pos) {
        return calculateBoundingBox(orientation, pos, DEFAULT_LENGTH);
    }

    public static AxisAlignedBB calculateBoundingBox(Direction orientation, Vector3d pos, float length) {
        int xOffset = orientation.getStepX();
        int yOffset = orientation.getStepY();
        int zOffset = orientation.getStepZ();

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
    public void setPos(double x, double y, double z) {
        this.setPosRaw(x, y, z);
        this.refreshDimensions();
    }

    public void onSyncedDataUpdated(DataParameter<?> key) {
        if (ORIENTATION.equals(key) || LENGTH.equals(key)) {
            this.refreshDimensions();
        }
    }

    @Override
    protected float getEyeHeight(Pose poseIn, EntitySize sizeIn) {
        return 0;
    }

    @Override
    protected AxisAlignedBB getBoundingBoxForPose(Pose pose) {
        return this.getBoundingBox();
    }

    public void blowFrom(BlockPos pos, double dx, double dz, BubbleEntity.PushForce force) {

    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        this.remove();
        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT compound) {
        Direction orientation = Direction.byName(compound.getString("orientation"));
        if (orientation != null) this.setOrientation(orientation);

        this.setLength(compound.getFloat("length"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT compound) {
        compound.putString("orientation", this.getOrientation().getName());
        compound.putFloat("length", this.getLength());
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}

package jozufozu.bubbles.entity;

import jozufozu.bubbles.Bubbles;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.EndermanEntity;
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
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.Vector;

public class BubbleStandEntity extends Entity {
    @SuppressWarnings("unchecked")
    public static final EntityType<BubbleStandEntity> BUBBLE_STAND = (EntityType<BubbleStandEntity>) EntityType.Builder.create(BubbleStandEntity::new, EntityClassification.MISC)
                                                                                                       .size(0.5f, 0.5f)
                                                                                                       .build("bubbles:bubble_stand")
                                                                                                       .setRegistryName("bubbles:bubble_stand");

    private static final DataParameter<Direction> ORIENTATION = EntityDataManager.createKey(BubbleStandEntity.class, DataSerializers.DIRECTION);
    private static final DataParameter<Float> LENGTH = EntityDataManager.createKey(BubbleStandEntity.class, DataSerializers.FLOAT);
    public static final float DEFAULT_LENGTH = 0.5f;
    //private static final DataParameter<Integer> ATTACHMENT = EntityDataManager.createKey(BubbleStandEntity.class, DataSerializers.OPTIONAL_UNIQUE_ID);

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
    }

    public Direction getOrientation() {
        return this.dataManager.get(ORIENTATION);
    }

    public void setOrientation(Direction orientation) {
        this.dataManager.set(ORIENTATION, orientation);
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
    public void tick() {
        super.tick();

        if (altering != null) {
            adjustLength(altering);
        } else if (getLength() > 2.5) {
            setLength(2.5f);
        }

        if (!world.isRemote && world.isAirBlock(this.getPosition().offset(this.getOrientation().getOpposite()))) {
            this.remove();
        }

        this.lastTickLength = this.getLength();
    }

    protected void adjustLength(PlayerEntity altering) {
        Direction orientation = this.getOrientation();
        int xOffset = Math.abs(orientation.getXOffset());
        int yOffset = Math.abs(orientation.getYOffset());
        int zOffset = Math.abs(orientation.getZOffset());

        // the normals for the 2 planes that contain the orientation vector
        Vector3d plane1 = new Vector3d(zOffset, xOffset, yOffset);
        Vector3d plane2 = new Vector3d(yOffset, zOffset, xOffset);

        Vector3d look = altering.getLookVec();
        Vector3d eyePosition = altering.getEyePosition(1f);

        Vector3d inter1 = rayPlaneIntersection(eyePosition, look, this.getPositionVec(), plane1);
        Vector3d inter2 = rayPlaneIntersection(eyePosition, look, this.getPositionVec(), plane2);

        if (inter1 != null && inter2 != null) {
            double len1 = inter1.subtract(eyePosition).lengthSquared();
            double len2 = inter2.subtract(eyePosition).lengthSquared();

            if (len1 > len2) {
                this.setLengthFromPlaneIntersection(inter1);
            } else {
                this.setLengthFromPlaneIntersection(inter2);
            }
        } else if (inter1 != null) {
            this.setLengthFromPlaneIntersection(inter1);
        } else if (inter2 != null) {
            this.setLengthFromPlaneIntersection(inter2);
        }
    }

    private void setLengthFromPlaneIntersection(Vector3d intersection) {
        Direction orientation = this.getOrientation();

        int xOffset = orientation.getXOffset();
        int yOffset = orientation.getYOffset();
        int zOffset = orientation.getZOffset();

        Vector3d to = intersection.subtract(this.getPositionVec());

        double length = to.x * xOffset + to.y * yOffset + to.z * zOffset;

        length = Math.round(length / 0.5) * 0.5;

        length = Math.max(length, 0.5);

        if (length > 2.5) {
            length = 2.5 + Math.log10(length - 1.5);
        }

        length = Math.min(length, 3.5);

        this.setLength((float) length);
    }

    @Nullable
    public static Vector3d rayPlaneIntersection(Vector3d rayPos, Vector3d rayDir, Vector3d planePos, Vector3d planeNorm) {
        double denom = rayDir.dotProduct(planeNorm);

        if (Math.abs(denom) > 1e-6) {
            Vector3d diff = planePos.subtract(rayPos);
            double t = diff.dotProduct(planeNorm) / denom;

            return rayPos.add(rayDir.x * t, rayDir.y * t, rayDir.z * t);
        }

        return null;
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

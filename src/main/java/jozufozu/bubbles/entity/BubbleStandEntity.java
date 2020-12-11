package jozufozu.bubbles.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class BubbleStandEntity extends Entity {
    @SuppressWarnings("unchecked")
    public static final EntityType<BubbleStandEntity> BUBBLE_STAND = (EntityType<BubbleStandEntity>) EntityType.Builder.create(BubbleStandEntity::new, EntityClassification.MISC)
                                                                                                       .size(0.75f, 0.75f)
                                                                                                       .build("bubbles:bubble_stand")
                                                                                                       .setRegistryName("bubbles:bubble_stand");

    private static final DataParameter<Direction> MOUNT_SIDE = EntityDataManager.createKey(BubbleStandEntity.class, DataSerializers.DIRECTION);
    private static final DataParameter<Integer> LENGTH = EntityDataManager.createKey(BubbleStandEntity.class, DataSerializers.VARINT);
    //private static final DataParameter<Integer> ATTACHMENT = EntityDataManager.createKey(BubbleStandEntity.class, DataSerializers.OPTIONAL_UNIQUE_ID);


    public BubbleStandEntity(World worldIn) {
        super(BUBBLE_STAND, worldIn);
    }

    public BubbleStandEntity(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Override
    protected void registerData() {
        this.dataManager.register(MOUNT_SIDE, Direction.DOWN);
        this.dataManager.register(LENGTH, 0);
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

    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {

    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}

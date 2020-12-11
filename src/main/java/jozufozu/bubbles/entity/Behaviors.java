package jozufozu.bubbles.entity;

import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;

import java.util.HashMap;
import java.util.function.BiConsumer;

public class Behaviors {

    public static final HashMap<Class<? extends Entity>, BiConsumer<BubbleEntity, Entity>> MOUNT_BEHAVIORS = new HashMap<>();
    public static final HashMap<Class<? extends Entity>, BiConsumer<BubbleEntity, Entity>> DISMOUNT_BEHAVIORS = new HashMap<>();
    public static final HashMap<Class<? extends Entity>, BiConsumer<BubbleEntity, Entity>> TICK_BEHAVIORS = new HashMap<>();

    public static void initBehaviors() {

        // Items don't despawn in bubbles
        MOUNT_BEHAVIORS.put(ItemEntity.class, (bubbleEntity, entity) -> {
            ((ItemEntity) entity).setInfinitePickupDelay();
            ((ItemEntity) entity).setNoDespawn();
        });
        DISMOUNT_BEHAVIORS.put(ItemEntity.class, (bubbleEntity, entity) -> ((ItemEntity) entity).setDefaultPickupDelay());

        // Area effect clouds don't shrink
        TICK_BEHAVIORS.put(AreaEffectCloudEntity.class, (bubbleEntity, entity) -> {
            AreaEffectCloudEntity cloud = (AreaEffectCloudEntity) entity;
            float radius = cloud.getRadius();
            cloud.setRadius(radius - cloud.radiusPerTick);
        });
    }

    public static void doMountBehavior(BubbleEntity bubble, Entity entity) {
        doBehavior(bubble, entity, MOUNT_BEHAVIORS);
    }

    public static void doDismountBehavior(BubbleEntity bubble, Entity entity) {
        doBehavior(bubble, entity, DISMOUNT_BEHAVIORS);
    }

    public static void doTickBehavior(BubbleEntity bubble, Entity entity) {
        doBehavior(bubble, entity, TICK_BEHAVIORS);
    }

    private static void doBehavior(BubbleEntity bubble, Entity entity, HashMap<Class<? extends Entity>, BiConsumer<BubbleEntity, Entity>> behaviors) {
        Class<? extends Entity> passengerClass = entity.getClass();

        BiConsumer<BubbleEntity, Entity> behavior = behaviors.get(passengerClass);

        if (behavior != null) {
            behavior.accept(bubble, entity);
        }
    }
}

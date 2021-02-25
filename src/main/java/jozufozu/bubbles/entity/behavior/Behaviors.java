package jozufozu.bubbles.entity.behavior;

import jozufozu.bubbles.entity.BubbleEntity;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.TNTEntity;

import java.util.HashMap;

public class Behaviors {

    private static final HashMap<Class<? extends Entity>, BubbleBehavior<? extends Entity>> MOUNT_BEHAVIORS = new HashMap<>();
    private static final HashMap<Class<? extends Entity>, BubbleBehavior<? extends Entity>> DISMOUNT_BEHAVIORS = new HashMap<>();
    private static final HashMap<Class<? extends Entity>, BubbleBehavior<? extends Entity>> TICK_BEHAVIORS = new HashMap<>();

    public static void initBehaviors() {

        // You can't pick up items in bubbles
        addBehavior(MOUNT_BEHAVIORS, ItemEntity.class, (bubbleEntity, entity) -> entity.setInfinitePickupDelay());
        addBehavior(DISMOUNT_BEHAVIORS, ItemEntity.class, (bubbleEntity, entity) -> entity.setDefaultPickupDelay());

        // Area effect clouds don't shrink
        addBehavior(TICK_BEHAVIORS, AreaEffectCloudEntity.class, (bubbleEntity, entity) -> {
            float radius = entity.getRadius();
            entity.setRadius(radius - entity.radiusPerTick);
        });

        addBehavior(TICK_BEHAVIORS, TNTEntity.class, (bubbleEntity, entity) -> {
            if (entity.getFuse() == 1) {

            }
        });
    }

    public static <T extends Entity> void addBehavior(HashMap<Class<? extends Entity>, BubbleBehavior<? extends Entity>> type, Class<T> entityClass, BubbleBehavior<T> behavior) {
        type.put(entityClass, behavior);
    }

    public static <T extends Entity> void doMountBehavior(BubbleEntity bubble, T entity) {
        doBehavior(bubble, entity, MOUNT_BEHAVIORS);
    }

    public static <T extends Entity> void doDismountBehavior(BubbleEntity bubble, T entity) {
        doBehavior(bubble, entity, DISMOUNT_BEHAVIORS);
    }

    public static <T extends Entity> void doTickBehavior(BubbleEntity bubble, T entity) {
        doBehavior(bubble, entity, TICK_BEHAVIORS);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Entity> void doBehavior(BubbleEntity bubble, T entity, HashMap<Class<? extends Entity>, BubbleBehavior<? extends Entity>> behaviors) {
        try {
            Class<T> passengerClass = (Class<T>) entity.getClass();

            BubbleBehavior<T> behavior = (BubbleBehavior<T>) behaviors.get(passengerClass);

            if (behavior != null) {
                behavior.doBehavior(bubble, entity);
            }
        } catch (ClassCastException e) {
            throw new IllegalStateException("Entity class does not match ");
        }
    }
}

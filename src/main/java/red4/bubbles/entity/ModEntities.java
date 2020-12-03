package red4.bubbles.entity;

import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.BiConsumer;

public class ModEntities {

    public static final HashMap<Class<? extends Entity>, BiConsumer<BubbleEntity, Entity>> TICK_BEHAVIORS = new HashMap<>();

    public static void initBehaviors() {
        TICK_BEHAVIORS.put(AreaEffectCloudEntity.class, (bubbleEntity, entity) -> {
            AreaEffectCloudEntity cloud = (AreaEffectCloudEntity) entity;
            float radius = cloud.getRadius();
            cloud.setRadius(radius - cloud.radiusPerTick);
        });
    }
}

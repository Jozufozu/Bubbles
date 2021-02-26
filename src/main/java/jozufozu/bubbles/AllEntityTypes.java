package jozufozu.bubbles;

import jozufozu.bubbles.content.core.BubbleEntity;
import jozufozu.bubbles.content.stands.CraftingStandEntity;
import jozufozu.bubbles.content.stands.WandStandEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.event.RegistryEvent;

public class AllEntityTypes {
    @SuppressWarnings("unchecked")
    public static final EntityType<WandStandEntity> BUBBLE_STAND = (EntityType<WandStandEntity>) EntityType.Builder.create(WandStandEntity::new, EntityClassification.MISC)
                                                                                                                   .size(0.5f, 0.5f)
                                                                                                                   .build("bubbles:bubble_stand")
                                                                                                                   .setRegistryName("bubbles:bubble_stand");

    @SuppressWarnings("unchecked")
    public static final EntityType<CraftingStandEntity> CRAFTING_STAND = (EntityType<CraftingStandEntity>) EntityType.Builder.create(CraftingStandEntity::new, EntityClassification.MISC)
                                                                                                                             .size(0.5f, 0.5f)
                                                                                                                             .build("bubbles:crafting_stand")
                                                                                                                             .setRegistryName("bubbles:crafting_stand");
    @SuppressWarnings("unchecked")
    public static final EntityType<BubbleEntity> BUBBLE = (EntityType<BubbleEntity>) EntityType.Builder.create(BubbleEntity::new, EntityClassification.MISC)
                                                                                                       .size(0.5f, 0.5f)
                                                                                                       .setShouldReceiveVelocityUpdates(true)
                                                                                                       .build("bubbles:bubble")
                                                                                                       .setRegistryName("bubbles:bubble");

    public static void registerEntities(final RegistryEvent.Register<EntityType<?>> event) {
        event.getRegistry().register(BUBBLE);
        event.getRegistry().register(BUBBLE_STAND);
        event.getRegistry().register(CRAFTING_STAND);
    }
}

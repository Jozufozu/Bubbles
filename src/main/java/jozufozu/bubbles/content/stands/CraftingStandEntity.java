package jozufozu.bubbles.content.stands;

import jozufozu.bubbles.AllEntityTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public class CraftingStandEntity extends AbstractStandEntity {

    public CraftingStandEntity(World world) {
        super(AllEntityTypes.CRAFTING_STAND, world);
    }

    public CraftingStandEntity(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }
}

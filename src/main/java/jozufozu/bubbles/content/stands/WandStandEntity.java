package jozufozu.bubbles.content.stands;

import jozufozu.bubbles.AllEntityTypes;
import jozufozu.bubbles.AllItems;
import jozufozu.bubbles.content.core.BubbleEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class WandStandEntity extends AbstractStandEntity {

    public WandStandEntity(World world) {
        super(AllEntityTypes.BUBBLE_STAND, world);
    }

    public WandStandEntity(EntityType<Entity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public ItemStack getPickedResult(RayTraceResult target) {
        return new ItemStack(AllItems.WAND_STAND.get());
    }

    @Override
    public void blowFrom(BlockPos pos, double dx, double dz, BubbleEntity.PushForce force) {
        Vector3d lookVec = this.getLookAngle();

        double lookX = lookVec.x;
        double lookZ = lookVec.z;

        if (Math.abs(lookX * dx + lookZ * dz) > 1e-3) {
            double radius = 0.25;

            Vector3d spawnPos = this.getAttachmentPosition();
            BubbleEntity entity = new BubbleEntity(level, spawnPos.x + dx * radius, spawnPos.y - radius, spawnPos.z + dz * radius, force);

            level.addFreshEntity(entity);
        }
    }
}

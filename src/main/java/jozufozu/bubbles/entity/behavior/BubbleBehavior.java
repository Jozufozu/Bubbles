package jozufozu.bubbles.entity.behavior;

import jozufozu.bubbles.entity.BubbleEntity;
import net.minecraft.entity.Entity;

@FunctionalInterface
public interface BubbleBehavior<T extends Entity> {
    void doBehavior(BubbleEntity bubble, T entity);
}

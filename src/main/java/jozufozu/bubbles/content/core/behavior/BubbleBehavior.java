package jozufozu.bubbles.content.core.behavior;

import jozufozu.bubbles.content.core.BubbleEntity;
import net.minecraft.entity.Entity;

@FunctionalInterface
public interface BubbleBehavior<T extends Entity> {
    void doBehavior(BubbleEntity bubble, T entity);
}

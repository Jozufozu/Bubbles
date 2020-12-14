package jozufozu.bubbles.entity;

import java.util.function.Function;

public enum BubbleState {
    EMPTY(View::new),
    HOLDING_ENTITY(View::new),
    HOLDING_EXPLOSION(View::new);

    private final Function<BubbleEntity, View> viewer;

    BubbleState(Function<BubbleEntity, View> viewer) {
        this.viewer = viewer;
    }

    public View getView(BubbleEntity bubble) {
        return viewer.apply(bubble);
    }

    public static class View {
        public final BubbleEntity bubble;

        public View(BubbleEntity bubble) {
            this.bubble = bubble;
        }

        public float getEmptySize() {
            return bubble.getEmptySize();
        }
    }
}

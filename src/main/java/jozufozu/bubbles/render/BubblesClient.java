package jozufozu.bubbles.render;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.material.MaterialSpec;
import com.jozufozu.flywheel.core.Formats;
import com.jozufozu.flywheel.event.GatherContextEvent;

import jozufozu.bubbles.Bubbles;
import net.minecraft.util.ResourceLocation;

public class BubblesClient {
    public static final ResourceLocation BUBBLE = new ResourceLocation(Bubbles.MODID, "bubble");
    public static final VertexFormat BUBBLE_INSTANCE = VertexFormat.builder().build();

    public static final MaterialSpec<BubbleData> BUBBLE_MATERIAL = new MaterialSpec<>(BUBBLE, BUBBLE, Formats.UNLIT_MODEL, BUBBLE_INSTANCE, BubbleData::new);

    public static void flwInit(GatherContextEvent event) {
        event.getBackend().register(BUBBLE_MATERIAL);
    }
}

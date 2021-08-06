package jozufozu.bubbles.render;

import com.jozufozu.flywheel.backend.gl.buffer.MappedBuffer;
import com.jozufozu.flywheel.backend.instancing.InstanceData;
import com.jozufozu.flywheel.backend.instancing.Instancer;

public class BubbleData extends InstanceData {
    public BubbleData(Instancer<?> owner) {
        super(owner);
    }

    @Override
    public void write(MappedBuffer buf) {

    }
}

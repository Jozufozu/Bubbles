package jozufozu.bubbles.entity;

import jozufozu.bubbles.Bubbles;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.IDataSerializer;
import jozufozu.bubbles.entity.BubbleState;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.registries.DataSerializerEntry;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class Serializers {
    public static final DeferredRegister<DataSerializerEntry> SERIALIZERS = DeferredRegister.create(ForgeRegistries.DATA_SERIALIZERS, Bubbles.MODID);

    public static final IDataSerializer<BubbleState> BUBBLE_STATE = new IDataSerializer<BubbleState>() {
        public void write(PacketBuffer buf, BubbleState value) {
            buf.writeEnumValue(value);
        }

        public BubbleState read(PacketBuffer buf) {
            return buf.readEnumValue(BubbleState.class);
        }

        public BubbleState copyValue(BubbleState value) {
            return value;
        }
    };

    static {
        SERIALIZERS.register("bubble_state", () -> new DataSerializerEntry(BUBBLE_STATE));
    }
}

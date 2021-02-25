package jozufozu.bubbles.entity;

import jozufozu.bubbles.Bubbles;
import jozufozu.bubbles.entity.behavior.StandAttachment;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraftforge.registries.DataSerializerEntry;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class Serializers {
    public static final DeferredRegister<DataSerializerEntry> SERIALIZERS = DeferredRegister.create(ForgeRegistries.DATA_SERIALIZERS, Bubbles.MODID);

    public static final IDataSerializer<StandAttachment> STAND_ATTACHMENT = new IDataSerializer<StandAttachment>() {
        public void write(PacketBuffer buf, StandAttachment value) {
            buf.writeEnumValue(value);
        }

        public StandAttachment read(PacketBuffer buf) {
            return buf.readEnumValue(StandAttachment.class);
        }

        public StandAttachment copyValue(StandAttachment value) {
            return value;
        }
    };

    static {
        SERIALIZERS.register("attachment", () -> new DataSerializerEntry(STAND_ATTACHMENT));
    }
}

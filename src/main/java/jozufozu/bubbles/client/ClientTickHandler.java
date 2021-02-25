package jozufozu.bubbles.client;


import jozufozu.bubbles.Bubbles;
import jozufozu.bubbles.block.BellowsBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Bubbles.MODID)
public final class ClientTickHandler {

    private ClientTickHandler() {}

    public static int ticksInGame = 0;
    public static float partialTicks = 0;
    public static float delta = 0;
    public static float total = 0;

    private static void calcDelta() {
        float oldTotal = total;
        total = ticksInGame + partialTicks;
        delta = total - oldTotal;
    }

    @SubscribeEvent
    public static void bellowsHighlight(DrawHighlightEvent.HighlightBlock event) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        ClientWorld world = Minecraft.getInstance().world;

        if (player == null || world == null) return;

        if (!player.isSneaking()) {
            return;
        }

        BlockRayTraceResult target = event.getTarget();

        BlockPos pos = target.getPos();
        BlockState state = world.getBlockState(pos);

        Block block = state.getBlock();
        if (block instanceof BellowsBlock) {
            Vector3d view = event.getInfo().getProjectedView();
            AxisAlignedBB pushZone = BellowsBlock.getPushZone(state, pos).offset(-view.x, -view.y, -view.z);

            WorldRenderer.drawBoundingBox(event.getMatrix(), event.getBuffers().getBuffer(RenderType.getLines()), pushZone, 1f, 1f, 1f, 0.5f);
        }
    }

    @SubscribeEvent
    public static void renderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            partialTicks = event.renderTickTime;
        } else {
            calcDelta();
        }
    }

    @SubscribeEvent
    public static void clientTickEnd(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {

            if (!Minecraft.getInstance().isGamePaused()) {
                ticksInGame++;
                partialTicks = 0;
            }

            calcDelta();
        }
    }
}
package jozufozu.bubbles.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.Entity;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import jozufozu.bubbles.Bubbles;

@Mod.EventBusSubscriber(modid = Bubbles.MODID)
@OnlyIn(Dist.CLIENT)
public class BubbleThingsRenderManager {

    @SubscribeEvent
    public static void renderBubbles(RenderWorldLastEvent event) {
        IProfiler profiler = Minecraft.getInstance().getProfiler();
        profiler.startSection("renderBubbleThings");
        MatrixStack matrixStack = event.getMatrixStack();

        WorldRenderer worldRenderer = event.getContext();

        ClientWorld world = Minecraft.getInstance().world;

        ActiveRenderInfo info = worldRenderer.renderManager.info;

        float partialTicks = event.getPartialTicks();

        Vector3d vector3d = info.getProjectedView();
        double camX = vector3d.getX();
        double camY = vector3d.getY();
        double camZ = vector3d.getZ();

        Matrix4f matrix4f = matrixStack.getLast().getMatrix();

        ClippingHelper cam = new ClippingHelper(matrix4f, event.getProjectionMatrix());
        cam.setCameraPosition(camX, camY, camZ);

        IRenderTypeBuffer.Impl renderTypeBuffers = worldRenderer.renderTypeTextures.getBufferSource();

        for (Entity entity : world.getAllEntities()) {
            tryRenderBubblePart(worldRenderer.renderManager, entity, cam, camX, camY, camZ, partialTicks, matrixStack, renderTypeBuffers);
        }

        renderTypeBuffers.finish();

        profiler.endSection();
    }

    @SuppressWarnings("unchecked")
    private static <T extends Entity> void tryRenderBubblePart(EntityRendererManager renderManager, T entityIn, ClippingHelper cam, double camX, double camY, double camZ, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn) {
        EntityRenderer<? super T> renderer = renderManager.getRenderer(entityIn);

        if (renderer instanceof EntityRendererWithBubbleParts) {

            EntityRendererWithBubbleParts<? super T> bubbleParts = ((EntityRendererWithBubbleParts<? super T>) renderer);

            if (!bubbleParts.shouldRenderBubbleParts(entityIn, cam, camX, camY, camZ)) return;

            double x = MathHelper.lerp(partialTicks, entityIn.lastTickPosX, entityIn.getPosX()) - camX;
            double y = MathHelper.lerp(partialTicks, entityIn.lastTickPosY, entityIn.getPosY()) - camY;
            double z = MathHelper.lerp(partialTicks, entityIn.lastTickPosZ, entityIn.getPosZ()) - camZ;
            float yaw = MathHelper.lerp(partialTicks, entityIn.prevRotationYaw, entityIn.rotationYaw);
            try {
                Vector3d vector3d = renderer.getRenderOffset(entityIn, partialTicks);
                double d2 = x + vector3d.getX();
                double d3 = y + vector3d.getY();
                double d0 = z + vector3d.getZ();
                matrixStackIn.push();
                matrixStackIn.translate(d2, d3, d0);
                bubbleParts.renderBubbleParts(entityIn, yaw, partialTicks, matrixStackIn, bufferIn, renderManager.getPackedLight(entityIn, partialTicks));

                matrixStackIn.pop();
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering entity in world");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being rendered");
                entityIn.fillCrashReport(crashreportcategory);
                CrashReportCategory crashreportcategory1 = crashreport.makeCategory("Renderer details");
                crashreportcategory1.addDetail("Assigned renderer", renderer);
                crashreportcategory1.addDetail("Location", CrashReportCategory.getCoordinateInfo(x, y, z));
                crashreportcategory1.addDetail("Rotation", yaw);
                crashreportcategory1.addDetail("Delta", partialTicks);
                throw new ReportedException(crashreport);
            }
        }
    }
}

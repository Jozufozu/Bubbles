package jozufozu.bubbles.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import jozufozu.bubbles.Bubbles;
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

@Mod.EventBusSubscriber(modid = Bubbles.MODID)
@OnlyIn(Dist.CLIENT)
public class BubbleThingsRenderManager {

    @SubscribeEvent
    public static void renderBubbles(RenderWorldLastEvent event) {
        IProfiler profiler = Minecraft.getInstance().getProfiler();
        profiler.push("renderBubbleThings");
        MatrixStack matrixStack = event.getMatrixStack();

        WorldRenderer worldRenderer = event.getContext();

        ClientWorld world = Minecraft.getInstance().level;

        ActiveRenderInfo info = worldRenderer.entityRenderDispatcher.camera;

        float partialTicks = event.getPartialTicks();

        Vector3d vector3d = info.getPosition();
        double camX = vector3d.x();
        double camY = vector3d.y();
        double camZ = vector3d.z();

        Matrix4f matrix4f = matrixStack.last().pose();

        ClippingHelper cam = new ClippingHelper(matrix4f, event.getProjectionMatrix());
        cam.prepare(camX, camY, camZ);

        IRenderTypeBuffer.Impl renderTypeBuffers = worldRenderer.renderBuffers.bufferSource();

        for (Entity entity : world.entitiesForRendering()) {
            tryRenderBubblePart(worldRenderer.entityRenderDispatcher, entity, cam, camX, camY, camZ, partialTicks, matrixStack, renderTypeBuffers);
        }

        renderTypeBuffers.endBatch();

        profiler.pop();
    }

    @SuppressWarnings("unchecked")
    private static <T extends Entity> void tryRenderBubblePart(EntityRendererManager renderManager, T entityIn, ClippingHelper cam, double camX, double camY, double camZ, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn) {
        EntityRenderer<? super T> renderer = renderManager.getRenderer(entityIn);

        if (renderer instanceof EntityRendererWithBubbleParts) {

            EntityRendererWithBubbleParts<? super T> bubbleParts = ((EntityRendererWithBubbleParts<? super T>) renderer);

            if (!bubbleParts.shouldRenderBubbleParts(entityIn, cam, camX, camY, camZ)) return;

            double x = MathHelper.lerp(partialTicks, entityIn.xOld, entityIn.getX()) - camX;
            double y = MathHelper.lerp(partialTicks, entityIn.yOld, entityIn.getY()) - camY;
            double z = MathHelper.lerp(partialTicks, entityIn.zOld, entityIn.getZ()) - camZ;
            float yaw = MathHelper.lerp(partialTicks, entityIn.yRotO, entityIn.yRot);
            try {
                Vector3d vector3d = renderer.getRenderOffset(entityIn, partialTicks);
                double d2 = x + vector3d.x();
                double d3 = y + vector3d.y();
                double d0 = z + vector3d.z();
                matrixStackIn.pushPose();
                matrixStackIn.translate(d2, d3, d0);
                bubbleParts.renderBubbleParts(entityIn, yaw, partialTicks, matrixStackIn, bufferIn, renderManager.getPackedLightCoords(entityIn, partialTicks));

                matrixStackIn.popPose();
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering entity in world");
                CrashReportCategory crashreportcategory = crashreport.addCategory("Entity being rendered");
                entityIn.fillCrashReportCategory(crashreportcategory);
                CrashReportCategory crashreportcategory1 = crashreport.addCategory("Renderer details");
                crashreportcategory1.setDetail("Assigned renderer", renderer);
                crashreportcategory1.setDetail("Location", CrashReportCategory.formatLocation(x, y, z));
                crashreportcategory1.setDetail("Rotation", yaw);
                crashreportcategory1.setDetail("Delta", partialTicks);
                throw new ReportedException(crashreport);
            }
        }
    }
}

package red4.bubbles.client.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.IShaderManager;
import net.minecraft.client.shader.ShaderLinkHelper;
import net.minecraft.client.shader.ShaderLoader;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.system.MemoryUtil;
import red4.bubbles.Bubbles;
import red4.bubbles.client.ClientTickHandler;

import javax.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public class ShaderHelper {

    public static final FloatBuffer FLOAT_BUFFER = MemoryUtil.memAllocFloat(1);
    public static final FloatBuffer VEC3_BUFFER = MemoryUtil.memAllocFloat(3);
    public static final FloatBuffer VEC2_BUFFER = MemoryUtil.memAllocFloat(2);
    private static final Map<Shader, ShaderProgram> PROGRAMS = new EnumMap<>(Shader.class);

    @SuppressWarnings("deprecation")
    public static void initShaders() {
        // Can be null when running datagenerators due to the unfortunate time we call this
        if (Minecraft.getInstance() != null
                && Minecraft.getInstance().getResourceManager() instanceof IReloadableResourceManager) {
            ((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).addReloadListener(
                    (IResourceManagerReloadListener) manager -> {
                        PROGRAMS.values().forEach(ShaderLinkHelper::deleteShader);
                        PROGRAMS.clear();
                        for (Shader shader : Shader.values()) {
                            createProgram(manager, shader);
                        }
                    });
        }
    }

    public static void useShader(Shader shader, @Nullable ShaderCallback cb) {
        ShaderProgram prog = PROGRAMS.get(shader);
        if (prog == null) {
            return;
        }

        int program = prog.getProgram();
        ShaderLinkHelper.func_227804_a_(program);

        int time = GlStateManager.getUniformLocation(program, "time");
        GlStateManager.uniform1i(time, ClientTickHandler.ticksInGame);

        int partialTicks = GlStateManager.getUniformLocation(program, "partialTicks");
        FLOAT_BUFFER.position(0);
        FLOAT_BUFFER.put(0, ClientTickHandler.partialTicks);
        GlStateManager.uniform1f(partialTicks, FLOAT_BUFFER);

        MainWindow window = Minecraft.getInstance().getMainWindow();

        int height = window.getHeight();
        int width = window.getWidth();

        ShaderHelper.VEC2_BUFFER.position(0);
        ShaderHelper.VEC2_BUFFER.put(0, (float) width);
        ShaderHelper.VEC2_BUFFER.put(1, (float) height);

        int windowSize = GlStateManager.getUniformLocation(program, "windowSize");
        GlStateManager.uniform2f(windowSize, ShaderHelper.VEC2_BUFFER);

        Vector3d cameraPos = Minecraft.getInstance().getRenderManager().info.getProjectedView();

        ShaderHelper.VEC3_BUFFER.position(0);
        ShaderHelper.VEC3_BUFFER.put(0, (float) cameraPos.x);
        ShaderHelper.VEC3_BUFFER.put(1, (float) cameraPos.y);
        ShaderHelper.VEC3_BUFFER.put(2, (float) cameraPos.z);

        int camera = GlStateManager.getUniformLocation(program, "cameraPos");
        GlStateManager.uniform3f(camera, ShaderHelper.VEC3_BUFFER);

        if (cb != null) {
            cb.call(program);
        }
    }

    public static void releaseShader() {
        ShaderLinkHelper.func_227804_a_(0);
    }

    private static void createProgram(IResourceManager manager, Shader shader) {
        try {
            ShaderLoader vert = createShader(manager, shader.vert, ShaderLoader.ShaderType.VERTEX);
            ShaderLoader frag = createShader(manager, shader.frag, ShaderLoader.ShaderType.FRAGMENT);
            int progId = ShaderLinkHelper.createProgram();
            ShaderProgram prog = new ShaderProgram(progId, vert, frag);
            ShaderLinkHelper.linkProgram(prog);
            PROGRAMS.put(shader, prog);

            Bubbles.LOGGER.info("Loaded program {}", shader.name());

        } catch (IOException ex) {
            Bubbles.LOGGER.error("Failed to load program {}", shader.name(), ex);
        }
    }

    private static ShaderLoader createShader(IResourceManager manager, String filename, ShaderLoader.ShaderType shaderType) throws IOException {
        ResourceLocation loc = new ResourceLocation(Bubbles.MODID, filename);
        try (InputStream is = new BufferedInputStream(manager.getResource(loc).getInputStream())) {
            return ShaderLoader.func_216534_a(shaderType, loc.toString(), is, shaderType.name().toLowerCase(Locale.ROOT));
        }
    }

    private static class ShaderProgram implements IShaderManager {
        private final int program;
        private final ShaderLoader vert;
        private final ShaderLoader frag;

        private ShaderProgram(int program, ShaderLoader vert, ShaderLoader frag) {
            this.program = program;
            this.vert = vert;
            this.frag = frag;
        }

        @Override
        public int getProgram() {
            return program;
        }

        @Override
        public void markDirty() {

        }

        @Override
        public ShaderLoader getVertexShaderLoader() {
            return vert;
        }

        @Override
        public ShaderLoader getFragmentShaderLoader() {
            return frag;
        }
    }
}

package jozufozu.bubbles.render.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import jozufozu.bubbles.Bubbles;
import jozufozu.bubbles.render.ClientTickHandler;
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
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;

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
    public static final FloatBuffer VEC2_BUFFER = MemoryUtil.memAllocFloat(2);
    public static final FloatBuffer VEC3_BUFFER = MemoryUtil.memAllocFloat(3);

    private static final Map<Shader, ShaderProgram> PROGRAMS = new EnumMap<>(Shader.class);

    @SuppressWarnings("deprecation")
    public static void initShaders() {
        // Can be null when running datagenerators due to the unfortunate time we call this
        if (Minecraft.getInstance() != null
                && Minecraft.getInstance().getResourceManager() instanceof IReloadableResourceManager) {
            ((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(
                    (IResourceManagerReloadListener) manager -> {
                        PROGRAMS.values().forEach(ShaderLinkHelper::releaseProgram);
                        PROGRAMS.clear();
                        for (Shader shader : Shader.values()) {
                            createProgram(manager, shader);
                        }
                    });
        }
    }

    public static void useShader(Shader shader) {
        useShader(shader, null);
    }

    public static void useShader(Shader shader, @Nullable ShaderCallback cb) {
        ShaderProgram prog = PROGRAMS.get(shader);
        if (prog == null) {
            return;
        }

        int program = prog.getId();
        ShaderLinkHelper.glUseProgram(program);

        int time = GlStateManager._glGetUniformLocation(program, "time");
        GL20.glUniform1i(time, ClientTickHandler.ticksInGame);

        int partialTicks = GlStateManager._glGetUniformLocation(program, "partialTicks");
        GL20.glUniform1f(partialTicks, ClientTickHandler.partialTicks);

        MainWindow window = Minecraft.getInstance().getWindow();

        int height = window.getScreenHeight();
        int width = window.getScreenWidth();

        int windowSize = GlStateManager._glGetUniformLocation(program, "windowSize");
        GL20.glUniform2f(windowSize, width, height);

        Vector3d cameraPos = Minecraft.getInstance().getEntityRenderDispatcher().camera.getPosition();
        int camera = GlStateManager._glGetUniformLocation(program, "cameraPos");
        GL20.glUniform3f(camera, (float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z);

        if (cb != null) {
            cb.call(program);
        }
    }

    public static void releaseShader() {
        ShaderLinkHelper.glUseProgram(0);
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
            return ShaderLoader.compileShader(shaderType, loc.toString(), is, shaderType.name().toLowerCase(Locale.ROOT));
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
        public int getId() {
            return program;
        }

        @Override
        public void markDirty() {

        }

        @Override
        public ShaderLoader getVertexProgram() {
            return vert;
        }

        @Override
        public ShaderLoader getFragmentProgram() {
            return frag;
        }
    }
}

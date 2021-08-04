/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package jozufozu.bubbles.render.shader;

import jozufozu.bubbles.Bubbles;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public class ShaderWrappedRenderType extends RenderType {
    public static final ShaderWrappedRenderType BUBBLE = makeBlockLayer(Shader.BUBBLE);

    private static ShaderWrappedRenderType makeBlockLayer(Shader shader) {
        RenderType.State state = RenderType.State.builder().setShadeModelState(SMOOTH_SHADE).setLightmapState(LIGHTMAP).setTextureState(BLOCK_SHEET_MIPPED).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setOutputState(TRANSLUCENT_TARGET).createCompositeState(true);

        return new ShaderWrappedRenderType(shader, create("bubbles:block_bubble", DefaultVertexFormats.BLOCK, 7, 262144, true, true, state));
    }

    private final RenderType delegate;
    private final Shader shader;

    public ShaderWrappedRenderType(Shader shader, RenderType delegate) {
        super(Bubbles.MODID + delegate.toString() + "_with_" + shader.name(), delegate.format(), delegate.mode(), delegate.bufferSize(), delegate.affectsCrumbling(), true,
              () -> {
                  delegate.setupRenderState();
                  ShaderHelper.useShader(shader);
              },
              () -> {
                  ShaderHelper.releaseShader();
                  delegate.clearRenderState();
              });
        this.delegate = delegate;
        this.shader = shader;
    }

    @Override
    public Optional<RenderType> outline() {
        return delegate.outline();
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return other instanceof ShaderWrappedRenderType
                && delegate.equals(((ShaderWrappedRenderType) other).delegate)
                && shader == ((ShaderWrappedRenderType) other).shader;
    }

    @Override
    public int hashCode() {
        return Objects.hash(delegate, shader);
    }
}
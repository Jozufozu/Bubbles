/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package jozufozu.bubbles.client.shader;

import jozufozu.bubbles.Bubbles;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public class ShaderWrappedRenderType extends RenderType {
    public static final ShaderWrappedRenderType BUBBLE = makeBlockLayer(Shader.BUBBLE);

    private static ShaderWrappedRenderType makeBlockLayer(Shader shader) {
        RenderType.State state = RenderType.State.getBuilder().shadeModel(SHADE_ENABLED).lightmap(LIGHTMAP_ENABLED).texture(BLOCK_SHEET_MIPPED).transparency(TRANSLUCENT_TRANSPARENCY).target(field_239236_S_).build(true);

        return new ShaderWrappedRenderType(shader, makeType("bubbles:block_bubble", DefaultVertexFormats.BLOCK, 7, 262144, true, true, state));
    }

    private final RenderType delegate;
    private final Shader shader;

    public ShaderWrappedRenderType(Shader shader, RenderType delegate) {
        super(Bubbles.MODID + delegate.toString() + "_with_" + shader.name(), delegate.getVertexFormat(), delegate.getDrawMode(), delegate.getBufferSize(), delegate.isUseDelegate(), true,
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
    public Optional<RenderType> getOutline() {
        return delegate.getOutline();
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
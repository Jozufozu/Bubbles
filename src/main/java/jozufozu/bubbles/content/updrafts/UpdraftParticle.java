package jozufozu.bubbles.content.updrafts;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public abstract class UpdraftParticle extends SpriteTexturedParticle {
    protected final IAnimatedSprite spriteWithAge;

    protected final boolean mirrored;

    protected UpdraftParticle(IAnimatedSprite spriteWithAge, ClientWorld world, double x, double y, double z) {
        super(world, x, y, z);
        this.spriteWithAge = spriteWithAge;
        this.hasPhysics = false;
        mirrored = this.random.nextBoolean();
    }

    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.yd += 0.005D;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.setSpriteFromAge(this.spriteWithAge);
            this.move(this.xd, this.yd, this.zd);
            this.xd *= 0.85;
            this.zd *= 0.85;

            if (this.level.getBlockState(new BlockPos(this.x, this.y, this.z)).canOcclude()) {
                this.remove();
            }
        }
    }

    public void render(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks) {
        Vector3d cameraPos = renderInfo.getPosition();
        float x = (float)(MathHelper.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float y = (float)(MathHelper.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float z = (float)(MathHelper.lerp(partialTicks, this.zo, this.z) - cameraPos.z());

        Quaternion quaternion = new Quaternion(0.0F, 0.0F, 0.0F, 1.0F);

        quaternion.mul(Vector3f.YP.rotation((float) Math.atan2(x, z)));

        Vector3f[] quad = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
        float scale = this.getQuadSize(partialTicks);

        for (int i = 0; i < 4; ++i) {
            Vector3f vector3f = quad[i];
            vector3f.transform(quaternion);
            vector3f.mul(scale);
            vector3f.add(x, y, z);
        }

        float minU = this.getU0();
        float maxU = this.getU1();
        float minV = this.getV0();
        float maxV = this.getV1();
        int light = this.getLightColor(partialTicks);

        if (mirrored) {
            float t = minU;
            minU = maxU;
            maxU = t;
        }

        buffer.vertex(quad[0].x(), quad[0].y(), quad[0].z()).uv(maxU, maxV).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();
        buffer.vertex(quad[1].x(), quad[1].y(), quad[1].z()).uv(maxU, minV).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();
        buffer.vertex(quad[2].x(), quad[2].y(), quad[2].z()).uv(minU, minV).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();
        buffer.vertex(quad[3].x(), quad[3].y(), quad[3].z()).uv(minU, maxV).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();
    }
}

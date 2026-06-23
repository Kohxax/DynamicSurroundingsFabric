package org.orecruncher.dsurround.effects.particles;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.orecruncher.dsurround.config.WaterRippleStyle;
import org.orecruncher.dsurround.lib.GameUtils;
import org.orecruncher.dsurround.lib.gui.ColorPalette;

public class WaterRippleParticle extends SingleQuadParticle {

    private static final float TEX_SIZE_HALF = 0.5F;
    private static final int BLOCKS_FROM_FADE = 5;
    private static final int MAX_BLOCKS_FADE = 12;

    // Quaternion for 90 degree rotation around X axis to make the quad horizontal (facing up)
    private static final Quaternionf HORIZONTAL_ROTATION = new Quaternionf().rotateX((float) (-Math.PI / 2.0));

    private final WaterRippleStyle rippleStyle;

    private final float growthRate;
    private float texU1;
    private float texU2;
    private float texV1;
    private float texV2;
    private final float defaultColorAlpha;

    public WaterRippleParticle(WaterRippleStyle rippleStyle, ClientLevel world, double x, double y, double z, TextureAtlasSprite sprite) {
        super(world, x, y, z, 0.0, 0.0, 0.0, sprite);

        this.rippleStyle = rippleStyle;
        this.lifetime = rippleStyle.getMaxAge();

        if (rippleStyle.doScaling()) {
            this.growthRate = this.lifetime / 500F;
            this.quadSize = this.growthRate;
        } else {
            this.growthRate = 0F;
            this.quadSize = 1F;
        }

        this.y -= 0.2D;

        var player = GameUtils.getPlayer().orElseThrow();
        var cameraPos = BlockPos.containing(player.getEyePosition(1.0f));
        var position = BlockPos.containing(this.x, this.y, this.z);

        var colorRgb = this.level.getBiome(position).value().getWaterColor();
        this.rCol = ColorPalette.getRed(colorRgb) / 255F;
        this.gCol = ColorPalette.getGreen(colorRgb) / 255F;
        this.bCol = ColorPalette.getBlue(colorRgb) / 255F;

        float distance = (float) Mth.clamp(
                Math.sqrt(cameraPos.distSqr(position)) - BLOCKS_FROM_FADE,
                0,
                MAX_BLOCKS_FADE
        );
        this.alpha = this.defaultColorAlpha = 0.60F * (MAX_BLOCKS_FADE - distance) / MAX_BLOCKS_FADE;

        this.texU1 = rippleStyle.getU1(this.age);
        this.texU2 = rippleStyle.getU2(this.age);
        this.texV1 = rippleStyle.getV1(this.age);
        this.texV2 = rippleStyle.getV2(this.age);
    }

    @Override
    protected @NotNull SingleQuadParticle.Layer getLayer() {
        return new SingleQuadParticle.Layer(true, this.rippleStyle.getTexture(), RenderPipelines.TRANSLUCENT_PARTICLE);
    }

    @Override
    public @NotNull ParticleRenderType getGroup() {
        return ParticleRenderType.SINGLE_QUADS;
    }

    @Override
    public float getQuadSize(float tickDelta) {
        return this.quadSize * Mth.clamp(((float)this.age + tickDelta) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
    }

    @Override
    protected float getU0() {
        return this.texU1;
    }

    @Override
    protected float getU1() {
        return this.texU2;
    }

    @Override
    protected float getV0() {
        return this.texV1;
    }

    @Override
    protected float getV1() {
        return this.texV2;
    }

    @Override
    public void extract(@NotNull QuadParticleRenderState renderState, @NotNull Camera camera, float tickDelta) {
        // Use a horizontal quaternion to make the quad flat on the water surface
        Vec3 camPos = camera.position();
        float x = (float)(Mth.lerp(tickDelta, this.xo, this.x) - camPos.x());
        float y = (float)(Mth.lerp(tickDelta, this.yo, this.y) - camPos.y());
        float z = (float)(Mth.lerp(tickDelta, this.zo, this.z) - camPos.z());
        this.extractRotatedQuad(renderState, HORIZONTAL_ROTATION, x, y, z, tickDelta);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            if (this.rippleStyle.doScaling()) {
                this.quadSize += this.growthRate;
            }

            if (this.rippleStyle.doAlpha()) {
                this.alpha = this.defaultColorAlpha * (float) (this.lifetime - this.age)/this.lifetime;
            }

            this.texU1 = this.rippleStyle.getU1(this.age);
            this.texU2 = this.rippleStyle.getU2(this.age);
            this.texV1 = this.rippleStyle.getV1(this.age);
            this.texV2 = this.rippleStyle.getV2(this.age);
        }
    }
}

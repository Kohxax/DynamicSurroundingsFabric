package org.orecruncher.dsurround.effects.particles;

import net.minecraft.resources.Identifier;

public class DsurroundParticleRenderType {

    private final Identifier texture;

    public DsurroundParticleRenderType(final Identifier texture) {
        this.texture = texture;
    }

    protected Identifier getTexture() {
        return this.texture;
    }

    @Override
    public String toString() {
        return this.texture.toString();
    }
}

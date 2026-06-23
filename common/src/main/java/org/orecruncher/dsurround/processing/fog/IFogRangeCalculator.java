package org.orecruncher.dsurround.processing.fog;

import net.minecraft.client.renderer.fog.FogData;
import org.jetbrains.annotations.NotNull;

public interface IFogRangeCalculator {

    @NotNull
    String getName();

    boolean enabled();

    @NotNull
    FogData render(@NotNull final FogData data, float renderDistance, float partialTick);

    void tick();

    void disconnect();
}

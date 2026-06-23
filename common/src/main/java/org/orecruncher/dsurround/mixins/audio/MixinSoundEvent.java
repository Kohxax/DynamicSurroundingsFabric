package org.orecruncher.dsurround.mixins.audio;

import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(SoundEvent.class)
public class MixinSoundEvent {

    @Shadow
    @Final
    private Identifier location;
    @Shadow
    @Final
    private Optional<Float> fixedRange;

    public String toString() {
        return "%s{fixedRange %s}".formatted(this.location.toString(), this.fixedRange.map(String::valueOf).orElse("variable"));
    }
}

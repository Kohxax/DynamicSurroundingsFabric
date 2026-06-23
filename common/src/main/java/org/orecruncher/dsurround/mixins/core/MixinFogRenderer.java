package org.orecruncher.dsurround.mixins.core;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.joml.Vector4f;
import org.orecruncher.dsurround.eventing.ClientEventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public class MixinFogRenderer {

    @Inject(method = "setupFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/fog/FogRenderer;updateBuffer(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V"))
    private void dsurround_renderFog(Camera camera, int renderDistance, DeltaTracker deltaTracker, float partialTick, ClientLevel level, CallbackInfoReturnable<Vector4f> cir, @Local FogData fogData) {
        ClientEventHooks.FOG_RENDER_EVENT.raise().onRenderFog(fogData, renderDistance, partialTick);
    }
}

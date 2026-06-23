package org.orecruncher.dsurround.mixins.core;

import net.minecraft.client.multiplayer.RegistryDataCollector;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.orecruncher.dsurround.eventing.ClientState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RegistryDataCollector.class)
public class MixinTagCollector {
    @Inject(method = "collectGameRegistries", at = @At("TAIL"))
    private void dsurround_tagsUpdated(ResourceProvider resourceProvider, RegistryAccess.Frozen frozen, boolean local, CallbackInfoReturnable<RegistryAccess.Frozen> cir) {
        if (local)
            ClientState.TAG_SYNC.raise().onTagSync(cir.getReturnValue());
    }
}

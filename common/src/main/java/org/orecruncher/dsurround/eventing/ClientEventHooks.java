package org.orecruncher.dsurround.eventing;

import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import org.orecruncher.dsurround.lib.events.EventingFactory;
import org.orecruncher.dsurround.lib.events.IPhasedEvent;

import java.util.Collection;

public final class ClientEventHooks {
    public static final IPhasedEvent<ICollectDiagnostics> COLLECT_DIAGNOSTICS = EventingFactory.createPrioritizedEvent(callbacks -> event -> {
        for (var callback : callbacks) {
            callback.onCollect(event);
        }
    });

    public static final IPhasedEvent<IBlockUpdates> BLOCK_UPDATE = EventingFactory.createPrioritizedEvent(callbacks -> blockPositions -> {
        for (var callback : callbacks) {
            callback.onBlockUpdates(blockPositions);
        }
    });

    public static final IPhasedEvent<IEntityStep> ENTITY_STEP_EVENT = EventingFactory.createPrioritizedEvent(callbacks -> (entity, blockPos, blockState) -> {
        for (var callback : callbacks) {
            callback.onStep(entity, blockPos, blockState);
        }
    });

    public static final IPhasedEvent<IFogRender> FOG_RENDER_EVENT = EventingFactory.createPrioritizedEvent(callbacks -> (data, renderDistance, partialTick) -> {
        for (var callback : callbacks) {
            callback.onRenderFog(data, renderDistance, partialTick);
        }
    });

    @FunctionalInterface
    public interface IBlockUpdates {
        void onBlockUpdates(Collection<BlockPos> blockPositions);
    }

    @FunctionalInterface
    public interface ICollectDiagnostics {
        void onCollect(CollectDiagnosticsEvent event);
    }

    @FunctionalInterface
    public interface IEntityStep {
        void onStep(Entity entity, BlockPos stepPosition, BlockState blockState);
    }

    @FunctionalInterface
    public interface IFogRender {
        void onRenderFog(FogData data, float renderDistance, float partialTick);
    }

}

package org.orecruncher.dsurround.effects.particles;

import dev.architectury.event.events.client.ClientLifecycleEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.orecruncher.dsurround.eventing.ClientEventHooks;
import org.orecruncher.dsurround.eventing.ClientState;
import org.orecruncher.dsurround.eventing.CollectDiagnosticsEvent;
import org.orecruncher.dsurround.lib.GameUtils;
import org.orecruncher.dsurround.lib.collections.ObjectArray;

import java.lang.ref.WeakReference;
import java.util.function.Supplier;

/**
 * Special particle that proxies a collection in the particle engine. The commonality
 * of the collection is rendering setup. This collection is centered on the player
 * to prevent it from going out of scope. It is modeled on the NoRenderParticle in
 * Minecraft.
 */
public final class ParticleRenderCollection<TParticle extends SingleQuadParticle> extends Particle {

    private final ObjectArray<TParticle> particles;

    private ParticleRenderCollection(@NotNull ClientLevel clientLevel) {
        super(clientLevel, 0, 0, 0);
        this.particles = new ObjectArray<>(128);
        this.tick();
    }

    @NotNull
    @Override
    public ParticleRenderType getGroup() {
        // Use NO_RENDER since we manage sub-particle rendering ourselves
        // Sub-particles are added directly to the particle engine
        return ParticleRenderType.NO_RENDER;
    }

    @Override
    public void tick() {
        // Keep the particle colocated with the player
        var playerPos = GameUtils.getPlayer().orElseThrow().getEyePosition();
        this.setPos(playerPos.x(), playerPos.y(), playerPos.z());

        if (!this.particles.isEmpty()) {
            this.particles.forEach(Particle::tick);
            this.particles.removeIf(p -> !p.isAlive());
        }
    }

    public void add(@NotNull TParticle particle) {
        this.particles.add(particle);
        // Also add to the particle engine so it participates in the extract/render pipeline
        GameUtils.getParticleManager().add(particle);
    }

    /**
     * Helper that manages related particles in Minecraft's ParticleEngine. The helper will register with events, so
     * instances of this class need to be maintained as singletons throughout the lifetime of the client.
     */
    public static final class Helper<TParticle extends SingleQuadParticle> {

        private final String name;

        private WeakReference<ParticleRenderCollection<TParticle>> particle;
        private String diagnostics;

        /**
         * Initializes a helper instance used to manage the state of the main particle within the ParticleEngine.
         *
         * @param name The name of the helper; used in diagnostics
         * @param textureSupplier Provides the texture to bind when rendering (kept for API compatibility)
         */
        public Helper(@NotNull String name, @NotNull Supplier<Identifier> textureSupplier) {
            this(name, textureSupplier, null);
        }

        /**
         * Initializes a helper instance used to manage the state of the main particle within the ParticleEngine.
         *
         * @param name The name of the helper; used in diagnostics
         * @param textureSupplier Provides the texture to bind when rendering (kept for API compatibility)
         * @param setup Unused in new rendering system, kept for API compatibility
         */
        public Helper(@NotNull String name, @NotNull Supplier<Identifier> textureSupplier, @Nullable Object setup) {
            this.name = name;
            this.diagnostics = this.name;

            ClientLifecycleEvent.CLIENT_LEVEL_LOAD.register(state -> this.clear());
            ClientEventHooks.COLLECT_DIAGNOSTICS.register(this::collectDiagnostics);
            ClientState.TICK_END.register(this::tick);
        }

        /**
         * Adds a particle to the helper.
         *
         * @param particle The particle to add
         */
        public void add(TParticle particle) {
            this.get().add(particle);
        }

        @NotNull
        private ParticleRenderCollection<TParticle> get() {
            var pc = this.particle != null ? this.particle.get() : null;
            if (pc == null || !pc.isAlive()) {
                pc = new ParticleRenderCollection<>(GameUtils.getWorld().orElseThrow());
                this.particle = new WeakReference<>(pc);
                GameUtils.getParticleManager().add(pc);
            }
            return pc;
        }

        private void clear() {
            var pc = this.particle != null ? this.particle.get() : null;
            if (pc != null) {
                pc.remove();
                this.particle = null;
            }
        }

        private void tick(@NotNull Minecraft client) {
            var pc = this.particle != null ? this.particle.get() : null;
            this.diagnostics = this.name + ": ";
            if (pc == null)
                this.diagnostics += "Not Set";
            else if (!pc.isAlive())
                this.diagnostics += "DEAD";
            else
                this.diagnostics += pc.particles.size();
        }

        private void collectDiagnostics(@NotNull CollectDiagnosticsEvent event) {
            event.add(CollectDiagnosticsEvent.Section.Particles, this.diagnostics);
        }
    }
}

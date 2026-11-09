package com.vloxboost.mixin;

import com.vloxboost.BoostConfig;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Drops new particles once the cap is reached. A single explosion or a
 * campfire-heavy base can otherwise hold tens of thousands of particles alive and
 * cost more than the terrain does.
 */
@Mixin(ParticleEngine.class)
public abstract class ParticleCapMixin {
	@Shadow
	@Final
	private Map<ParticleRenderType, ParticleGroup<?>> particles;

	@Inject(method = "add", at = @At("HEAD"), cancellable = true)
	private void vloxboost$capParticles(Particle particle, CallbackInfo info) {
		BoostConfig config = BoostConfig.get();
		if (!config.particleCap) {
			return;
		}

		int live = 0;
		for (ParticleGroup<?> group : this.particles.values()) {
			live += group.size();
			if (live >= config.maxParticles) {
				info.cancel();
				return;
			}
		}
	}
}

package com.vloxboost;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Mixin bugs are invisible until the client launches, where a wrong descriptor is a
 * crash. These tests read the real 26.2 classes and fail if any hook the mod
 * declares has moved, been renamed, or changed shape.
 */
class MixinTargetTest {
	@Test
	void entityCullTargetExists() throws Exception {
		Method shouldRender = EntityRenderDispatcher.class.getMethod("shouldRender",
				Entity.class, Frustum.class, double.class, double.class, double.class);

		assertEquals(boolean.class, shouldRender.getReturnType(),
				"shouldRender must keep returning boolean so cancelling it with false is valid");

		Entity.class.getMethod("distanceToSqr", Entity.class);
	}

	@Test
	void particleCapTargetsExist() throws Exception {
		ParticleEngine.class.getMethod("add", Particle.class);

		Field particles = ParticleEngine.class.getDeclaredField("particles");
		assertTrue(Map.class.isAssignableFrom(particles.getType()),
				"particles must stay a Map so the cap can sum the live count");
		assertTrue(Modifier.isFinal(particles.getModifiers()), "the @Shadow field is declared final");

		Class<?> group = Class.forName("net.minecraft.client.particle.ParticleGroup");
		group.getMethod("size");
	}

	@Test
	void blockEntityCullTargetExists() throws Exception {
		Method extract = BlockEntityRenderDispatcher.class.getMethod("tryExtractRenderState",
				BlockEntity.class, float.class, ModelFeatureRenderer.CrumblingOverlay.class, boolean.class);

		assertEquals(BlockEntityRenderState.class, extract.getReturnType(),
				"returning null is only safe while the state type is the declared return type");
	}

	@Test
	void tickTargetExists() throws Exception {
		Minecraft.class.getMethod("tick");
		Minecraft.class.getMethod("getFps");
		Minecraft.class.getMethod("getCameraEntity");
	}

	@Test
	void renderDistanceOptionExists() throws Exception {
		Method accessor = Options.class.getMethod("renderDistance");

		assertEquals(0, accessor.getParameterTypes().length, "renderDistance takes no arguments");
		assertEquals("net.minecraft.client.OptionInstance", accessor.getReturnType().getName());
	}
}

package com.vloxboost.mixin;

import com.vloxboost.BoostConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vanilla only asks whether an entity is inside the render distance and the
 * frustum. This adds a hard distance limit that is usually far shorter, which is
 * where most of the win is on a busy server: the entity is never extracted, so
 * no model, no pose and no buffer is built for it.
 */
@Mixin(EntityRenderDispatcher.class)
public class EntityCullMixin {
	@Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
	private void vloxboost$skipDistantEntity(Entity entity, Frustum frustum, double x, double y, double z,
			CallbackInfoReturnable<Boolean> info) {
		BoostConfig config = BoostConfig.get();
		if (!config.entityCull) {
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft == null) {
			return;
		}

		Entity camera = minecraft.getCameraEntity();
		if (camera == null || camera == entity) {
			return;
		}

		double limit = config.entityDistance;
		if (camera.distanceToSqr(entity) > limit * limit) {
			info.setReturnValue(false);
		}
	}
}

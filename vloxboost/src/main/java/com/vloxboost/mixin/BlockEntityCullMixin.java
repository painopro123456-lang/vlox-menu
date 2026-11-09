package com.vloxboost.mixin;

import com.vloxboost.BoostConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Skips block entities that are too far away to read. The caller already handles
 * a null render state, which is what vanilla returns for a block entity without a
 * renderer, so returning null here is the supported path out.
 */
@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityCullMixin {
	@Inject(method = "tryExtractRenderState", at = @At("HEAD"), cancellable = true)
	private void vloxboost$skipDistantBlockEntity(BlockEntity blockEntity, float partialTick,
			ModelFeatureRenderer.CrumblingOverlay overlay, boolean bl,
			CallbackInfoReturnable<Object> info) {
		BoostConfig config = BoostConfig.get();
		if (!config.blockEntityCull) {
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft == null) {
			return;
		}

		Entity camera = minecraft.getCameraEntity();
		if (camera == null) {
			return;
		}

		BlockPos pos = blockEntity.getBlockPos();
		double dx = camera.getX() - (pos.getX() + 0.5D);
		double dy = camera.getY() - (pos.getY() + 0.5D);
		double dz = camera.getZ() - (pos.getZ() + 0.5D);
		double limit = config.blockEntityDistance;

		if (dx * dx + dy * dy + dz * dz > limit * limit) {
			info.setReturnValue(null);
		}
	}
}

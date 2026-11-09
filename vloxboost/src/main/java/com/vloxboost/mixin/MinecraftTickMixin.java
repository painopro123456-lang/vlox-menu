package com.vloxboost.mixin;

import com.vloxboost.DynamicQuality;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The only tick hook the mod needs; no Fabric API dependency. */
@Mixin(Minecraft.class)
public class MinecraftTickMixin {
	@Inject(method = "tick", at = @At("TAIL"))
	private void vloxboost$tick(CallbackInfo info) {
		DynamicQuality.tick((Minecraft) (Object) this);
	}
}

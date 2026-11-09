package com.vloxboost;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VloxBoost implements ClientModInitializer {
	public static final String MOD_ID = "vloxboost";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		BoostConfig.load();
		BoostConfig config = BoostConfig.get();

		LOGGER.info("VloxBoost ready: entity cull {} at {}m, block entity cull {} at {}m, particle cap {} at {}, "
						+ "adaptive render distance {} targeting {} fps",
				config.entityCull, config.entityDistance,
				config.blockEntityCull, config.blockEntityDistance,
				config.particleCap, config.maxParticles,
				config.dynamicRenderDistance, config.targetFps);
	}
}

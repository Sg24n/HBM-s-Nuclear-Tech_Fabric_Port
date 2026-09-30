package com.hbm.main;

import com.hbm.content.HbmCreativeTabs;
import com.hbm.content.ItemRegistry;
import com.hbm.core.placeholder.PlaceholderContent;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainRegistry implements ModInitializer {
	public static final String MOD_ID = "hbm";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ItemRegistry.initialize();
		PlaceholderContent.initialize();
		HbmCreativeTabs.initialize();
		LOGGER.info("Hbm's Nuclear Tech initialized");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}

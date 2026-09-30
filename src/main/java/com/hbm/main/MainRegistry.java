package com.hbm.main;

import com.hbm.component.HbmDataComponents;
import com.hbm.content.HbmCreativeTabs;
import com.hbm.content.ItemRegistry;
import com.hbm.core.placeholder.PlaceholderContent;
import com.hbm.recipe.MachineRecipes;
import com.hbm.recipe.PressRecipes;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainRegistry implements ModInitializer {
	public static final String MOD_ID = "hbm";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		HbmDataComponents.initialize();
		ItemRegistry.initialize();
		MachineRecipes.load();
		PlaceholderContent.initialize();
		HbmCreativeTabs.initialize();
		// Recipes build ItemStacks, which need bound components: defer to server start.
		ServerLifecycleEvents.SERVER_STARTING.register(server -> PressRecipes.initialize());
		LOGGER.info("Hbm's Nuclear Tech initialized");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}

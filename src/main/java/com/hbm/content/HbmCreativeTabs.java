package com.hbm.content;

import com.hbm.core.placeholder.PlaceholderContent;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.Locale;

public final class HbmCreativeTabs {

	private HbmCreativeTabs() {
	}

	private static String stackKey(ItemStack stack) {
		return BuiltInRegistries.ITEM.getKey(stack.getItem()) + "|" + stack.getComponents();
	}

	public static void initialize() {
		for (PlaceholderContent.Tab tab : PlaceholderContent.tabs()) {
			String path = tab.key().split(":")[1].toLowerCase(Locale.ROOT);
			CreativeModeTab group = FabricCreativeModeTab.builder()
					.title(Component.translatable(tab.titleKey()))
					.icon(() -> PlaceholderContent.iconStack(tab))
					.displayItems((context, output) -> {
						Set<String> seen = new HashSet<>();
						for (PlaceholderContent.Stack stack : PlaceholderContent.tab(tab.key())) {
							ItemStack itemStack = PlaceholderContent.stack(stack);
							if (seen.add(stackKey(itemStack))) {
								output.accept(itemStack);
							}
						}
					})
					.build();
			Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
					ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath("hbm", path)), group);
		}
		addTo(vanilla("transportation"), "3:transportation");
		addTo(vanilla("ingredients"), "4:misc");
		addTo(vanilla("tools_and_utilities"), "7:tools");
		addTo(vanilla("combat"), "8:combat");
	}

	private static ResourceKey<CreativeModeTab> vanilla(String path) {
		return ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace(path));
	}

	private static void addTo(ResourceKey<CreativeModeTab> tab, String key) {
		CreativeModeTabEvents.modifyOutputEvent(tab).register(output -> {
			Set<String> seen = new HashSet<>();
			for (PlaceholderContent.Stack stack : PlaceholderContent.tab(key)) {
				ItemStack itemStack = PlaceholderContent.stack(stack);
				if (seen.add(stackKey(itemStack))) {
					output.accept(itemStack);
				}
			}
		});
	}
}

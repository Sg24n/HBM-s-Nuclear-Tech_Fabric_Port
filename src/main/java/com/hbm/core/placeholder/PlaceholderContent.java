package com.hbm.core.placeholder;

import com.hbm.main.MainRegistry;
import com.hbm.content.ItemRegistry;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PlaceholderContent {

	public record Stack(String path, int index, int meta, String display, String model) {
	}

	public record Tab(String key, String iconPath, String titleKey) {
	}

	public static final DataComponentType<Integer> PLACEHOLDER_INDEX = DataComponentType.<Integer>builder()
			.persistent(Codec.INT)
			.build();

	private static final Map<String, Item> ITEMS = new HashMap<>();
	private static final Map<String, List<Stack>> BY_TAB = new LinkedHashMap<>();
	private static final List<Tab> TABS = new ArrayList<>();
	private static int registered;

	private PlaceholderContent() {
	}

	public static void initialize() {
		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
				Identifier.fromNamespaceAndPath(MainRegistry.MOD_ID, "placeholder_index"), PLACEHOLDER_INDEX);

		List<String> lines = readLines("/hbm/legacy/creative.tsv");
		java.util.Set<String> paths = new java.util.LinkedHashSet<>();
		for (String line : lines) {
			String[] parts = line.split("\t", -1);
			if (parts.length < 5) {
				continue;
			}
			String path = parts[2];
			paths.add(path);
			item(path);
			BY_TAB.computeIfAbsent(parts[0], key -> new ArrayList<>())
					.add(new Stack(path, Integer.parseInt(parts[1]), Integer.parseInt(parts[3]), parts[4],
							parts.length > 5 ? parts[5] : ""));
		}
		if (ITEMS.size() != paths.size()) {
			throw new IllegalStateException("placeholder registration mismatch: " + ITEMS.size() + " registered vs " + paths.size() + " declared");
		}

		for (String line : readLines("/hbm/legacy/tabs.tsv")) {
			String[] parts = line.split("\t", -1);
			if (parts.length >= 3) {
				TABS.add(new Tab(parts[0], parts[1], parts[2]));
			}
		}
		if (TABS.isEmpty()) {
			throw new IllegalStateException("missing /hbm/legacy/tabs.tsv");
		}

		int stacks = BY_TAB.values().stream().mapToInt(List::size).sum();
		MainRegistry.LOGGER.info("Placeholder content: {} items, {} creative stacks, {} tabs", registered, stacks, TABS.size());
		for (Tab tab : TABS) {
			MainRegistry.LOGGER.info("Placeholder tab {}: {} stacks, icon {}", tab.key(), tab(tab.key()).size(), tab.iconPath());
		}
	}

	public static Item item(String path) {
		Item existing = ITEMS.get(path);
		if (existing != null) {
			return existing;
		}
		Identifier id = Identifier.fromNamespaceAndPath(MainRegistry.MOD_ID, path);
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
		Item item = new Item(new Item.Properties().setId(key));
		Registry.register(BuiltInRegistries.ITEM, key, item);
		ITEMS.put(path, item);
		registered++;
		return item;
	}

	public static List<Tab> tabs() {
		return TABS;
	}

	public static List<Stack> tab(String tab) {
		return BY_TAB.getOrDefault(tab, List.of());
	}

	public static ItemStack stack(Stack stack) {
		ItemStack real = ItemRegistry.stack(stack.path(), stack.meta());
		if (!real.isEmpty()) {
			return real;
		}
		ItemStack itemStack = new ItemStack(ITEMS.get(stack.path()));
		itemStack.set(DataComponents.CUSTOM_NAME, Component.literal(stack.display()));
		itemStack.set(PLACEHOLDER_INDEX, stack.index());
		if (!stack.model().isEmpty()) {
			itemStack.set(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath(MainRegistry.MOD_ID, stack.model()));
		}
		return itemStack;
	}

	public static ItemStack iconStack(Tab tab) {
		List<Stack> stacks = tab(tab.key());
		for (Stack stack : stacks) {
			if (stack.path().equals(tab.iconPath())) {
				return stack(stack);
			}
		}
		return stacks.isEmpty() ? ItemStack.EMPTY : stack(stacks.get(0));
	}

	private static List<String> readLines(String resource) {
		try (InputStream stream = PlaceholderContent.class.getResourceAsStream(resource)) {
			if (stream == null) {
				throw new IllegalStateException("missing resource " + resource);
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
			List<String> lines = new ArrayList<>();
			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.isBlank()) {
					lines.add(line);
				}
			}
			return lines;
		} catch (Exception exception) {
			throw new IllegalStateException("failed to read " + resource, exception);
		}
	}
}

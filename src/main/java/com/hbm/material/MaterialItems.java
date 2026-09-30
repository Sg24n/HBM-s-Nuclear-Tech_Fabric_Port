package com.hbm.material;

import com.hbm.main.MainRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
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
import java.util.List;
import java.util.Map;

public final class MaterialItems {

	public record Entry(String legacyPath, int meta, Item item) {
	}

	private static final Map<String, Entry> BY_LEGACY = new HashMap<>();
	private static final Map<Item, NTMMaterial> MATERIAL_OF = new HashMap<>();
	private static final List<Item> ORDER = new ArrayList<>();
	private static int registered;

	private MaterialItems() {
	}

	public static void initialize() {
		for (String line : readLines("/hbm/legacy/material-items.tsv")) {
			String[] parts = line.split("\t", -1);
			if (parts.length < 3) {
				continue;
			}
			String path = "item." + parts[0];
			int meta = Integer.parseInt(parts[1]);
			Identifier id = Identifier.fromNamespaceAndPath(MainRegistry.MOD_ID, parts[2].contains(":")
					? parts[2].split(":", 2)[1] : parts[2]);
			ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
			Item item = new Item(new Item.Properties().setId(key));
			Registry.register(BuiltInRegistries.ITEM, key, item);
			Entry entry = new Entry(path, meta, item);
			BY_LEGACY.put(key(entry.legacyPath(), entry.meta()), entry);
			NTMMaterial material = parts.length > 3 && !parts[3].isBlank()
					? Mats.matByName.get(parts[3]) : Mats.matById.get(meta);
			if (material != null) {
				MATERIAL_OF.put(item, material);
			}
			ORDER.add(item);
			registered++;
		}
		MainRegistry.LOGGER.info("Material items: {} registered from matrix", registered);
	}

	public static Item get(String legacyPath, int meta) {
		Entry entry = BY_LEGACY.get(key(legacyPath, meta));
		return entry == null ? null : entry.item();
	}

	public static ItemStack stack(String legacyPath, int meta) {
		Item item = get(legacyPath, meta);
		return item == null ? ItemStack.EMPTY : new ItemStack(item);
	}

	public static NTMMaterial materialOf(Item item) {
		return MATERIAL_OF.get(item);
	}

	public static List<Item> registered() {
		return List.copyOf(ORDER);
	}

	private static String key(String legacyPath, int meta) {
		return legacyPath + "|" + meta;
	}

	private static List<String> readLines(String resource) {
		try (InputStream stream = MaterialItems.class.getResourceAsStream(resource)) {
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

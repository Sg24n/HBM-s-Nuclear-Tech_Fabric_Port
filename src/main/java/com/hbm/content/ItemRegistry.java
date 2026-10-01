package com.hbm.content;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hbm.block.HbmBlockShapes;
import com.hbm.block.HbmDirectionalBlock;
import com.hbm.block.HbmShapedBlock;
import com.hbm.main.MainRegistry;
import com.hbm.material.Mats;
import com.hbm.material.NTMMaterial;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.AABB;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ItemRegistry {

	public record Entry(String legacyPath, int meta, Item item) {
	}

	private static final List<String> TABLES = List.of(
			"/hbm/legacy/material-items.tsv",
			"/hbm/legacy/identity-items.tsv",
			"/hbm/legacy/hidden-items.tsv");

	private static final List<String> BLOCK_TABLES = List.of(
			"/hbm/legacy/block-items.tsv",
			"/hbm/legacy/hidden-blocks.tsv");

	private static final Map<String, Entry> BY_LEGACY = new HashMap<>();
	private static final Map<Item, NTMMaterial> MATERIAL_OF = new HashMap<>();
	private static final Map<String, int[]> BLOCK_PROPS = new HashMap<>();
	private static final Map<String, List<AABB>> BLOCK_SHAPES = new HashMap<>();
	private static int registered;

	private ItemRegistry() {
	}

	public static void initialize() {
		loadBlockProperties();
		loadBlockShapes();
		for (String table : TABLES) {
			for (String line : readLines(table)) {
				String[] parts = line.split("\t", -1);
				if (parts.length < 3) {
					continue;
				}
				String path = "item." + parts[0];
				int meta = Integer.parseInt(parts[1]);
				String idPath = parts[2].contains(":") ? parts[2].split(":", 2)[1] : parts[2];
				Identifier id = Identifier.fromNamespaceAndPath(MainRegistry.MOD_ID, idPath);
				ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
				Item item = new Item(new Item.Properties().setId(key));
				Registry.register(BuiltInRegistries.ITEM, key, item);
				BY_LEGACY.put(key(path, meta), new Entry(path, meta, item));
				NTMMaterial material = parts.length > 3 && !parts[3].isBlank()
						? Mats.matByName.get(parts[3]) : null;
				if (material != null) {
					MATERIAL_OF.put(item, material);
				}
				registered++;
			}
		}
		initializeBlocks();
		initializeVariants();
		MainRegistry.LOGGER.info("Item registry: {} items registered", registered);
	}

	private static void initializeBlocks() {
		for (String table : BLOCK_TABLES) {
			for (String line : readLines(table)) {
				String[] parts = line.split("\t", -1);
				if (parts.length < 3) {
					continue;
				}
				String path = "tile." + parts[0];
				int meta = Integer.parseInt(parts[1]);
				String idPath = parts[2].contains(":") ? parts[2].split(":", 2)[1] : parts[2];
				Identifier id = Identifier.fromNamespaceAndPath(MainRegistry.MOD_ID, idPath);
				ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
				int[] properties = BLOCK_PROPS.getOrDefault(parts[0], new int[] { 0, 0 });
				BlockBehaviour.Properties behaviour = BlockBehaviour.Properties.of().setId(blockKey);
				if (properties[0] > 0) {
					int light = properties[0];
					behaviour = behaviour.lightLevel(state -> light);
				}
				Block block;
				if (properties[1] == 1) {
					block = new HbmDirectionalBlock(behaviour.noOcclusion());
				} else if (BLOCK_SHAPES.containsKey(parts[0])) {
					block = new HbmShapedBlock(behaviour.noOcclusion());
				} else {
					block = new Block(behaviour);
				}
				Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
				List<AABB> boxes = BLOCK_SHAPES.get(parts[0]);
				if (boxes != null) {
					HbmBlockShapes.put(block, boxes);
				}
				ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
				BlockItem item = new BlockItem(block, new Item.Properties().setId(itemKey));
				Registry.register(BuiltInRegistries.ITEM, itemKey, item);
				BY_LEGACY.put(key(path, meta), new Entry(path, meta, item));
				registered++;
			}
		}
	}

	private static void loadBlockProperties() {
		for (String line : readLines("/hbm/legacy/block-properties.tsv")) {
			String[] parts = line.split("\t", -1);
			if (parts.length >= 3) {
				BLOCK_PROPS.put(parts[0], new int[] { Integer.parseInt(parts[1]), Integer.parseInt(parts[2]) });
			}
		}
	}

	private static void loadBlockShapes() {
		try (InputStream in = ItemRegistry.class.getResourceAsStream("/hbm/legacy/block-shapes.json")) {
			if (in == null) {
				return;
			}
			JsonObject root = JsonParser.parseString(new String(in.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
			for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
				List<AABB> boxes = new ArrayList<>();
				for (JsonElement element : entry.getValue().getAsJsonArray()) {
					JsonArray pair = element.getAsJsonArray();
					JsonArray min = pair.get(0).getAsJsonArray();
					JsonArray max = pair.get(1).getAsJsonArray();
					boxes.add(new AABB(min.get(0).getAsDouble(), min.get(1).getAsDouble(), min.get(2).getAsDouble(),
							max.get(0).getAsDouble(), max.get(1).getAsDouble(), max.get(2).getAsDouble()));
				}
				BLOCK_SHAPES.put(entry.getKey(), boxes);
			}
		} catch (Exception exception) {
			MainRegistry.LOGGER.error("Failed to load block shapes", exception);
		}
	}

	private static void initializeVariants() {
		for (String line : readLines("/hbm/legacy/registry-variants.tsv")) {
			String[] parts = line.split("\t", -1);
			if (parts.length < 3) {
				continue;
			}
			String path = parts[0];
			int meta = Integer.parseInt(parts[1]);
			String idPath = parts[2].contains(":") ? parts[2].split(":", 2)[1] : parts[2];
			Identifier id = Identifier.fromNamespaceAndPath(MainRegistry.MOD_ID, idPath);
			ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
			Item item = new Item(new Item.Properties().setId(key));
			Registry.register(BuiltInRegistries.ITEM, key, item);
			BY_LEGACY.put(key(path, meta), new Entry(path, meta, item));
			registered++;
		}
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

	private static String key(String legacyPath, int meta) {
		return legacyPath + "|" + meta;
	}

	private static List<String> readLines(String resource) {
		try (InputStream stream = ItemRegistry.class.getResourceAsStream(resource)) {
			if (stream == null) {
				return List.of();
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

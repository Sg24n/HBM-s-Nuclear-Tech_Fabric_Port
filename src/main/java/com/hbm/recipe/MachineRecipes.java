package com.hbm.recipe;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hbm.main.MainRegistry;

/**
 * Raw machine-recipe data ported from the legacy dump (hbmRecipes/*.json).
 * Machines are not implemented yet, so the templates are loaded and counted but
 * not consumed.
 */
public final class MachineRecipes {

	private static final Map<String, String> RAW = new LinkedHashMap<>();
	private static final Map<String, Integer> COUNTS = new LinkedHashMap<>();

	private MachineRecipes() {
	}

	public static void load() {
		try (InputStream index = MachineRecipes.class.getResourceAsStream("/hbm/recipes/index.txt")) {
			if (index == null) {
				return;
			}
			for (String name : new String(index.readAllBytes(), StandardCharsets.UTF_8).split("\\R")) {
				name = name.trim();
				if (name.isEmpty()) {
					continue;
				}
				try (InputStream in = MachineRecipes.class.getResourceAsStream("/hbm/recipes/" + name)) {
					if (in == null) {
						continue;
					}
					String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
					RAW.put(name, json);
					JsonObject object = JsonParser.parseString(json).getAsJsonObject();
					JsonArray recipes = object.getAsJsonArray("recipes");
					COUNTS.put(name, recipes == null ? 0 : recipes.size());
				}
			}
		} catch (Exception exception) {
			MainRegistry.LOGGER.error("Failed to load machine recipes", exception);
		}
		MainRegistry.LOGGER.info("Machine recipes: {} categories, {} recipes", RAW.size(), total());
	}

	public static String raw(String category) {
		return RAW.get(category);
	}

	public static int count(String category) {
		return COUNTS.getOrDefault(category, 0);
	}

	public static Set<String> categories() {
		return RAW.keySet();
	}

	public static int total() {
		return COUNTS.values().stream().mapToInt(Integer::intValue).sum();
	}
}

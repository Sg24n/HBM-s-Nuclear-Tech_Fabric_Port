package com.hbm.recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central registry for ported HBM recipes. Recipes are data: machines are not
 * implemented yet, but the recipe sets are ported category by category.
 */
public final class HbmRecipes {

	private static final Map<String, List<Object>> BY_CATEGORY = new HashMap<>();

	private HbmRecipes() {
	}

	public static void register(String category, Object recipe) {
		BY_CATEGORY.computeIfAbsent(category, key -> new ArrayList<>()).add(recipe);
	}

	public static List<Object> get(String category) {
		return BY_CATEGORY.getOrDefault(category, List.of());
	}

	public static Map<String, List<Object>> all() {
		return BY_CATEGORY;
	}

	public static int count(String category) {
		return get(category).size();
	}
}

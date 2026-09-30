package com.hbm.recipe;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * A recipe input. Items and tags are kept unresolved (tags bind at datapack
 * load), so recipe data can be registered before the world exists; a machine
 * pass resolves these later.
 */
public sealed interface IngredientSpec {

	record OfItem(ItemStack stack) implements IngredientSpec {
	}

	record OfTag(TagKey<Item> tag) implements IngredientSpec {
	}
}

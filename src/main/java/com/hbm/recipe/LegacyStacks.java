package com.hbm.recipe;

import com.hbm.content.ItemRegistry;
import com.hbm.main.MainRegistry;
import com.hbm.material.NTMMaterial;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.Locale;

/**
 * Resolves legacy recipe references (ModItems fields, matrix metas, ore dict
 * names) into modern item stacks and ingredients.
 */
public final class LegacyStacks {

	private LegacyStacks() {
	}

	/** Legacy dedicated item: {@code ModItems.<field>} at meta 0. */
	public static ItemStack stack(String legacyPath, int count) {
		return stack(legacyPath, 0, count);
	}

	/** Legacy matrix item: {@code new ItemStack(ModItems.<field>, count, meta)}. */
	public static ItemStack stack(String legacyPath, int meta, int count) {
		Item item = ItemRegistry.get(legacyPath, meta);
		if (item == null) {
			throw new IllegalStateException("unregistered legacy item: " + legacyPath + " meta " + meta);
		}
		return new ItemStack(item, count);
	}

	/** {@code DictFrame.fromOne(item, enum)} / {@code new ItemStack(item, count, enum.ordinal())}. */
	public static ItemStack enumStack(String legacyPath, int ordinal, int count) {
		return stack(legacyPath, ordinal, count);
	}

	/** {@code Mats.MAT_X.make(ModItems.<field>)}: the matrix item for a material id. */
	public static ItemStack materialStack(String shape, int materialId, int count) {
		return stack("item." + shape, materialId, count);
	}

	public static ItemStack vanilla(ItemLike item, int count) {
		return new ItemStack(item, count);
	}

	/** Ore dictionary name -> tag, e.g. {@code ingotIron} -> {@code hbm:ingot/iron}. */
	public static IngredientSpec tag(String group, String material) {
		return new IngredientSpec.OfTag(TagKey.create(Registries.ITEM,
				Identifier.fromNamespaceAndPath(MainRegistry.MOD_ID, group + "/" + material)));
	}

	/** {@code IRON.ingot()} -> {@code hbm:ingot/iron}. */
	public static IngredientSpec tagMaterial(String group, NTMMaterial material) {
		return tag(group, slug(material.names[0]));
	}

	private static String slug(String value) {
		return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
	}

	/** Plain item ingredient. */
	public static IngredientSpec of(ItemStack stack) {
		return new IngredientSpec.OfItem(stack);
	}
}

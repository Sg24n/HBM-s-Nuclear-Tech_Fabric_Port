package com.hbm.recipe;

import com.hbm.material.Mats;
import com.hbm.material.NTMMaterial;
import com.hbm.material.MaterialShapes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * Ported press (stamp) recipes. Original: com.hbm.inventory.recipes.PressRecipes.
 * The stamp machine is not implemented yet; the recipe data is registered so a
 * later machine pass can consume it.
 */
public final class PressRecipes {

	public record Recipe(IngredientSpec input, StampType stamp, ItemStack output) {
	}

	public enum StampType {
		FLAT, PLATE, WIRE, CIRCUIT, C357, C44, C50, C9,
		PRINTING1, PRINTING2, PRINTING3, PRINTING4, PRINTING5, PRINTING6, PRINTING7, PRINTING8
	}

	public static final String CATEGORY = "press";

	private static boolean initialized;

	private PressRecipes() {
	}

	private static void add(StampType stamp, IngredientSpec input, ItemStack output) {
		HbmRecipes.register(CATEGORY, new Recipe(input, stamp, output));
	}

	public static void initialize() {
		if (initialized) {
			return;
		}
		initialized = true;
		// FLAT
		add(StampType.FLAT, LegacyStacks.tag("dust", "netherquartz"), LegacyStacks.vanilla(Items.QUARTZ, 1));
		add(StampType.FLAT, LegacyStacks.tag("dust", "lapis"), LegacyStacks.vanilla(Items.LAPIS_LAZULI, 1));
		add(StampType.FLAT, LegacyStacks.tag("dust", "diamond"), LegacyStacks.vanilla(Items.DIAMOND, 1));
		add(StampType.FLAT, LegacyStacks.tag("dust", "emerald"), LegacyStacks.vanilla(Items.EMERALD, 1));
		add(StampType.FLAT, LegacyStacks.of(LegacyStacks.stack("item.biomass", 1)), LegacyStacks.stack("item.biomass_compressed", 1));
		add(StampType.FLAT, LegacyStacks.of(LegacyStacks.stack("item.meteorite_sword_reforged", 1)), LegacyStacks.stack("item.meteorite_sword_hardened", 1));
		add(StampType.FLAT, LegacyStacks.of(new ItemStack(Items.JUNGLE_LOG)), LegacyStacks.stack("item.ball_resin", 1));
		add(StampType.FLAT, LegacyStacks.tag("dust", "coal"), LegacyStacks.enumStack("item.briquette", 0, 1));
		add(StampType.FLAT, LegacyStacks.tag("dust", "lignite"), LegacyStacks.enumStack("item.briquette", 1, 1));

		// PLATE
		add(StampType.PLATE, LegacyStacks.tagMaterial("ingot", Mats.MAT_IRON), LegacyStacks.stack("item.plate_iron", 1));
		add(StampType.PLATE, LegacyStacks.tagMaterial("ingot", Mats.MAT_GOLD), LegacyStacks.stack("item.plate_gold", 1));
		add(StampType.PLATE, LegacyStacks.tagMaterial("ingot", Mats.MAT_TITANIUM), LegacyStacks.stack("item.plate_titanium", 1));
		add(StampType.PLATE, LegacyStacks.tagMaterial("ingot", Mats.MAT_ALUMINIUM), LegacyStacks.stack("item.plate_aluminium", 1));
		add(StampType.PLATE, LegacyStacks.tagMaterial("ingot", Mats.MAT_STEEL), LegacyStacks.stack("item.plate_steel", 1));
		add(StampType.PLATE, LegacyStacks.tagMaterial("ingot", Mats.MAT_LEAD), LegacyStacks.stack("item.plate_lead", 1));
		add(StampType.PLATE, LegacyStacks.tagMaterial("ingot", Mats.MAT_COPPER), LegacyStacks.stack("item.plate_copper", 1));
		add(StampType.PLATE, LegacyStacks.tagMaterial("ingot", Mats.MAT_SCHRABIDIUM), LegacyStacks.stack("item.plate_schrabidium", 1));
		add(StampType.PLATE, LegacyStacks.tagMaterial("ingot", Mats.MAT_CMB), LegacyStacks.stack("item.plate_combine_steel", 1));
		add(StampType.PLATE, LegacyStacks.tagMaterial("ingot", Mats.MAT_GUNMETAL), LegacyStacks.stack("item.plate_gunmetal", 1));
		add(StampType.PLATE, LegacyStacks.tagMaterial("ingot", Mats.MAT_WEAPONSTEEL), LegacyStacks.stack("item.plate_weaponsteel", 1));
		add(StampType.PLATE, LegacyStacks.tagMaterial("ingot", Mats.MAT_SATURN), LegacyStacks.stack("item.plate_saturnite", 1));
		add(StampType.PLATE, LegacyStacks.tagMaterial("ingot", Mats.MAT_DURA), LegacyStacks.stack("item.plate_dura_steel", 1));

		// CASINGS
		add(StampType.C9, LegacyStacks.tagMaterial("plate", Mats.MAT_GUNMETAL), LegacyStacks.enumStack("item.casing", 0, 4));
		add(StampType.C50, LegacyStacks.tagMaterial("plate", Mats.MAT_GUNMETAL), LegacyStacks.enumStack("item.casing", 1, 2));
		add(StampType.C9, LegacyStacks.tagMaterial("plate", Mats.MAT_WEAPONSTEEL), LegacyStacks.enumStack("item.casing", 2, 4));
		add(StampType.C50, LegacyStacks.tagMaterial("plate", Mats.MAT_WEAPONSTEEL), LegacyStacks.enumStack("item.casing", 3, 2));

		// WIRE
		for (NTMMaterial material : Mats.orderedList) {
			if (material.autogen.contains(MaterialShapes.WIRE)) {
				add(StampType.WIRE, LegacyStacks.tagMaterial("ingot", material),
						LegacyStacks.materialStack("wire_fine", material.id, 8));
			}
		}

		// CIRCUIT
		add(StampType.CIRCUIT, LegacyStacks.tag("billet", "silicon"), LegacyStacks.enumStack("item.circuit", 4, 1));
	}

	public static List<Recipe> recipes() {
		return HbmRecipes.get(CATEGORY).stream().map(Recipe.class::cast).toList();
	}
}

package com.hbm.test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemModelMappingTest {

	private static final Path DATA = Path.of("data", "legacy");
	private static final Path ITEM_MODELS = DATA.resolve("item-models.tsv");
	private static final Path ITEM_MODELS_SOURCE = DATA.resolve("item-models-source.tsv");
	private static final Path ITEM_MODELS_RUNTIME = DATA.resolve("item-models-runtime-source.tsv");
	private static final Path CREATIVE = Path.of("src", "main", "resources", "hbm", "legacy", "creative.tsv");
	private static final Path DUMP = DATA.resolve("legacy-items.json");
	private static final Path ASSETS = Path.of("src", "main", "resources", "assets", "hbm");

	// Creative stacks with a custom IItemRenderer that draws no OBJ (flat 2D icon
	// transformers / inventory-only glow) or renders a block (conveyor wand). These
	// are intentionally not mapped to an item OBJ model.
	private static final Set<String> NON_OBJ_RENDERERS = Set.of(
			"hbm:item.alloy_sword", "hbm:item.bismuth_axe", "hbm:item.bismuth_pickaxe",
			"hbm:item.blade_meteorite", "hbm:item.chlorophyte_axe", "hbm:item.chlorophyte_pickaxe",
			"hbm:item.cmb_sword", "hbm:item.cobalt_decorated_sword", "hbm:item.cobalt_sword",
			"hbm:item.conveyor_wand", "hbm:item.desh_sword", "hbm:item.dnt_sword",
			"hbm:item.ingot_chainsteel", "hbm:item.ingot_meteorite", "hbm:item.ingot_meteorite_forged",
			"hbm:item.ingot_steel_dusted", "hbm:item.mese_axe", "hbm:item.mese_pickaxe",
			"hbm:item.meteorite_sword_alloyed", "hbm:item.meteorite_sword_baleful",
			"hbm:item.meteorite_sword_bred", "hbm:item.meteorite_sword_etched",
			"hbm:item.meteorite_sword_fused", "hbm:item.meteorite_sword_hardened",
			"hbm:item.meteorite_sword_irradiated", "hbm:item.meteorite_sword_machined",
			"hbm:item.meteorite_sword_reforged", "hbm:item.meteorite_sword_seared",
			"hbm:item.meteorite_sword_treated", "hbm:item.schrabidium_sword", "hbm:item.starmetal_sword",
			"hbm:item.steel_sword", "hbm:item.titanium_sword", "hbm:item.volcanic_axe",
			"hbm:item.volcanic_pickaxe");

	private record Layer(String obj, String texture, List<String> parts) {
	}

	private record Mapping(String key, List<Layer> layers, boolean composite) {
	}

	private static List<Mapping> mappings;
	private static Map<String, String> creativeModel;

	@BeforeAll
	static void load() throws IOException {
		Assumptions.assumeTrue(Files.exists(ITEM_MODELS) && Files.exists(CREATIVE), "item-model data missing");
		Gson gson = new Gson();
		mappings = new ArrayList<>();
		for (Path file : List.of(ITEM_MODELS, ITEM_MODELS_SOURCE)) {
			if (!Files.exists(file)) {
				continue;
			}
			for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
				if (line.isBlank()) {
					continue;
				}
				String[] fields = line.split("\\t", -1);
				assertTrue(fields.length == 6 || fields.length == 7, "item-model columns in " + file);
				String key = key(fields[0], fields[1], fields[2], fields[3]);
				List<String> parts = fields.length > 6 && !fields[6].isBlank() ? List.of(fields[6]) : List.of();
				mappings.add(new Mapping(key, List.of(new Layer(fields[4], fields[5], parts)), false));
			}
		}
		if (Files.exists(ITEM_MODELS_RUNTIME)) {
			for (String line : Files.readAllLines(ITEM_MODELS_RUNTIME, StandardCharsets.UTF_8)) {
				if (line.isBlank()) {
					continue;
				}
				String[] fields = line.split("\\t", 5);
				assertEquals(5, fields.length, "runtime item-model columns");
				String key = key(fields[0], fields[1], fields[2], fields[3]);
				JsonObject spec = gson.fromJson(fields[4], JsonObject.class);
				String type = spec.get("type").getAsString();
				List<Layer> layers = new ArrayList<>();
				boolean composite = type.equals("composite");
				if (composite) {
					JsonArray values = spec.getAsJsonArray("layers");
					values.forEach(value -> layers.add(layer(value.getAsJsonObject())));
				} else {
					assertEquals("obj", type, "runtime model type");
					layers.add(layer(spec));
				}
				mappings.add(new Mapping(key, List.copyOf(layers), composite));
			}
		}

		creativeModel = new HashMap<>();
		for (String line : Files.readAllLines(CREATIVE, StandardCharsets.UTF_8)) {
			if (line.isBlank()) {
				continue;
			}
			String[] fields = line.split("\\t", -1);
			if (fields.length < 6) {
				continue;
			}
			creativeModel.put(key(fields[0], "hbm:" + fields[2], fields[3], fields[4]), fields[5]);
		}
	}

	@Test
	void keysAreUniqueAndMatchCreativeStacks() {
		Set<String> keys = new HashSet<>();
		for (Mapping mapping : mappings) {
			assertTrue(keys.add(mapping.key()), "duplicate item-models key: " + mapping.key());
			assertTrue(creativeModel.containsKey(mapping.key()), "no creative stack for " + mapping.key());
		}
		assertFalse(mappings.isEmpty(), "no item-model mappings");
		int mappedStacks = 0;
		for (Map.Entry<String, String> entry : creativeModel.entrySet()) {
			if (entry.getValue().startsWith("render/") && entry.getValue().contains("__")) {
				assertTrue(keys.contains(entry.getKey()), "select stack without mapping: " + entry.getKey());
				mappedStacks++;
			}
		}
		assertEquals(mappings.size(), mappedStacks, "mapped creative stacks");
	}

	@Test
	void resourcesExist() {
		for (Mapping mapping : mappings) {
			assertFalse(mapping.layers().isEmpty(), "empty model layers: " + mapping.key());
			for (Layer layer : mapping.layers()) {
				assertTrue(Files.exists(ASSETS.resolve(layer.obj())), "missing OBJ resource: " + layer.obj());
				assertTrue(Files.exists(ASSETS.resolve("textures").resolve(layer.texture() + ".png")),
						"missing texture resource: " + layer.texture());
			}
		}
	}

	@Test
	void rendererStacksAreMappedOrKnownNonObj() throws IOException {
		Assumptions.assumeTrue(Files.exists(DUMP), "legacy dump missing");
		Set<String> mappedIdMeta = new HashSet<>();
		for (Mapping mapping : mappings) {
			String[] parts = mapping.key().split("\\|", -1);
			mappedIdMeta.add(parts[1] + "|" + parts[2]);
		}
		JsonObject dump = new Gson().fromJson(Files.readString(DUMP, StandardCharsets.UTF_8), JsonObject.class);
		int renderer = 0;
		int unmapped = 0;
		for (var element : dump.getAsJsonArray("items")) {
			JsonObject item = element.getAsJsonObject();
			if (!item.has("renderer") || !item.get("renderer").getAsBoolean()) {
				continue;
			}
			String id = item.get("id").getAsString();
			if (!id.startsWith("hbm:")) {
				continue;
			}
			renderer++;
			if (!mappedIdMeta.contains(id + "|" + item.get("meta").getAsInt())) {
				unmapped++;
				assertTrue(NON_OBJ_RENDERERS.contains(id), "unmapped custom renderer without OBJ classification: " + id);
			}
		}
		assertTrue(renderer > 0, "no custom-renderer stacks found");
		assertTrue(unmapped > 0, "expected some known non-OBJ renderers");
	}

	@Test
	void capturedRenderDefinitionsAreNotBlank() throws IOException {
		Path dir = ASSETS.resolve("items").resolve("render");
		Assumptions.assumeTrue(Files.isDirectory(dir), "no render definitions");
		Gson gson = new Gson();
		Set<String> seen = new HashSet<>();
		int checked = 0;
		int repaired = 0;
		for (Path file : Files.newDirectoryStream(dir, "*.json")) {
			JsonObject model = gson.fromJson(Files.readString(file, StandardCharsets.UTF_8), JsonObject.class)
					.getAsJsonObject("model");
			if (model == null || !model.has("model")) {
				continue;
			}
			String ref = model.get("model").getAsString();
			if (!ref.startsWith("hbm:render/")) {
				repaired++;
				continue;
			}
			String hash = ref.substring("hbm:render/".length());
			if (!seen.add(hash)) {
				continue;
			}
			Path png = ASSETS.resolve("textures").resolve("item").resolve("render").resolve(hash + ".png");
			assertTrue(Files.exists(png), "missing render PNG: " + png);
			BufferedImage image = ImageIO.read(png.toFile());
			assertTrue(image != null, "unreadable render PNG: " + png);
			boolean opaque = false;
			for (int y = 0; y < image.getHeight() && !opaque; y++) {
				for (int x = 0; x < image.getWidth(); x++) {
					if ((image.getRGB(x, y) >>> 24) > 0) {
						opaque = true;
						break;
					}
				}
			}
			assertTrue(opaque, "blank captured render: " + png);
			checked++;
		}
		assertTrue(checked > 0, "no captured renders checked");
		assertTrue(repaired > 0, "expected repaired definitions");
	}

	@Test
	void mappedStacksSelectCaptureInGuiAndObjInHand() throws IOException {
		Gson gson = new Gson();
		for (Mapping mapping : mappings) {
			String itemModel = creativeModel.get(mapping.key());
			assertTrue(itemModel != null && itemModel.startsWith("render/") && itemModel.contains("__"),
					"expected select definition for " + mapping.key() + " but got " + itemModel);
			Path definition = ASSETS.resolve("items").resolve(itemModel + ".json");
			assertTrue(Files.exists(definition), "missing item definition: " + definition);
			JsonObject select = gson.fromJson(Files.readString(definition, StandardCharsets.UTF_8), JsonObject.class)
					.getAsJsonObject("model");
			assertEquals("minecraft:select", select.get("type").getAsString(), "definition type: " + itemModel);
			assertEquals("minecraft:display_context", select.get("property").getAsString(),
					"definition property: " + itemModel);
			assertEquals("gui", select.getAsJsonArray("cases").get(0).getAsJsonObject().get("when").getAsString(),
					"gui case: " + itemModel);
			assertTrue(select.getAsJsonArray("cases").get(0).getAsJsonObject().getAsJsonObject("model")
					.get("model").getAsString().startsWith("hbm:render/"), "gui model: " + itemModel);

			JsonObject fallback = select.getAsJsonObject("fallback");
			if (mapping.composite()) {
				assertEquals("minecraft:composite", fallback.get("type").getAsString(), "fallback type: " + itemModel);
				JsonArray models = fallback.getAsJsonArray("models");
				assertEquals(mapping.layers().size(), models.size(), "fallback layer count: " + itemModel);
				for (int i = 0; i < models.size(); i++) {
					String model = models.get(i).getAsJsonObject().get("model").getAsString();
					assertTrue(model.startsWith("hbm:obj/"), "composite model reference: " + model);
					assertLayer(mapping.layers().get(i), model.substring("hbm:".length()), gson);
				}
			} else {
				assertEquals("minecraft:model", fallback.get("type").getAsString(), "fallback type: " + itemModel);
				String model = fallback.get("model").getAsString();
				assertTrue(model.startsWith("hbm:obj/"), "fallback model: " + itemModel);
				assertLayer(mapping.layers().get(0), model.substring("hbm:".length()), gson);
			}
		}
	}

	@Test
	void unmappedStacksStayOnPlainCapture() throws IOException {
		Set<String> mappedKeys = new HashSet<>();
		for (Mapping mapping : mappings) {
			mappedKeys.add(mapping.key());
		}
		int checked = 0;
		for (Map.Entry<String, String> entry : creativeModel.entrySet()) {
			String model = entry.getValue();
			if (!model.startsWith("render/") || model.contains("__") || mappedKeys.contains(entry.getKey())) {
				continue;
			}
			Path definition = ASSETS.resolve("items").resolve(model + ".json");
			assertTrue(Files.exists(definition), "missing capture definition: " + definition);
			String body = Files.readString(definition, StandardCharsets.UTF_8);
			assertFalse(body.contains("minecraft:select"), "unmapped capture must keep plain model: " + model);
			checked++;
		}
		assertTrue(checked > 0, "no unmapped capture stacks checked");
	}

	private static Layer layer(JsonObject object) {
		List<String> parts = new ArrayList<>();
		if (object.has("parts")) {
			object.getAsJsonArray("parts").forEach(value -> parts.add(value.getAsString()));
		}
		return new Layer(object.get("obj").getAsString(), object.get("texture").getAsString(), List.copyOf(parts));
	}

	private static void assertLayer(Layer expected, String modelPath, Gson gson) throws IOException {
		Path definition = ASSETS.resolve("models").resolve(modelPath + ".json");
		assertTrue(Files.exists(definition), "missing OBJ model definition: " + definition);
		JsonObject object = gson.fromJson(Files.readString(definition, StandardCharsets.UTF_8), JsonObject.class);
		assertEquals("hbm:obj", object.get("fabric:type").getAsString(), "custom model type: " + definition);
		assertEquals("hbm:" + expected.obj(), object.get("obj").getAsString(), "OBJ reference: " + definition);
		assertEquals("hbm:" + expected.texture(), object.get("texture").getAsString(),
				"texture reference: " + definition);
		Set<String> actualParts = new HashSet<>();
		if (object.has("parts")) {
			object.getAsJsonArray("parts").forEach(value -> actualParts.add(value.getAsString()));
		}
		assertEquals(new HashSet<>(expected.parts()), actualParts, "OBJ group selection: " + definition);
	}

	private static String key(String tab, String id, String meta, String display) {
		return tab + "|" + id + "|" + meta + "|" + display;
	}
}

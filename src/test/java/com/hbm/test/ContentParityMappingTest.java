package com.hbm.test;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContentParityMappingTest {

	private static final Path MANIFEST = Path.of("data", "legacy", "legacy-items.json");
	private static final Path IDENTITY = Path.of("data", "legacy", "identity-map.json");
	private static final Set<String> COMPONENTS = Set.of(
			"hbm:charge", "hbm:fluid_id", "hbm:cart_base", "hbm:blueprint",
			"hbm:icf_pellet", "hbm:grenade", "hbm:bedrock_ore", "hbm:missile_custom");

	@BeforeAll
	static void requireData() {
		Assumptions.assumeTrue(Files.exists(MANIFEST) && Files.exists(IDENTITY), "legacy data missing");
	}

	@Test
	void everyNonBlockStackIsMapped() throws Exception {
		Gson gson = new Gson();
		JsonObject manifest = gson.fromJson(Files.readString(MANIFEST, StandardCharsets.UTF_8), JsonObject.class);
		JsonObject mappings = gson.fromJson(Files.readString(IDENTITY, StandardCharsets.UTF_8), JsonObject.class)
				.getAsJsonObject("mappings");

		int checked = 0;
		int unmappedBlocks = 0;
		for (JsonElement element : manifest.getAsJsonArray("items")) {
			JsonObject row = element.getAsJsonObject();
			String id = row.get("id").getAsString();
			if (id.startsWith("hbm:tile.")) {
				unmappedBlocks++;
				continue;
			}
			int meta = row.has("meta") ? row.get("meta").getAsInt() : 0;
			String nbt = row.has("nbt") && !row.get("nbt").isJsonNull() ? row.get("nbt").getAsString() : null;
			String key = nbt == null ? id + "|" + meta : id + "|" + meta + "|" + nbt;
			assertTrue(mappings.has(key), "unmapped non-block stack: " + key);
			checked++;
		}
		assertTrue(checked > 4000, "expected most stacks mapped, got " + checked);
		assertTrue(unmappedBlocks > 0, "blocks are expected to still be pending");
	}

	@Test
	void profileMappingsCarryKnownComponents() throws Exception {
		JsonObject mappings = new Gson().fromJson(
				Files.readString(IDENTITY, StandardCharsets.UTF_8), JsonObject.class).getAsJsonObject("mappings");

		int profiles = 0;
		for (Map.Entry<String, JsonElement> entry : mappings.entrySet()) {
			if (!entry.getKey().startsWith("hbm:item.")) {
				continue;
			}
			if (entry.getKey().indexOf('|') == entry.getKey().lastIndexOf('|')) {
				continue;
			}
			JsonObject value = entry.getValue().getAsJsonObject();
			assertTrue(value.has("id"), "profile mapping without id: " + entry.getKey());
			JsonObject components = value.getAsJsonObject("components");
			assertNotNull(components, "profile mapping without components: " + entry.getKey());
			for (String name : components.keySet()) {
				assertTrue(COMPONENTS.contains(name), "unknown component " + name + " in " + entry.getKey());
			}
			profiles++;
		}
		assertEquals(690, profiles, "expected 690 NBT-profile mappings");
	}

	@Test
	void vanillaStacksAreMarkedVanilla() throws Exception {
		JsonObject mappings = new Gson().fromJson(
				Files.readString(IDENTITY, StandardCharsets.UTF_8), JsonObject.class).getAsJsonObject("mappings");
		long vanity = mappings.entrySet().stream()
				.filter(entry -> entry.getValue().isJsonObject())
				.filter(entry -> entry.getValue().getAsJsonObject().has("vanilla"))
				.count();
		assertTrue(vanity >= 583, "expected the vanilla creative stacks to be marked, got " + vanity);
	}
}

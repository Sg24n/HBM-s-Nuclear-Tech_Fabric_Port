package com.hbm.test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.fail;

class LegacyContentParityTest {

	private static final Path DATA = Path.of("data", "legacy");
	private static final Path MANIFEST = DATA.resolve("legacy-items.json");
	private static final Path IDENTITY_MAP = DATA.resolve("identity-map.json");
	private static final Path COVERAGE = Path.of("data", "coverage.json");
	private static final Path REPORT = Path.of("build", "reports", "content-coverage.md");

	@Test
	void legacyContentIsFullyMapped() throws IOException {
		Assumptions.assumeTrue(Files.exists(MANIFEST), "legacy dump missing: run tools/legacy-dump first");

		Gson gson = new Gson();
		JsonObject manifest = read(gson, MANIFEST);
		JsonObject identityMap = read(gson, IDENTITY_MAP);
		JsonObject coverage = read(gson, COVERAGE);

		Map<String, JsonObject> mappings = new HashMap<>();
		for (Map.Entry<String, JsonElement> entry : identityMap.getAsJsonObject("mappings").entrySet()) {
			if (entry.getValue().isJsonObject()) {
				mappings.put(entry.getKey(), entry.getValue().getAsJsonObject());
			}
		}

		List<Entry> entries = new ArrayList<>();
		for (JsonElement element : manifest.getAsJsonArray("items")) {
			JsonObject object = element.getAsJsonObject();
			String id = object.get("id").getAsString();
			int meta = object.has("meta") ? object.get("meta").getAsInt() : 0;
			String nbt = object.has("nbt") && !object.get("nbt").isJsonNull() ? object.get("nbt").getAsString() : null;
			String cls = object.has("class") ? object.get("class").getAsString() : "";
			String key = nbt == null ? id + "|" + meta : id + "|" + meta + "|" + nbt;
			entries.add(new Entry(key, id, meta, cls));
		}

		StringBuilder report = new StringBuilder();
		report.append("# Legacy Content Coverage\n\n");
		report.append("- legacy creative stacks: ").append(entries.size()).append('\n');
		report.append("- distinct item ids: ").append(entries.stream().map(entry -> entry.id).distinct().count()).append('\n');
		long mapped = entries.stream().filter(entry -> mappings.containsKey(entry.key)).count();
		report.append("- mapped: ").append(mapped).append('\n');
		report.append("- unmapped: ").append(entries.size() - mapped).append("\n\n");

		List<String> failures = new ArrayList<>();
		JsonArray categories = coverage.getAsJsonArray("categories");
		for (JsonElement categoryElement : categories) {
			JsonObject category = categoryElement.getAsJsonObject();
			String name = category.get("name").getAsString();
			boolean locked = category.has("locked") && category.get("locked").getAsBoolean();
			String status = category.has("status") ? category.get("status").getAsString() : (locked ? "locked" : "pending");
			List<Entry> selected = select(entries, category);
			long categoryMapped = selected.stream().filter(entry -> mappings.containsKey(entry.key)).count();
			long categoryUnmapped = selected.size() - categoryMapped;

			report.append("## ").append(name).append(" (").append(status).append(")\n\n");
			report.append("- total: ").append(selected.size())
					.append(", mapped: ").append(categoryMapped)
					.append(", unmapped: ").append(categoryUnmapped).append('\n');

			if (categoryUnmapped > 0) {
				Map<String, Long> byClass = new TreeMap<>();
				selected.stream().filter(entry -> !mappings.containsKey(entry.key))
						.forEach(entry -> byClass.merge(entry.cls, 1L, Long::sum));
				report.append("- top unmapped classes:\n");
				byClass.entrySet().stream()
						.sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
						.limit(10)
						.forEach(entry -> report.append("  - ").append(entry.getKey()).append(": ").append(entry.getValue()).append('\n'));
			}
			report.append('\n');

			if (locked && categoryUnmapped > 0) {
				failures.add(name + ": " + categoryUnmapped + " unmapped of " + selected.size());
			}
		}

		Files.createDirectories(REPORT.getParent());
		Files.writeString(REPORT, report.toString(), StandardCharsets.UTF_8);

		if (!failures.isEmpty()) {
			fail("locked categories incomplete: " + failures);
		}
	}

	private static List<Entry> select(List<Entry> entries, JsonObject category) {
		JsonObject selector = category.has("selector") ? category.getAsJsonObject("selector") : new JsonObject();
		String kind = selector.has("kind") ? selector.get("kind").getAsString() : "all";
		List<Entry> selected = new ArrayList<>();
		switch (kind) {
			case "idPrefix": {
				String prefix = selector.get("value").getAsString();
				for (Entry entry : entries) {
					if (entry.id.startsWith(prefix)) {
						selected.add(entry);
					}
				}
				break;
			}
			case "classPrefix": {
				String prefix = selector.get("value").getAsString();
				for (Entry entry : entries) {
					if (entry.cls.startsWith(prefix)) {
						selected.add(entry);
					}
				}
				break;
			}
			default:
				selected.addAll(entries);
		}
		return selected;
	}

	private static JsonObject read(Gson gson, Path path) throws IOException {
		return gson.fromJson(Files.readString(path, StandardCharsets.UTF_8), JsonObject.class);
	}

	private record Entry(String key, String id, int meta, String cls) {
	}
}

package com.hbm.client.model;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.joml.Vector3f;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedModelDeserializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;

public final class ObjUnbakedModel implements UnbakedModel {

	private static final Map<String, ObjModel> CACHE = new ConcurrentHashMap<>();

	private final List<ObjCompositeGeometry.Layer> layers;
	private final com.mojang.blaze3d.platform.Transparency transparency;
	private final Vector3f translate;
	private final ItemTransforms transforms;

	public ObjUnbakedModel(List<ObjCompositeGeometry.Layer> layers, com.mojang.blaze3d.platform.Transparency transparency,
			Vector3f translate, ItemTransforms transforms) {
		this.layers = layers;
		this.transparency = transparency;
		this.translate = translate;
		this.transforms = transforms;
	}

	@Override
	public TextureSlots.Data textureSlots() {
		TextureSlots.Data.Builder builder = new TextureSlots.Data.Builder();
		for (int index = 0; index < layers.size(); index++) {
			builder.addTexture("#" + index, new Material(layers.get(index).texture()));
		}
		builder.addTexture("particle", new Material(layers.get(0).texture()));
		return builder.build();
	}

	@Override
	public UnbakedGeometry geometry() {
		return new ObjCompositeGeometry(layers, transparency, translate);
	}

	@Override
	public ItemTransforms transforms() {
		return transforms;
	}

	private static ItemTransform transform(float rx, float ry, float rz, float tx, float ty, float tz, float scale) {
		return new ItemTransform(new Vector3f(rx, ry, rz),
				new Vector3f(tx / 16.0F, ty / 16.0F, tz / 16.0F), new Vector3f(scale, scale, scale));
	}

	private static ItemTransforms defaultTransforms() {
		ItemTransform thirdPerson = transform(75.0F, 45.0F, 0.0F, 0.0F, 2.5F, 0.0F, 0.375F);
		ItemTransform firstPerson = transform(0.0F, 45.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4F);
		ItemTransform head = transform(0.0F, 180.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F);
		ItemTransform gui = transform(30.0F, 225.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.625F);
		ItemTransform ground = transform(0.0F, 0.0F, 0.0F, 0.0F, 3.0F, 0.0F, 0.25F);
		ItemTransform fixed = transform(0.0F, 180.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.5F);
		return new ItemTransforms(thirdPerson, thirdPerson, firstPerson, firstPerson, head, gui, ground, fixed, fixed);
	}

	private static ItemTransforms display(JsonObject json) {
		ItemTransforms defaults = defaultTransforms();
		if (!json.has("display")) {
			return defaults;
		}
		JsonObject display = json.getAsJsonObject("display");
		ItemTransform thirdRight = context(display, "thirdperson_righthand", defaults.thirdPersonRightHand());
		ItemTransform thirdLeft = context(display, "thirdperson_lefthand", defaults.thirdPersonLeftHand());
		ItemTransform firstRight = context(display, "firstperson_righthand", defaults.firstPersonRightHand());
		ItemTransform firstLeft = context(display, "firstperson_lefthand", defaults.firstPersonLeftHand());
		ItemTransform head = context(display, "head", defaults.head());
		ItemTransform gui = context(display, "gui", defaults.gui());
		ItemTransform ground = context(display, "ground", defaults.ground());
		ItemTransform fixed = context(display, "fixed", defaults.fixed());
		ItemTransform fixedBottom = context(display, "fixed_from_bottom", defaults.fixedFromBottom());
		return new ItemTransforms(thirdLeft, thirdRight, firstLeft, firstRight, head, gui, ground, fixed, fixedBottom);
	}

	private static ItemTransform context(JsonObject display, String name, ItemTransform fallback) {
		if (!display.has(name)) {
			return fallback;
		}
		JsonObject value = display.getAsJsonObject(name);
		Vector3f rotation = vector(value, "rotation", fallback.rotation(), 1.0F);
		Vector3f translation = vector(value, "translation", fallback.translation(), 1.0F / 16.0F);
		Vector3f scale = vector(value, "scale", fallback.scale(), 1.0F);
		return new ItemTransform(rotation, translation, scale);
	}

	private static Vector3f vector(JsonObject value, String key, org.joml.Vector3fc fallback, float factor) {
		if (!value.has(key)) {
			return new Vector3f(fallback);
		}
		JsonArray array = value.getAsJsonArray(key);
		return new Vector3f(array.get(0).getAsFloat() * factor, array.get(1).getAsFloat() * factor,
				array.get(2).getAsFloat() * factor);
	}

	public static ObjModel load(String path, boolean normalize) {
		return CACHE.computeIfAbsent(path + "|" + normalize, key -> {
			String[] parts = path.split(":", 2);
			String namespace = parts.length > 1 ? parts[0] : "hbm";
			String resource = parts.length > 1 ? parts[1] : parts[0];
			Path file = FabricLoader.getInstance().getModContainer(namespace)
					.flatMap(container -> container.findPath("assets/" + namespace + "/" + resource))
					.orElseThrow(() -> new IllegalStateException("missing OBJ resource " + path));
			try (Reader reader = Files.newBufferedReader(file)) {
				return ObjModel.parse(reader, normalize);
			} catch (IOException exception) {
				throw new IllegalStateException("failed to parse OBJ resource " + path, exception);
			}
		});
	}

	private static List<String> readParts(JsonObject json) {
		List<String> parts = new ArrayList<>();
		if (json.has("parts")) {
			for (JsonElement element : json.getAsJsonArray("parts")) {
				parts.add(element.getAsString());
			}
		}
		return parts;
	}

	public static final class Deserializer implements UnbakedModelDeserializer {

		@Override
		public UnbakedModel deserialize(JsonObject json, JsonDeserializationContext context) {
			boolean normalize = !json.has("normalize") || json.get("normalize").getAsBoolean();
			float defaultScale = json.has("scale") ? json.get("scale").getAsFloat() : 1.0F;
			List<ObjCompositeGeometry.Layer> layers = new ArrayList<>();
			if (json.has("layers")) {
				for (JsonElement element : json.getAsJsonArray("layers")) {
					JsonObject layer = element.getAsJsonObject();
					float scale = layer.has("scale") ? layer.get("scale").getAsFloat() : defaultScale;
					layers.add(new ObjCompositeGeometry.Layer(
							load(layer.get("obj").getAsString(), normalize),
							Identifier.parse(layer.get("texture").getAsString()),
							List.copyOf(readParts(layer)), scale));
				}
			} else {
				layers.add(new ObjCompositeGeometry.Layer(
						load(json.get("obj").getAsString(), normalize),
						Identifier.parse(json.get("texture").getAsString()),
						List.copyOf(readParts(json)), defaultScale));
			}
			return new ObjUnbakedModel(List.copyOf(layers), transparency(json), translate(json), display(json));
		}
	}

	private static Vector3f translate(JsonObject json) {
		if (!json.has("translate")) {
			return new Vector3f();
		}
		JsonArray array = json.getAsJsonArray("translate");
		return new Vector3f(array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat());
	}

	private static com.mojang.blaze3d.platform.Transparency transparency(JsonObject json) {
		String alpha = json.has("alpha") ? json.get("alpha").getAsString() : "transparent";
		return switch (alpha) {
			case "translucent" -> com.mojang.blaze3d.platform.Transparency.TRANSLUCENT;
			case "both" -> com.mojang.blaze3d.platform.Transparency.TRANSPARENT_AND_TRANSLUCENT;
			case "none" -> com.mojang.blaze3d.platform.Transparency.NONE;
			default -> com.mojang.blaze3d.platform.Transparency.TRANSPARENT;
		};
	}
}

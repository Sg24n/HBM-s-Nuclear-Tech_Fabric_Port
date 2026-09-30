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
	private static final ItemTransforms TRANSFORMS = defaultTransforms();

	private final ObjModel model;
	private final Identifier texture;
	private final List<String> parts;

	public ObjUnbakedModel(ObjModel model, Identifier texture, List<String> parts) {
		this.model = model;
		this.texture = texture;
		this.parts = parts;
	}

	@Override
	public TextureSlots.Data textureSlots() {
		Material material = new Material(texture);
		return new TextureSlots.Data.Builder().addTexture("#0", material).addTexture("particle", material).build();
	}

	@Override
	public UnbakedGeometry geometry() {
		return new ObjGeometry(model, texture, parts);
	}

	@Override
	public ItemTransforms transforms() {
		return TRANSFORMS;
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

	public static ObjModel load(String path) {
		return load(path, true);
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

	public static final class Deserializer implements UnbakedModelDeserializer {

		@Override
		public UnbakedModel deserialize(JsonObject json, JsonDeserializationContext context) {
			String obj = json.get("obj").getAsString();
			Identifier texture = Identifier.parse(json.get("texture").getAsString());
			boolean normalize = !json.has("normalize") || json.get("normalize").getAsBoolean();
			List<String> parts = new ArrayList<>();
			if (json.has("parts")) {
				for (JsonElement element : json.getAsJsonArray("parts")) {
					parts.add(element.getAsString());
				}
			}
			return new ObjUnbakedModel(load(obj, normalize), texture, parts);
		}
	}
}

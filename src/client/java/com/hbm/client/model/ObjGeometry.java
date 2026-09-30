package com.hbm.client.model;

import java.util.List;

import org.joml.Vector3f;

import com.mojang.blaze3d.platform.Transparency;

import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

public final class ObjGeometry implements UnbakedGeometry {

	private final ObjModel model;
	private final Identifier texture;
	private final List<String> parts;

	public ObjGeometry(ObjModel model, Identifier texture, List<String> parts) {
		this.model = model;
		this.texture = texture;
		this.parts = parts;
	}

	private static Direction direction(ObjModel.Face face) {
		Vector3f normal = new Vector3f(face.p1()).sub(face.p0()).cross(new Vector3f(face.p2()).sub(face.p0()));
		float x = Math.abs(normal.x);
		float y = Math.abs(normal.y);
		float z = Math.abs(normal.z);
		if (x < 1.0E-9F && y < 1.0E-9F && z < 1.0E-9F) {
			return Direction.UP;
		}
		if (x >= y && x >= z) {
			return normal.x > 0.0F ? Direction.EAST : Direction.WEST;
		}
		if (y >= z) {
			return normal.y > 0.0F ? Direction.UP : Direction.DOWN;
		}
		return normal.z > 0.0F ? Direction.SOUTH : Direction.NORTH;
	}

	@Override
	public QuadCollection bake(TextureSlots slots, ModelBaker baker, ModelState state, ModelDebugName name) {
		Material.Baked baked = baker.materials().get(new Material(texture), name);
		BakedQuad.MaterialInfo info = BakedQuad.MaterialInfo.of(baked, Transparency.TRANSPARENT, -1, null, 0);
		TextureAtlasSprite sprite = baked.sprite();
		QuadCollection.Builder builder = new QuadCollection.Builder();
		for (ObjModel.Face face : model.faces()) {
			if (!parts.isEmpty() && !parts.contains(face.group())) {
				continue;
			}
			Direction direction = direction(face);
			builder.addCulledFace(direction, new BakedQuad(face.p0(), face.p1(), face.p2(), face.p3(),
					UVPair.pack(sprite.getU(face.u0()), sprite.getV(face.v0())),
					UVPair.pack(sprite.getU(face.u1()), sprite.getV(face.v1())),
					UVPair.pack(sprite.getU(face.u2()), sprite.getV(face.v2())),
					UVPair.pack(sprite.getU(face.u3()), sprite.getV(face.v3())), direction, info));
		}
		QuadCollection collection = builder.build();
		com.hbm.main.MainRegistry.LOGGER.info("OBJ bake {}: {} quads, sprite={}", texture,
				collection.getAll().size(), sprite);
		return collection;
	}
}

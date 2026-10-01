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
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

/** Bakes one or more OBJ layers (each with its own texture, parts and scale). */
public final class ObjCompositeGeometry implements UnbakedGeometry {

	public record Layer(ObjModel model, Identifier texture, List<String> parts, float scale) {
	}

	private final List<Layer> layers;
	private final Transparency transparency;
	private final Vector3f translate;

	public ObjCompositeGeometry(List<Layer> layers, Transparency transparency, Vector3f translate) {
		this.layers = layers;
		this.transparency = transparency;
		this.translate = translate;
	}

	private static Direction direction(Vector3f p0, Vector3f p1, Vector3f p2) {
		Vector3f normal = new Vector3f(p1).sub(p0).cross(new Vector3f(p2).sub(p0));
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

	private Vector3f place(Vector3f p, float factor) {
		return new Vector3f(p.x * factor + translate.x, p.y * factor + translate.y, p.z * factor + translate.z);
	}

	@Override
	public QuadCollection bake(TextureSlots slots, ModelBaker baker, ModelState state, ModelDebugName name) {
		org.joml.Matrix4fc matrix = state.transformation().getMatrix();
		QuadCollection.Builder builder = new QuadCollection.Builder();
		for (Layer layer : layers) {
			Material.Baked baked = baker.materials().get(new Material(layer.texture()), name);
			BakedQuad.MaterialInfo info = BakedQuad.MaterialInfo.of(baked, transparency, -1, null, 0);
			var sprite = baked.sprite();
			float factor = layer.scale();
			for (ObjModel.Face face : layer.model().faces()) {
				if (!layer.parts().isEmpty() && !layer.parts().contains(face.group())) {
					continue;
				}
				Vector3f p0 = matrix.transformPosition(place(face.p0(), factor), new Vector3f());
				Vector3f p1 = matrix.transformPosition(place(face.p1(), factor), new Vector3f());
				Vector3f p2 = matrix.transformPosition(place(face.p2(), factor), new Vector3f());
				Vector3f p3 = matrix.transformPosition(place(face.p3(), factor), new Vector3f());
				Direction faceDirection = direction(p0, p1, p2);
				builder.addUnculledFace(new BakedQuad(p0, p1, p2, p3,
						UVPair.pack(sprite.getU(face.u0()), sprite.getV(face.v0())),
						UVPair.pack(sprite.getU(face.u1()), sprite.getV(face.v1())),
						UVPair.pack(sprite.getU(face.u2()), sprite.getV(face.v2())),
						UVPair.pack(sprite.getU(face.u3()), sprite.getV(face.v3())), faceDirection, info));
			}
		}
		return builder.build();
	}
}

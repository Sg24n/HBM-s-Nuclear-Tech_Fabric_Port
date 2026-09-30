package com.hbm.client.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

public final class ObjModel {

	public record Face(Vector3f p0, Vector3f p1, Vector3f p2, Vector3f p3,
			float u0, float v0, float u1, float v1, float u2, float v2, float u3, float v3, String group) {
	}

	private record Corner(int position, int uv) {
	}

	private record Polygon(List<Corner> corners, String group) {
	}

	private final List<Face> faces;

	private ObjModel(List<Face> faces) {
		this.faces = faces;
	}

	public List<Face> faces() {
		return faces;
	}

	public static ObjModel parse(Reader reader) throws IOException {
		List<Vector3f> positions = new ArrayList<>();
		List<float[]> uvs = new ArrayList<>();
		List<Polygon> polygons = new ArrayList<>();
		String group = "";
		BufferedReader buffered = new BufferedReader(reader);
		String line;
		while ((line = buffered.readLine()) != null) {
			line = line.trim();
			if (line.isEmpty() || line.charAt(0) == '#') {
				continue;
			}
			String[] parts = line.split("\\s+");
			switch (parts[0]) {
				case "v" -> positions.add(new Vector3f(value(parts[1]), value(parts[2]), value(parts[3])));
				case "vt" -> uvs.add(new float[] { value(parts[1]), parts.length > 2 ? value(parts[2]) : 0.0F });
				case "o", "g" -> group = parts.length > 1 ? parts[1] : "";
				case "f" -> polygons.add(new Polygon(parseFace(parts, positions.size(), uvs.size()), group));
				default -> {
				}
			}
		}
		if (positions.isEmpty() || polygons.isEmpty()) {
			throw new IOException("empty OBJ");
		}
		float[] bounds = bounds(positions);
		normalize(positions, bounds);
		List<Face> faces = new ArrayList<>(polygons.size());
		for (Polygon polygon : polygons) {
			List<Corner> corners = polygon.corners();
			for (int i = 1; i + 1 < corners.size(); i++) {
				faces.add(face(corners.get(0), corners.get(i), corners.get(i + 1), positions, uvs, bounds, polygon.group()));
			}
		}
		return new ObjModel(faces);
	}

	private static List<Corner> parseFace(String[] parts, int positionCount, int uvCount) {
		List<Corner> corners = new ArrayList<>(parts.length - 1);
		for (int i = 1; i < parts.length; i++) {
			String[] indices = parts[i].split("/");
			int position = resolve(indices[0], positionCount);
			int uv = indices.length > 1 && !indices[1].isEmpty() ? resolve(indices[1], uvCount) : -1;
			corners.add(new Corner(position, uv));
		}
		return corners;
	}

	private static int resolve(String token, int count) {
		int index = Integer.parseInt(token);
		return index < 0 ? count + index : index - 1;
	}

	private static Face face(Corner a, Corner b, Corner c, List<Vector3f> positions, List<float[]> uvs, float[] bounds, String group) {
		Vector3f pa = positions.get(a.position());
		Vector3f pb = positions.get(b.position());
		Vector3f pc = positions.get(c.position());
		Vector3f normal = new Vector3f(pb).sub(pa).cross(new Vector3f(pc).sub(pa));
		float[] box = boxUv(normal, pa, pb, pc);
		float[] ta = a.uv() >= 0 ? uvs.get(a.uv()) : project(pa, normal, box);
		float[] tb = b.uv() >= 0 ? uvs.get(b.uv()) : project(pb, normal, box);
		float[] tc = c.uv() >= 0 ? uvs.get(c.uv()) : project(pc, normal, box);
		return new Face(pa, pb, pc, pc, ta[0], flip(ta[1]), tb[0], flip(tb[1]), tc[0], flip(tc[1]), tc[0], flip(tc[1]), group);
	}

	private static float flip(float v) {
		return 1.0F - v;
	}

	private static float value(String token) {
		return Float.parseFloat(token);
	}

	private static float[] bounds(List<Vector3f> positions) {
		float[] bounds = { Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE };
		for (Vector3f position : positions) {
			bounds[0] = Math.min(bounds[0], position.x);
			bounds[1] = Math.min(bounds[1], position.y);
			bounds[2] = Math.min(bounds[2], position.z);
			bounds[3] = Math.max(bounds[3], position.x);
			bounds[4] = Math.max(bounds[4], position.y);
			bounds[5] = Math.max(bounds[5], position.z);
		}
		return bounds;
	}

	private static void normalize(List<Vector3f> positions, float[] bounds) {
		float sizeX = bounds[3] - bounds[0];
		float sizeY = bounds[4] - bounds[1];
		float sizeZ = bounds[5] - bounds[2];
		float scale = 1.0F / Math.max(sizeX, Math.max(sizeY, sizeZ));
		float centerX = (bounds[0] + bounds[3]) * 0.5F;
		float centerZ = (bounds[2] + bounds[5]) * 0.5F;
		for (Vector3f position : positions) {
			position.set((position.x - centerX) * scale + 0.5F, (position.y - bounds[1]) * scale,
					(position.z - centerZ) * scale + 0.5F);
		}
	}

	private static float[] boxUv(Vector3f normal, Vector3f pa, Vector3f pb, Vector3f pc) {
		float ax = Math.abs(normal.x);
		float ay = Math.abs(normal.y);
		float az = Math.abs(normal.z);
		if (ay >= ax && ay >= az) {
			return new float[] { Math.min(pa.x, Math.min(pb.x, pc.x)), Math.min(pa.z, Math.min(pb.z, pc.z)),
					Math.max(pa.x, Math.max(pb.x, pc.x)), Math.max(pa.z, Math.max(pb.z, pc.z)) };
		}
		if (ax >= az) {
			return new float[] { Math.min(pa.z, Math.min(pb.z, pc.z)), Math.min(pa.y, Math.min(pb.y, pc.y)),
					Math.max(pa.z, Math.max(pb.z, pc.z)), Math.max(pa.y, Math.max(pb.y, pc.y)) };
		}
		return new float[] { Math.min(pa.x, Math.min(pb.x, pc.x)), Math.min(pa.y, Math.min(pb.y, pc.y)),
				Math.max(pa.x, Math.max(pb.x, pc.x)), Math.max(pa.y, Math.max(pb.y, pc.y)) };
	}

	private static float[] project(Vector3f position, Vector3f normal, float[] box) {
		float ax = Math.abs(normal.x);
		float ay = Math.abs(normal.y);
		float az = Math.abs(normal.z);
		float u;
		float v;
		if (ay >= ax && ay >= az) {
			u = (position.x - box[0]) / Math.max(box[2] - box[0], 1.0E-6F);
			v = (position.z - box[1]) / Math.max(box[3] - box[1], 1.0E-6F);
		} else if (ax >= az) {
			u = (position.z - box[0]) / Math.max(box[2] - box[0], 1.0E-6F);
			v = (position.y - box[1]) / Math.max(box[3] - box[1], 1.0E-6F);
		} else {
			u = (position.x - box[0]) / Math.max(box[2] - box[0], 1.0E-6F);
			v = (position.y - box[1]) / Math.max(box[3] - box[1], 1.0E-6F);
		}
		return new float[] { u, v };
	}
}

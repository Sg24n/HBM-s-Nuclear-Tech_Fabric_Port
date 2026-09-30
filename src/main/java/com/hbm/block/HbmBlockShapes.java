package com.hbm.block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Collision/interaction shape table for generated HBM blocks. */
public final class HbmBlockShapes {

	private static final Map<Block, VoxelShape> SHAPES = new HashMap<>();

	private HbmBlockShapes() {
	}

	public static void put(Block block, List<AABB> boxes) {
		VoxelShape shape = Shapes.empty();
		for (AABB box : boxes) {
			shape = Shapes.or(shape, Shapes.create(box));
		}
		SHAPES.put(block, shape);
	}

	public static VoxelShape get(Block block) {
		return SHAPES.get(block);
	}
}

package com.hbm.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A block with a custom (non full-cube) collision shape. */
public class HbmShapedBlock extends Block {

	public HbmShapedBlock(Properties properties) {
		super(properties);
	}

	private VoxelShape shape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context,
			VoxelShape fallback) {
		VoxelShape custom = HbmBlockShapes.get(this);
		return custom != null ? custom : fallback;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return shape(state, level, pos, context, super.getShape(state, level, pos, context));
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return shape(state, level, pos, context, super.getCollisionShape(state, level, pos, context));
	}

	@Override
	protected int getLightDampening(BlockState state) {
		return 0;
	}

	@Override
	protected boolean propagatesSkylightDown(BlockState state) {
		return true;
	}
}

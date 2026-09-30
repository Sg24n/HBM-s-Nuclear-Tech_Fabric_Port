package com.hbm.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A horizontal-facing block with an optional custom collision shape. */
public class HbmDirectionalBlock extends HorizontalDirectionalBlock {

	public HbmDirectionalBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
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
}

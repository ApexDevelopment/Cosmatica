package cc.badideas.cosmatica.block;

import finalforeach.cosmicreach.world.blocks.BlockState;

public record PositionedBlockState(BlockState blockState, int x, int y, int z) {
}

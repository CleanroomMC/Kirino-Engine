package com.cleanroommc.kirino.engine.render.platform.minecraft.semantic;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;

public final class BlockUnifier {
    private BlockUnifier() {
    }

    public static BlockModelType getBlockModelType(IBlockState blockState) {
        if (blockState.isFullBlock()) {
            return BlockModelType.FULL_BLOCK;
        }
        if (blockState.getRenderType() == EnumBlockRenderType.INVISIBLE) {
            return BlockModelType.INVISIBLE;
        }
        if (blockState.getRenderType() == EnumBlockRenderType.LIQUID) {
            return BlockModelType.FLUID;
        }
        if (blockState.getRenderType() == EnumBlockRenderType.ENTITYBLOCK_ANIMATED) {
            return BlockModelType.TESR;
        }
        if (blockState.getRenderType() == EnumBlockRenderType.MODEL) {
            return BlockModelType.CUSTOM_MODEL;
        }

        throw new IllegalStateException("Can't get a valid BlockModelType from " + blockState.getBlock() + ".");
    }

    public static BlockRenderingType getBlockRenderingType(IBlockState blockState) {
        if (blockState.getBlock().getRenderLayer() == BlockRenderLayer.SOLID) {
            return BlockRenderingType.OPAQUE;
        }
        if (blockState.getBlock().getRenderLayer() == BlockRenderLayer.CUTOUT || blockState.getBlock().getRenderLayer() == BlockRenderLayer.CUTOUT_MIPPED) {
            return BlockRenderingType.CUTOUT;
        }
        if (blockState.getBlock().getRenderLayer() == BlockRenderLayer.TRANSLUCENT) {
            return BlockRenderingType.TRANSPARENT;
        }

        throw new IllegalStateException("Can't get a valid BlockRenderingType from " + blockState.getBlock() + ".");
    }
}

package com.cleanroommc.kirino.engine.render.minecraft.utils;

import com.cleanroommc.kirino.KirinoCore;
import com.cleanroommc.kirino.engine.render.minecraft.semantic.BlockModelType;
import com.cleanroommc.kirino.engine.render.minecraft.semantic.BlockUnifier;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockFluidRenderer;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;

import java.lang.invoke.MethodHandle;
import java.nio.ByteBuffer;

public class BlockMeshGenerator {
    private final BlockRendererDispatcher blockRendererDispatcher;
    private final BlockModelRenderer blockModelRenderer;
    private final BlockFluidRenderer blockFluidRenderer;
    private final BufferBuilder bufferBuilder;

    public BlockMeshGenerator() {
        blockRendererDispatcher = MethodHolder.getBlockRendererDispatcher(Minecraft.getMinecraft());
        blockModelRenderer = MethodHolder.getBlockModelRenderer(blockRendererDispatcher);
        blockFluidRenderer = MethodHolder.getBlockFluidRenderer(blockRendererDispatcher);
        bufferBuilder = new BufferBuilder(1024 * 64); // 64KB
    }

    public int[] getFullBlockTexCoords(int worldX, int worldY, int worldZ, IBlockAccess blockAccess, IBlockState blockState) {
        Preconditions.checkState(BlockUnifier.getBlockModelType(blockState) == BlockModelType.FULL_BLOCK);

        BlockPos.PooledMutableBlockPos blockPos = BlockPos.PooledMutableBlockPos.retain(worldX, worldY, worldZ);

        IBakedModel model = blockRendererDispatcher.getModelForState(blockState);
        IBlockState extendedState = blockState.getBlock().getExtendedState(blockState, blockAccess, blockPos);

        bufferBuilder.reset();
        bufferBuilder.begin(7, DefaultVertexFormats.BLOCK);
        blockModelRenderer.renderModel(blockAccess, model, extendedState, blockPos, bufferBuilder, true);
        bufferBuilder.finishDrawing();

        blockPos.release();

//                DOWN(0, 1, -1, "down", EnumFacing.AxisDirection.NEGATIVE, EnumFacing.Axis.Y, new Vec3i(0, -1, 0)),
//                UP(1, 0, -1, "up", EnumFacing.AxisDirection.POSITIVE, EnumFacing.Axis.Y, new Vec3i(0, 1, 0)),
//                NORTH(2, 3, 2, "north", EnumFacing.AxisDirection.NEGATIVE, EnumFacing.Axis.Z, new Vec3i(0, 0, -1)),
//                SOUTH(3, 2, 0, "south", EnumFacing.AxisDirection.POSITIVE, EnumFacing.Axis.Z, new Vec3i(0, 0, 1)),
//                WEST(4, 5, 1, "west", EnumFacing.AxisDirection.NEGATIVE, EnumFacing.Axis.X, new Vec3i(-1, 0, 0)),
//                EAST(5, 4, 3, "east", EnumFacing.AxisDirection.POSITIVE, EnumFacing.Axis.X, new Vec3i(1, 0, 0));

        ByteBuffer byteBuffer = MethodHolder.getByteBuffer(bufferBuilder);
        int remaining = byteBuffer.remaining();

        // one stride == 28 bytes
        // one face == 112 bytes
        Preconditions.checkState(remaining % 112 == 0);

        int vertexCount = remaining / 28;

        for (int i = 0; i < vertexCount; i++) {
            byteBuffer.position(28 * i);
            KirinoCore.LOGGER.info("xyz: " + byteBuffer.getFloat() + ", " + byteBuffer.getFloat() + ", " + byteBuffer.getFloat());
            byteBuffer.position(28 * i + 16);
            KirinoCore.LOGGER.info("texcoords: " + byteBuffer.getFloat() + ", " + byteBuffer.getFloat());
            KirinoCore.LOGGER.info("---------");
        }

        return null;
    }

    private static class MethodHolder {
        static final Delegate DELEGATE;

        static {
            DELEGATE = new Delegate(
                    ReflectionUtils.getFieldGetter(Minecraft.class, "blockRenderDispatcher", "field_175618_aM", BlockRendererDispatcher.class),
                    ReflectionUtils.getFieldGetter(BlockRendererDispatcher.class, "blockModelRenderer", "field_175027_c", BlockModelRenderer.class),
                    ReflectionUtils.getFieldGetter(BlockRendererDispatcher.class, "fluidRenderer", "field_175025_e", BlockFluidRenderer.class),
                    ReflectionUtils.getFieldGetter(BufferBuilder.class, "byteBuffer", "field_179001_a", ByteBuffer.class));

            Preconditions.checkNotNull(DELEGATE.blockRendererDispatcherGetter);
            Preconditions.checkNotNull(DELEGATE.blockModelRendererGetter);
            Preconditions.checkNotNull(DELEGATE.blockFluidRendererGetter);
            Preconditions.checkNotNull(DELEGATE.byteBufferGetter);
        }

        static BlockRendererDispatcher getBlockRendererDispatcher(Minecraft minecraft) {
            BlockRendererDispatcher result;
            try {
                result = (BlockRendererDispatcher) DELEGATE.blockRendererDispatcherGetter.invokeExact(minecraft);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return result;
        }

        static BlockModelRenderer getBlockModelRenderer(BlockRendererDispatcher blockRendererDispatcher) {
            BlockModelRenderer result;
            try {
                result = (BlockModelRenderer) DELEGATE.blockModelRendererGetter.invokeExact(blockRendererDispatcher);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return result;
        }

        static BlockFluidRenderer getBlockFluidRenderer(BlockRendererDispatcher blockRendererDispatcher) {
            BlockFluidRenderer result;
            try {
                result = (BlockFluidRenderer) DELEGATE.blockFluidRendererGetter.invokeExact(blockRendererDispatcher);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return result;
        }

        static ByteBuffer getByteBuffer(BufferBuilder bufferBuilder) {
            ByteBuffer result;
            try {
                result = (ByteBuffer) DELEGATE.byteBufferGetter.invokeExact(bufferBuilder);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return result;
        }

        record Delegate(
                MethodHandle blockRendererDispatcherGetter,
                MethodHandle blockModelRendererGetter,
                MethodHandle blockFluidRendererGetter,
                MethodHandle byteBufferGetter) {
        }
    }
}

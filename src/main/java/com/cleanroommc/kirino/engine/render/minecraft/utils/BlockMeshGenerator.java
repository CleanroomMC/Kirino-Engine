package com.cleanroommc.kirino.engine.render.minecraft.utils;

import com.cleanroommc.kirino.engine.render.ecs.struct.BlockInfo;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.lang.invoke.MethodHandle;
import java.nio.ByteBuffer;

public class BlockMeshGenerator {
    private final BlockRendererDispatcher blockRendererDispatcher;
    private final BlockModelRenderer blockModelRenderer;
    private final BlockFluidRenderer blockFluidRenderer;

    public BlockMeshGenerator() {
        blockRendererDispatcher = MethodHolder.getBlockRendererDispatcher(Minecraft.getMinecraft());
        blockModelRenderer = MethodHolder.getBlockModelRenderer(blockRendererDispatcher);
        blockFluidRenderer = MethodHolder.getBlockFluidRenderer(blockRendererDispatcher);
    }

    private static int encodeTexCoordsToInt32(float texCoordX, float texCoordY) {
        Preconditions.checkArgument(texCoordX >= 0f && texCoordX <= 1f,
                "Argument \"texCoordX\" must in [0, 1].");
        Preconditions.checkArgument(texCoordY >= 0f && texCoordY <= 1f,
                "Argument \"texCoordY\" must in [0, 1].");

        int a = Math.round(texCoordX * 65535f);
        int b = Math.round(texCoordY * 65535f);
        a = Math.min(a, 0xFFFF);
        b = Math.min(b, 0xFFFF);

        return (b << 16) | a;
    }

    /**
     * @param worldX X-coordinate
     * @param worldY Y-coordinate
     * @param worldZ Z-coordinate
     * @param blockAccess A world
     * @param blockState A block state
     * @param bufferBuilder A buffer builder with the minimum size <code>112 * 6</code> bytes
     * @return
     */
    @SuppressWarnings("all")
    public BlockInfo getFullBlockInfo(int worldX, int worldY, int worldZ, IBlockAccess blockAccess, IBlockState blockState, BufferBuilder bufferBuilder) {
        Preconditions.checkState(BlockUnifier.getBlockModelType(blockState) == BlockModelType.FULL_BLOCK);
        Preconditions.checkArgument(MethodHolder.getByteBuffer(bufferBuilder).capacity() >= 112 * 6,
                "Size of argument \"bufferBuilder\" is %s bytes. Requires at least 672 bytes.", MethodHolder.getByteBuffer(bufferBuilder).capacity());

        BlockPos.PooledMutableBlockPos blockPos = BlockPos.PooledMutableBlockPos.retain(worldX, worldY, worldZ);

        IBakedModel model = blockRendererDispatcher.getModelForState(blockState);
        IBlockState extendedState = blockState.getBlock().getExtendedState(blockState, blockAccess, blockPos);

        bufferBuilder.reset();
        bufferBuilder.begin(7, DefaultVertexFormats.BLOCK);
        blockModelRenderer.renderModel(blockAccess, model, extendedState, blockPos, bufferBuilder, false);
        bufferBuilder.finishDrawing();

        blockPos.release();

        ByteBuffer byteBuffer = MethodHolder.getByteBuffer(bufferBuilder);
        int remaining = byteBuffer.remaining();

        // one stride == 28 bytes
        // one face == 112 bytes
        // expecting 6 faces
        Preconditions.checkState(remaining == 112 * 6);

        BlockInfo blockInfo = new BlockInfo();

        // face0 down -Y
        byteBuffer.position(0 * 112 + 0 * 28 + 16);
        blockInfo.yMinusFaceTexCoord0 = encodeTexCoordsToInt32(byteBuffer.getFloat(), byteBuffer.getFloat());
        byteBuffer.position(0 * 112 + 1 * 28 + 16);
        blockInfo.yMinusFaceTexCoord1 = encodeTexCoordsToInt32(byteBuffer.getFloat(), byteBuffer.getFloat());
        byteBuffer.position(0 * 112 + 2 * 28 + 16);
        blockInfo.yMinusFaceTexCoord2 = encodeTexCoordsToInt32(byteBuffer.getFloat(), byteBuffer.getFloat());
        byteBuffer.position(0 * 112 + 3 * 28 + 16);
        blockInfo.yMinusFaceTexCoord3 = encodeTexCoordsToInt32(byteBuffer.getFloat(), byteBuffer.getFloat());

        // face1 up +Y
        byteBuffer.position(1 * 112 + 0 * 28 + 16);
        blockInfo.yPlusFaceTexCoord0 = encodeTexCoordsToInt32(byteBuffer.getFloat(), byteBuffer.getFloat());
        byteBuffer.position(1 * 112 + 1 * 28 + 16);
        blockInfo.yPlusFaceTexCoord1 = encodeTexCoordsToInt32(byteBuffer.getFloat(), byteBuffer.getFloat());
        byteBuffer.position(1 * 112 + 2 * 28 + 16);
        blockInfo.yPlusFaceTexCoord2 = encodeTexCoordsToInt32(byteBuffer.getFloat(), byteBuffer.getFloat());
        byteBuffer.position(1 * 112 + 3 * 28 + 16);
        blockInfo.yPlusFaceTexCoord3 = encodeTexCoordsToInt32(byteBuffer.getFloat(), byteBuffer.getFloat());

        // face2 north -Z
        byteBuffer.position(2 * 112 + 0 * 28 + 16);
        blockInfo.zMinusFaceTexCoord0 = encodeTexCoordsToInt32(byteBuffer.getFloat(), byteBuffer.getFloat());
        byteBuffer.position(2 * 112 + 1 * 28 + 16);
        blockInfo.zMinusFaceTexCoord1 = encodeTexCoordsToInt32(byteBuffer.getFloat(), byteBuffer.getFloat());
        byteBuffer.position(2 * 112 + 2 * 28 + 16);
        blockInfo.zMinusFaceTexCoord2= encodeTexCoordsToInt32(byteBuffer.getFloat(), byteBuffer.getFloat());
        byteBuffer.position(2 * 112 + 3 * 28 + 16);
        blockInfo.zMinusFaceTexCoord3 = encodeTexCoordsToInt32(byteBuffer.getFloat(), byteBuffer.getFloat());

        // face3 south +Z
        byteBuffer.position(3 * 112 + 0 * 28 + 16);
        blockInfo.zPlusFaceTexCoord0 = encodeTexCoordsToInt32(byteBuffer.getFloat(), byteBuffer.getFloat());
        byteBuffer.position(3 * 112 + 1 * 28 + 16);
        blockInfo.zPlusFaceTexCoord1 = encodeTexCoordsToInt32(byteBuffer.getFloat(), byteBuffer.getFloat());
        byteBuffer.position(3 * 112 + 2 * 28 + 16);
        blockInfo.zPlusFaceTexCoord2= encodeTexCoordsToInt32(byteBuffer.getFloat(), byteBuffer.getFloat());
        byteBuffer.position(3 * 112 + 3 * 28 + 16);
        blockInfo.zPlusFaceTexCoord3 = encodeTexCoordsToInt32(byteBuffer.getFloat(), byteBuffer.getFloat());

        // face4 west -X
        byteBuffer.position(4 * 112 + 0 * 28 + 16);
        blockInfo.xMinusFaceTexCoord0 = encodeTexCoordsToInt32(byteBuffer.getFloat(), byteBuffer.getFloat());
        byteBuffer.position(4 * 112 + 1 * 28 + 16);
        blockInfo.xMinusFaceTexCoord1 = encodeTexCoordsToInt32(byteBuffer.getFloat(), byteBuffer.getFloat());
        byteBuffer.position(4 * 112 + 2 * 28 + 16);
        blockInfo.xMinusFaceTexCoord2= encodeTexCoordsToInt32(byteBuffer.getFloat(), byteBuffer.getFloat());
        byteBuffer.position(4 * 112 + 3 * 28 + 16);
        blockInfo.xMinusFaceTexCoord3 = encodeTexCoordsToInt32(byteBuffer.getFloat(), byteBuffer.getFloat());

        // face5 east +X
        byteBuffer.position(5 * 112 + 0 * 28 + 16);
        blockInfo.xPlusFaceTexCoord0 = encodeTexCoordsToInt32(byteBuffer.getFloat(), byteBuffer.getFloat());
        byteBuffer.position(5 * 112 + 1 * 28 + 16);
        blockInfo.xPlusFaceTexCoord1 = encodeTexCoordsToInt32(byteBuffer.getFloat(), byteBuffer.getFloat());
        byteBuffer.position(5 * 112 + 2 * 28 + 16);
        blockInfo.xPlusFaceTexCoord2= encodeTexCoordsToInt32(byteBuffer.getFloat(), byteBuffer.getFloat());
        byteBuffer.position(5 * 112 + 3 * 28 + 16);
        blockInfo.xPlusFaceTexCoord3 = encodeTexCoordsToInt32(byteBuffer.getFloat(), byteBuffer.getFloat());

        return blockInfo;
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

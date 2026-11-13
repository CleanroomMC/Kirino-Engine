package com.cleanroommc.kirino.engine.render.usage.task.job;

import com.cleanroommc.kirino.engine.render.usage.ecs.struct.Block;
import com.cleanroommc.kirino.engine.render.usage.ecs.struct.BlockInfo;
import com.cleanroommc.kirino.engine.render.usage.minecraft.semantic.BlockModelType;
import com.cleanroommc.kirino.engine.render.usage.minecraft.semantic.BlockUnifier;
import com.cleanroommc.kirino.engine.render.usage.minecraft.utils.BlockMeshGenerator;
import com.cleanroommc.kirino.engine.resource.ResourceLayout;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.chunk.Chunk;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import sun.misc.Unsafe;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChunkMeshletGenJobTest {
    @ParameterizedTest
    @CsvSource({"0, 0, 0", "3, 0, 7", "3, 4, 7", "-60, 4, 87", "-1, 15, -1"})
    void fillBlockInfo_sectionCoordinates_readsCorrectHeightAndPassesWorldPosition(int chunkX, int sectionY, int chunkZ)
        throws Exception {
        IBlockState full = state(true);
        IBlockState nonFull = state(false);
        RecordingChunk chunk = allocate(RecordingChunk.class);
        chunk.sectionY = sectionY;
        chunk.full = full;
        chunk.nonFull = nonFull;
        chunk.reads = new ArrayList<>();
        RecordingGenerator generator = allocate(RecordingGenerator.class);
        generator.calls = new ArrayList<>();
        ResourceLayout layout = construct(ResourceLayout.class);
        ResourceStorage storage = construct(ResourceStorage.class);
        ChunkMeshletGenJob job = new ChunkMeshletGenJob();
        job.storage = storage;
        job.blockMeshGenerator = layout.slot(BlockMeshGenerator.class);
        storage.put(job.blockMeshGenerator, generator);
        var section = new ChunkMeshletGenJob.ChunkCluster(chunkX, sectionY, chunkZ, chunk, null, null, null, null);
        List<Block> blocks = List.of(new Block(0, 0, 0, 63), new Block(15, 15, 15, 63), new Block(3, 7, 11, 63));

        job.fillBlockInfo(section, blocks, null);

        assertEquals(blocks.size(), generator.calls.size());
        for (int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);
            Call call = generator.calls.get(i);
            assertArrayEquals(new int[]{block.position.x, sectionY * 16 + block.position.y, block.position.z},
                chunk.reads.get(i));
            assertEquals(chunkX * 16 + block.position.x, call.x);
            assertEquals(sectionY * 16 + block.position.y, call.y);
            assertEquals(chunkZ * 16 + block.position.z, call.z);
            assertSame(full, call.state);
            assertSame(job.world, call.access);
            assertNull(call.buffer);
            assertSame(call.result, block.blockInfo);
        }
    }

    private static IBlockState state(boolean full) {
        return (IBlockState) Proxy.newProxyInstance(IBlockState.class.getClassLoader(), new Class[]{IBlockState.class},
            (proxy, method, args) -> {
                if (method.getName().equals("isFullBlock")) return full;
                if (method.getName().equals("getRenderType")) {
                    return full ? EnumBlockRenderType.MODEL : EnumBlockRenderType.INVISIBLE;
                }
                throw new AssertionError("Unexpected state method: " + method.getName());
            });
    }

    private static <T> T construct(Class<T> type) throws Exception {
        var constructor = type.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private static <T> T allocate(Class<T> type) throws Exception {
        var field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return type.cast(((Unsafe) field.get(null)).allocateInstance(type));
    }

    private record Call(int x, int y, int z, IBlockAccess access, IBlockState state,
                        BufferBuilder buffer, BlockInfo result) {
    }

    private static final class RecordingChunk extends Chunk {
        int sectionY;
        IBlockState full;
        IBlockState nonFull;
        List<int[]> reads;

        private RecordingChunk() { super(null, 0, 0); }

        @Override public IBlockState getBlockState(int x, int y, int z) {
            reads.add(new int[]{x, y, z});
            return y / 16 == sectionY ? full : nonFull;
        }
    }

    private static final class RecordingGenerator extends BlockMeshGenerator {
        List<Call> calls;

        private RecordingGenerator() { super((Minecraft) null); }

        @Override public BlockInfo genFullBlockInfo(int x, int y, int z, IBlockAccess access,
                                                   IBlockState state, BufferBuilder buffer) {
            assertEquals(BlockModelType.FULL_BLOCK, BlockUnifier.getBlockModelType(state));
            BlockInfo result = new BlockInfo();
            calls.add(new Call(x, y, z, access, state, buffer, result));
            return result;
        }
    }
}

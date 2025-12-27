package com.cleanroommc.kirino.engine.render.ecs.component;

import com.cleanroommc.kirino.engine.render.scene.gpu_meshlet.MeshletGpuRegistry;
import com.cleanroommc.kirino.ecs.component.ICleanComponent;
import com.cleanroommc.kirino.ecs.component.scan.CleanComponent;
import com.cleanroommc.kirino.engine.render.ecs.struct.AABB;
import com.cleanroommc.kirino.engine.render.ecs.struct.Block;
import com.google.common.base.Preconditions;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

@CleanComponent
public class MeshletComponent implements ICleanComponent, Iterable<Block> {
    public AABB aabb = new AABB();
    public Vector3f normal = new Vector3f();

    /**
     * Used by {@link MeshletGpuRegistry}. <code>-1</code> stands for invalid.
     */
    public int meshletId = -1;

    /**
     * <p><b>0</b>: opaque</p>
     * <p><b>1</b>: transparent</p>
     * <p><b>2</b>: cutout</p>
     */
    public int pass = 0;

    public boolean isConsumed = false;

    /**
     * X-coordinate under chunk coordinate system. (* 16 = world coordinate)
     */
    public int chunkPosX;

    /**
     * Y-coordinate under chunk coordinate system. (* 16 = world coordinate)
     */
    public int chunkPosY;

    /**
     * Z-coordinate under chunk coordinate system. (* 16 = world coordinate)
     */
    public int chunkPosZ;

    public int blockCount = 0;

    public Block block0 = new Block();
    public Block block1 = new Block();
    public Block block2 = new Block();
    public Block block3 = new Block();
    public Block block4 = new Block();
    public Block block5 = new Block();
    public Block block6 = new Block();
    public Block block7 = new Block();
    public Block block8 = new Block();
    public Block block9 = new Block();
    public Block block10 = new Block();
    public Block block11 = new Block();
    public Block block12 = new Block();
    public Block block13 = new Block();
    public Block block14 = new Block();
    public Block block15 = new Block();
    public Block block16 = new Block();
    public Block block17 = new Block();
    public Block block18 = new Block();
    public Block block19 = new Block();
    public Block block20 = new Block();
    public Block block21 = new Block();
    public Block block22 = new Block();
    public Block block23 = new Block();
    public Block block24 = new Block();
    public Block block25 = new Block();
    public Block block26 = new Block();
    public Block block27 = new Block();
    public Block block28 = new Block();
    public Block block29 = new Block();
    public Block block30 = new Block();
    public Block block31 = new Block();

    public Block getBlock(int index) {
        Preconditions.checkElementIndex(index, 32);

        return switch (index) {
            case 0 -> block0;
            case 1 -> block1;
            case 2 -> block2;
            case 3 -> block3;
            case 4 -> block4;
            case 5 -> block5;
            case 6 -> block6;
            case 7 -> block7;
            case 8 -> block8;
            case 9 -> block9;
            case 10 -> block10;
            case 11 -> block11;
            case 12 -> block12;
            case 13 -> block13;
            case 14 -> block14;
            case 15 -> block15;
            case 16 -> block16;
            case 17 -> block17;
            case 18 -> block18;
            case 19 -> block19;
            case 20 -> block20;
            case 21 -> block21;
            case 22 -> block22;
            case 23 -> block23;
            case 24 -> block24;
            case 25 -> block25;
            case 26 -> block26;
            case 27 -> block27;
            case 28 -> block28;
            case 29 -> block29;
            case 30 -> block30;
            case 31 -> block31;
            default -> throw new IndexOutOfBoundsException(index);
        };
    }

    public void setBlock(int index, Block block) {
        Preconditions.checkElementIndex(index, 32);

        switch (index) {
            case 0: block0 = block;
            case 1: block1 = block;
            case 2: block2 = block;
            case 3: block3 = block;
            case 4: block4 = block;
            case 5: block5 = block;
            case 6: block6 = block;
            case 7: block7 = block;
            case 8: block8 = block;
            case 9: block9 = block;
            case 10: block10 = block;
            case 11: block11 = block;
            case 12: block12 = block;
            case 13: block13 = block;
            case 14: block14 = block;
            case 15: block15 = block;
            case 16: block16 = block;
            case 17: block17 = block;
            case 18: block18 = block;
            case 19: block19 = block;
            case 20: block20 = block;
            case 21: block21 = block;
            case 22: block22 = block;
            case 23: block23 = block;
            case 24: block24 = block;
            case 25: block25 = block;
            case 26: block26 = block;
            case 27: block27 = block;
            case 28: block28 = block;
            case 29: block29 = block;
            case 30: block30 = block;
            case 31: block31 = block;
        }
    }

    public void addBlock(Block block) {
        if (++blockCount > 32) {
            blockCount = 32;
            return;
        }

        setBlock(blockCount - 1, block);
    }

    @NonNull
    @Override
    public Iterator<Block> iterator() {
        return new Iterator<>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < blockCount;
            }

            @Override
            public Block next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                return getBlock(index++);
            }
        };
    }

    public MeshletComponent() {
    }

    public MeshletComponent(List<Block> blocks) {
        Preconditions.checkPositionIndex(blocks.size(), 32);

        blockCount = 0;
        for (Block block : blocks) {
            addBlock(block);
        }
    }
}

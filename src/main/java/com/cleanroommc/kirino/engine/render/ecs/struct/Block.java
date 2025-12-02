package com.cleanroommc.kirino.engine.render.ecs.struct;

import com.cleanroommc.kirino.ecs.component.scan.CleanStruct;
import org.joml.Vector3i;

@CleanStruct
public class Block {
    public Vector3i position;
    public int faceMask;
    public int positionAndFaceMask;

    public BlockInfo blockInfo = new BlockInfo();

    public Block() {
        position = new Vector3i();
        faceMask = 0b111111;
        compressPositionAndFaceMask();
    }

    public Block(int x, int y, int z, int faceMask) {
        position = new Vector3i(x, y, z);
        this.faceMask = faceMask;
        compressPositionAndFaceMask();
    }

    /**
     * <p>Compresses the block rendering record into an integer</p>
     * <p>
     *     The integer is formatted as: <table>
     *         <tr>
     *             <th>17</th>
     *             <th>16</th>
     *             <th>15</th>
     *             <th>14</th>
     *             <th>13</th>
     *             <th>12</th>
     *             <th>11</th>
     *             <th>10</th>
     *             <th>9</th>
     *             <th>8</th>
     *             <th>7</th>
     *             <th>6</th>
     *             <th>5</th>
     *             <th>4</th>
     *             <th>3</th>
     *             <th>2</th>
     *             <th>1</th>
     *             <th>0</th>
     *         </tr>
     *         <tr>
     *             <th>z</th>
     *             <th>z</th>
     *             <th>z</th>
     *             <th>z</th>
     *             <th>y</th>
     *             <th>y</th>
     *             <th>y</th>
     *             <th>y</th>
     *             <th>x</th>
     *             <th>x</th>
     *             <th>x</th>
     *             <th>x</th>
     *             <th>f</th>
     *             <th>f</th>
     *             <th>f</th>
     *             <th>f</th>
     *             <th>f</th>
     *             <th>f</th>
     *         </tr>
     *     </table>
     * </p>
     * <p>
     *     Where:
     *     <table>
     *         <tr>
     *             <th>x</th>
     *             <th>the x position of the block in the 16x16x16 cutout of the chunk</th>
     *         </tr>
     *         <tr>
     *             <th>y</th>
     *             <th>the y position of the block in the 16x16x16 cutout of the chunk</th>
     *         </tr>
     *         <tr>
     *             <th>z</th>
     *             <th>the z position of the block in the 16x16x16 cutout of the chunk</th>
     *         </tr>
     *         <tr>
     *             <th>f</th>
     *             <th>A bitfield of the sides to be rendered. From the right in order:
     *             X+, X-, Y+, Y-, Z+, Z-</th>
     *         </tr>
     *     </table>
     * </p>
     * @return The integer as described above
     */
    public static int compress(int x, int y, int z, int faceMask) {
        x = x & 0b1111; // 4 bits
        y = y & 0b1111; // 4 bits
        z = z & 0b1111; // 4 bits
        faceMask = faceMask & 0b111111; // 6 bits

        return (z << 14) | (y << 10) | (x << 6) | faceMask;
    }

    /**
     * @see #compress(int, int, int, int)
     */
    public static int[] decompress(int data) {
        int faceMask = data & 0b111111;
        int x = (data >> 6) & 0b1111;
        int y = (data >> 10) & 0b1111;
        int z = (data >> 14) & 0b1111;

        return new int[]{x, y, z, faceMask};
    }

    public void compressPositionAndFaceMask() {
        positionAndFaceMask = compress(position.x, position.y, position.z, faceMask);
    }
}

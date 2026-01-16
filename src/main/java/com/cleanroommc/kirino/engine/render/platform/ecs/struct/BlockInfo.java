package com.cleanroommc.kirino.engine.render.platform.ecs.struct;

import com.cleanroommc.kirino.ecs.component.scan.CleanStruct;

@CleanStruct
public class BlockInfo {
    /**
     * <p><b>0</b>: yz<1, 1></p>
     */
    public int xPlusFaceTexCoord0;
    /**
     * <p><b>1</b>: yz<0, 1></p>
     */
    public int xPlusFaceTexCoord1;
    /**
     * <p><b>2</b>: yz<0, 0></p>
     */
    public int xPlusFaceTexCoord2;
    /**
     * <p><b>3</b>: yz<1, 0></p>
     */
    public int xPlusFaceTexCoord3;

    /**
     * <p><b>0</b>: yz<1, 0></p>
     */
    public int xMinusFaceTexCoord0;
    /**
     * <p><b>1</b>: yz<0, 0></p>
     */
    public int xMinusFaceTexCoord1;
    /**
     * <p><b>2</b>: yz<0, 1></p>
     */
    public int xMinusFaceTexCoord2;
    /**
     * <p><b>3</b>: yz<1, 1></p>
     */
    public int xMinusFaceTexCoord3;

    /**
     * <p><b>0</b>: xz<0, 0></p>
     */
    public int yPlusFaceTexCoord0;
    /**
     * <p><b>1</b>: xz<0, 1></p>
     */
    public int yPlusFaceTexCoord1;
    /**
     * <p><b>2</b>: xz<1, 1></p>
     */
    public int yPlusFaceTexCoord2;
    /**
     * <p><b>3</b>: xz<1, 0></p>
     */
    public int yPlusFaceTexCoord3;

    /**
     * <p><b>0</b>: xz<0, 1></p>
     */
    public int yMinusFaceTexCoord0;
    /**
     * <p><b>1</b>: xz<0, 0></p>
     */
    public int yMinusFaceTexCoord1;
    /**
     * <p><b>2</b>: xz<1, 0></p>
     */
    public int yMinusFaceTexCoord2;
    /**
     * <p><b>3</b>: xz<1, 1></p>
     */
    public int yMinusFaceTexCoord3;

    /**
     * <p><b>0</b>: xy<0, 1></p>
     */
    public int zPlusFaceTexCoord0;
    /**
     * <p><b>1</b>: xy<0, 0></p>
     */
    public int zPlusFaceTexCoord1;
    /**
     * <p><b>2</b>: xy<1, 0></p>
     */
    public int zPlusFaceTexCoord2;
    /**
     * <p><b>3</b>: xy<1, 1></p>
     */
    public int zPlusFaceTexCoord3;

    /**
     * <p><b>0</b>: xy<1, 1></p>
     */
    public int zMinusFaceTexCoord0;
    /**
     * <p><b>1</b>: xy<1, 0></p>
     */
    public int zMinusFaceTexCoord1;
    /**
     * <p><b>2</b>: xy<0, 0></p>
     */
    public int zMinusFaceTexCoord2;
    /**
     * <p><b>3</b>: xy<0, 1></p>
     */
    public int zMinusFaceTexCoord3;
}

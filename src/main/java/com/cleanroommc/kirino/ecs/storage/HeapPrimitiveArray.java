package com.cleanroommc.kirino.ecs.storage;

import com.google.common.base.Preconditions;

import java.util.Arrays;

public final class HeapPrimitiveArray implements PrimitiveArray {
    private final PrimitiveArrayType type;
    private int length;
    private byte[] byteArray;
    private short[] shortArray;
    private int[] intArray;
    private long[] longArray;
    private float[] floatArray;
    private double[] doubleArray;
    private boolean[] booleanArray;

    HeapPrimitiveArray(byte[] array) {
        type = PrimitiveArrayType.BYTE;
        length = array.length;
        byteArray = array;
    }

    HeapPrimitiveArray(short[] array) {
        type = PrimitiveArrayType.SHORT;
        length = array.length;
        shortArray = array;
    }

    HeapPrimitiveArray(int[] array) {
        type = PrimitiveArrayType.INT;
        length = array.length;
        intArray = array;
    }

    HeapPrimitiveArray(long[] array) {
        type = PrimitiveArrayType.LONG;
        length = array.length;
        longArray = array;
    }

    HeapPrimitiveArray(float[] array) {
        type = PrimitiveArrayType.FLOAT;
        length = array.length;
        floatArray = array;
    }

    HeapPrimitiveArray(double[] array) {
        type = PrimitiveArrayType.DOUBLE;
        length = array.length;
        doubleArray = array;
    }

    HeapPrimitiveArray(boolean[] array) {
        type = PrimitiveArrayType.BOOL;
        length = array.length;
        booleanArray = array;
    }

    HeapPrimitiveArray(PrimitiveArrayType type, int length) {
        this.type = type;
        this.length = length;
        switch (type) {
            case BYTE -> byteArray = new byte[length];
            case SHORT -> shortArray = new short[length];
            case INT -> intArray = new int[length];
            case LONG -> longArray = new long[length];
            case FLOAT -> floatArray = new float[length];
            case DOUBLE -> doubleArray = new double[length];
            case BOOL -> booleanArray = new boolean[length];
        }
    }

    Object resizedArray(int newLength) {
        return switch (type) {
            case BYTE -> Arrays.copyOf(byteArray, newLength);
            case SHORT -> Arrays.copyOf(shortArray, newLength);
            case INT -> Arrays.copyOf(intArray, newLength);
            case LONG -> Arrays.copyOf(longArray, newLength);
            case FLOAT -> Arrays.copyOf(floatArray, newLength);
            case DOUBLE -> Arrays.copyOf(doubleArray, newLength);
            case BOOL -> Arrays.copyOf(booleanArray, newLength);
        };
    }

    void replaceArray(Object array, int newLength) {
        switch (type) {
            case BYTE -> byteArray = (byte[]) array;
            case SHORT -> shortArray = (short[]) array;
            case INT -> intArray = (int[]) array;
            case LONG -> longArray = (long[]) array;
            case FLOAT -> floatArray = (float[]) array;
            case DOUBLE -> doubleArray = (double[]) array;
            case BOOL -> booleanArray = (boolean[]) array;
        }
        length = newLength;
    }

    @Override
    public byte getByte(int index) {
        Preconditions.checkState(type == PrimitiveArrayType.BYTE, "Wrong primitive column type.");
        return byteArray[index];
    }

    @Override
    public void setByte(int index, byte value) {
        Preconditions.checkState(type == PrimitiveArrayType.BYTE, "Wrong primitive column type.");
        byteArray[index] = value;
    }

    @Override
    public short getShort(int index) {
        Preconditions.checkState(type == PrimitiveArrayType.SHORT, "Wrong primitive column type.");
        return shortArray[index];
    }

    @Override
    public void setShort(int index, short value) {
        Preconditions.checkState(type == PrimitiveArrayType.SHORT, "Wrong primitive column type.");
        shortArray[index] = value;
    }

    @Override
    public int getInt(int index) {
        Preconditions.checkState(type == PrimitiveArrayType.INT, "Wrong primitive column type.");
        return intArray[index];
    }

    @Override
    public void setInt(int index, int value) {
        Preconditions.checkState(type == PrimitiveArrayType.INT, "Wrong primitive column type.");
        intArray[index] = value;
    }

    @Override
    public long getLong(int index) {
        Preconditions.checkState(type == PrimitiveArrayType.LONG, "Wrong primitive column type.");
        return longArray[index];
    }

    @Override
    public void setLong(int index, long value) {
        Preconditions.checkState(type == PrimitiveArrayType.LONG, "Wrong primitive column type.");
        longArray[index] = value;
    }

    @Override
    public float getFloat(int index) {
        Preconditions.checkState(type == PrimitiveArrayType.FLOAT, "Wrong primitive column type.");
        return floatArray[index];
    }

    @Override
    public void setFloat(int index, float value) {
        Preconditions.checkState(type == PrimitiveArrayType.FLOAT, "Wrong primitive column type.");
        floatArray[index] = value;
    }

    @Override
    public double getDouble(int index) {
        Preconditions.checkState(type == PrimitiveArrayType.DOUBLE, "Wrong primitive column type.");
        return doubleArray[index];
    }

    @Override
    public void setDouble(int index, double value) {
        Preconditions.checkState(type == PrimitiveArrayType.DOUBLE, "Wrong primitive column type.");
        doubleArray[index] = value;
    }

    @Override
    public boolean getBool(int index) {
        Preconditions.checkState(type == PrimitiveArrayType.BOOL, "Wrong primitive column type.");
        return booleanArray[index];
    }

    @Override
    public void setBool(int index, boolean value) {
        Preconditions.checkState(type == PrimitiveArrayType.BOOL, "Wrong primitive column type.");
        booleanArray[index] = value;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public PrimitiveArrayType type() {
        return type;
    }
}

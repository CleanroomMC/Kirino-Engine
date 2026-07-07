package com.cleanroommc.kirino.ecs.storage;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public final class HeapPrimitiveArray implements PrimitiveArray {
    private final PrimitiveArrayType type;
    private final int length;
    private final byte[] byteArray;
    private final short[] shortArray;
    private final int[] intArray;
    private final long[] longArray;
    private final float[] floatArray;
    private final double[] doubleArray;
    private final boolean[] booleanArray;

    HeapPrimitiveArray(byte[] array) {
        type = PrimitiveArrayType.BYTE;
        length = array.length;
        byteArray = array;
        shortArray = null;
        intArray = null;
        longArray = null;
        floatArray = null;
        doubleArray = null;
        booleanArray = null;
    }

    HeapPrimitiveArray(short[] array) {
        type = PrimitiveArrayType.SHORT;
        length = array.length;
        byteArray = null;
        shortArray = array;
        intArray = null;
        longArray = null;
        floatArray = null;
        doubleArray = null;
        booleanArray = null;
    }

    HeapPrimitiveArray(int[] array) {
        type = PrimitiveArrayType.INT;
        length = array.length;
        byteArray = null;
        shortArray = null;
        intArray = array;
        longArray = null;
        floatArray = null;
        doubleArray = null;
        booleanArray = null;
    }

    HeapPrimitiveArray(long[] array) {
        type = PrimitiveArrayType.LONG;
        length = array.length;
        byteArray = null;
        shortArray = null;
        intArray = null;
        longArray = array;
        floatArray = null;
        doubleArray = null;
        booleanArray = null;
    }

    HeapPrimitiveArray(float[] array) {
        type = PrimitiveArrayType.FLOAT;
        length = array.length;
        byteArray = null;
        shortArray = null;
        intArray = null;
        longArray = null;
        floatArray = array;
        doubleArray = null;
        booleanArray = null;
    }

    HeapPrimitiveArray(double[] array) {
        type = PrimitiveArrayType.DOUBLE;
        length = array.length;
        byteArray = null;
        shortArray = null;
        intArray = null;
        longArray = null;
        floatArray = null;
        doubleArray = array;
        booleanArray = null;
    }

    HeapPrimitiveArray(boolean[] array) {
        type = PrimitiveArrayType.BOOL;
        length = array.length;
        byteArray = null;
        shortArray = null;
        intArray = null;
        longArray = null;
        floatArray = null;
        doubleArray = null;
        booleanArray = array;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public byte getByte(int index) {
        Preconditions.checkState(type == PrimitiveArrayType.BYTE,
                "This is not a byte-typed array.");

        return byteArray[index];
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public short getShort(int index) {
        Preconditions.checkState(type == PrimitiveArrayType.SHORT,
                "This is not a short-typed array.");

        return shortArray[index];
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public int getInt(int index) {
        Preconditions.checkState(type == PrimitiveArrayType.INT,
                "This is not a integer-typed array.");

        return intArray[index];
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public long getLong(int index) {
        Preconditions.checkState(type == PrimitiveArrayType.LONG,
                "This is not a long-typed array.");

        return longArray[index];
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public float getFloat(int index) {
        Preconditions.checkState(type == PrimitiveArrayType.FLOAT,
                "This is not a float-typed array.");

        return floatArray[index];
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public double getDouble(int index) {
        Preconditions.checkState(type == PrimitiveArrayType.DOUBLE,
                "This is not a double-typed array.");

        return byteArray[index];
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public boolean getBool(int index) {
        Preconditions.checkState(type == PrimitiveArrayType.BOOL,
                "This is not a boolean-typed array.");

        return booleanArray[index];
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void setByte(int index, byte value) {
        Preconditions.checkState(type == PrimitiveArrayType.BYTE,
                "This is not a byte-typed array.");

        byteArray[index] = value;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void setShort(int index, short value) {
        Preconditions.checkState(type == PrimitiveArrayType.SHORT,
                "This is not a short-typed array.");

        shortArray[index] = value;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void setInt(int index, int value) {
        Preconditions.checkState(type == PrimitiveArrayType.INT,
                "This is not a integer-typed array.");

        intArray[index] = value;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void setLong(int index, long value) {
        Preconditions.checkState(type == PrimitiveArrayType.LONG,
                "This is not a long-typed array.");

        longArray[index] = value;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void setFloat(int index, float value) {
        Preconditions.checkState(type == PrimitiveArrayType.FLOAT,
                "This is not a float-typed array.");

        floatArray[index] = value;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void setDouble(int index, double value) {
        Preconditions.checkState(type == PrimitiveArrayType.DOUBLE,
                "This is not a double-typed array.");

        doubleArray[index] = value;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void setBool(int index, boolean value) {
        Preconditions.checkState(type == PrimitiveArrayType.BOOL,
                "This is not a boolean-typed array.");

        booleanArray[index] = value;
    }

    @Override
    public int length() {
        return length;
    }

    @NonNull
    @Override
    public PrimitiveArrayType type() {
        return type;
    }
}

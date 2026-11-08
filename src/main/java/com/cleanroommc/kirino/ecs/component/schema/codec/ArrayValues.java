package com.cleanroommc.kirino.ecs.component.schema.codec;

import java.util.Objects;

public final class ArrayValues implements PrimitiveValues {
    private final Object[] values;

    public ArrayValues(Object[] values) {
        this.values = Objects.requireNonNull(values);
    }

    public byte getByte(int column, int row) {
        return (byte) values[column];
    }

    public void setByte(int column, int row, byte value) {
        values[column] = value;
    }

    public short getShort(int column, int row) {
        return (short) values[column];
    }

    public void setShort(int column, int row, short value) {
        values[column] = value;
    }

    public int getInt(int column, int row) {
        return (int) values[column];
    }

    public void setInt(int column, int row, int value) {
        values[column] = value;
    }

    public long getLong(int column, int row) {
        return (long) values[column];
    }

    public void setLong(int column, int row, long value) {
        values[column] = value;
    }

    public float getFloat(int column, int row) {
        return (float) values[column];
    }

    public void setFloat(int column, int row, float value) {
        values[column] = value;
    }

    public double getDouble(int column, int row) {
        return (double) values[column];
    }

    public void setDouble(int column, int row, double value) {
        values[column] = value;
    }

    public boolean getBool(int column, int row) {
        return (boolean) values[column];
    }

    public void setBool(int column, int row, boolean value) {
        values[column] = value;
    }
}

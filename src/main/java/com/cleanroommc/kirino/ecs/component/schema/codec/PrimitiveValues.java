package com.cleanroommc.kirino.ecs.component.schema.codec;

public interface PrimitiveValues {
    byte getByte(int column, int row);

    void setByte(int column, int row, byte value);

    short getShort(int column, int row);

    void setShort(int column, int row, short value);

    int getInt(int column, int row);

    void setInt(int column, int row, int value);

    long getLong(int column, int row);

    void setLong(int column, int row, long value);

    float getFloat(int column, int row);

    void setFloat(int column, int row, float value);

    double getDouble(int column, int row);

    void setDouble(int column, int row, double value);

    boolean getBool(int column, int row);

    void setBool(int column, int row, boolean value);
}

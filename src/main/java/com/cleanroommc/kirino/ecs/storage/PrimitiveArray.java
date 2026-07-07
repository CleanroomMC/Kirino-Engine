package com.cleanroommc.kirino.ecs.storage;

import org.jspecify.annotations.NonNull;

public interface PrimitiveArray {

    /**
     * Getter for bytes.
     *
     * @param index The index
     * @return The value
     */
    byte getByte(int index);

    /**
     * Getter for shorts.
     *
     * @param index The index
     * @return The value
     */
    short getShort(int index);

    /**
     * Getter for integers.
     *
     * @param index The index
     * @return The value
     */
    int getInt(int index);

    /**
     * Getter for longs.
     *
     * @param index The index
     * @return The value
     */
    long getLong(int index);

    /**
     * Getter for floats.
     *
     * @param index The index
     * @return The value
     */
    float getFloat(int index);

    /**
     * Getter for doubles.
     *
     * @param index The index
     * @return The value
     */
    double getDouble(int index);

    /**
     * Getter for booleans.
     *
     * @param index The index
     * @return The value
     */
    boolean getBool(int index);

    /**
     * Setter for bytes.
     *
     * @param index The index
     * @param value The value
     */
    void setByte(int index, byte value);

    /**
     * Setter for shorts.
     *
     * @param index The index
     * @param value The value
     */
    void setShort(int index, short value);

    /**
     * Setter for integers.
     *
     * @param index The index
     * @param value The value
     */
    void setInt(int index, int value);

    /**
     * Setter for longs.
     *
     * @param index The index
     * @param value The value
     */
    void setLong(int index, long value);

    /**
     * Setter for floats.
     *
     * @param index The index
     * @param value The value
     */
    void setFloat(int index, float value);

    /**
     * Setter for doubles.
     *
     * @param index The index
     * @param value The value
     */
    void setDouble(int index, double value);

    /**
     * Setter for booleans.
     *
     * @param index The index
     * @param value The value
     */
    void setBool(int index, boolean value);

    /**
     * Returns the length.
     *
     * @return The length
     */
    int length();

    /**
     * Returns the type of this array.
     *
     * @return The type of the array.
     */
    @NonNull
    PrimitiveArrayType type();
}

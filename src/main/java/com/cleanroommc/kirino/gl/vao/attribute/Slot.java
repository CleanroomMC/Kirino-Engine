package com.cleanroommc.kirino.gl.vao.attribute;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public class Slot {
    private final Type type;
    private final int count;
    private final int size;

    private int divisor = 0;
    private InterpretationType interpretationType = InterpretationType.TO_FLOAT_KIND;
    private boolean normalize = false;

    @NonNull
    public Type getType() {
        return type;
    }

    public int getCount() {
        return count;
    }

    public int getSize() {
        return size;
    }

    public int getDivisor() {
        return divisor;
    }

    @NonNull
    public InterpretationType getInterpretationType() {
        return interpretationType;
    }

    public boolean isNormalize() {
        return normalize;
    }

    public Slot(@NonNull Type type, int count) {
        Preconditions.checkNotNull(type);
        Preconditions.checkArgument(!(count <= 0 || count > 4),
                "Component count=%s cannot be no greater than 0 or greater than 4.",
                count);

        this.type = type;
        this.count = count;
        this.size = type.length * count;
    }

    /**
     * @param divisor 0 for vertex data; 1 or more for instancing data
     */
    @NonNull
    public Slot setDivisor(int divisor) {
        Preconditions.checkArgument(divisor >= 0, "Divisor cannot be less than 0.");

        this.divisor = divisor;
        return this;
    }

    @NonNull
    public Slot setInterpretationType(@NonNull InterpretationType type) {
        Preconditions.checkNotNull(type);

        interpretationType = type;
        return this;
    }

    /**
     * This is only for {@link InterpretationType#TO_FLOAT_KIND}.
     * Especially for <code>byte -> float: ([0, 255] -> [0.0, 1.0])</code>.
     */
    @NonNull
    public Slot setNormalize(boolean flag) {
        normalize = flag;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, count, size, divisor, interpretationType, normalize);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Slot other = (Slot) obj;
        return count == other.count &&
                size == other.size &&
                divisor == other.divisor &&
                normalize == other.normalize &&
                type == other.type &&
                interpretationType == other.interpretationType;
    }
}

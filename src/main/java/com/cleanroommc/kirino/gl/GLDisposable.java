package com.cleanroommc.kirino.gl;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public abstract class GLDisposable implements Comparable<GLDisposable> {

    @NonNull
    public final String getName() {
        return getResourceIdentifier() + "@" + this.hashCode();
    }

    /**
     * @return The name of this type of resource
     */
    @NonNull
    public String getResourceIdentifier() {
        return this.getClass().getSimpleName();
    }

    /**
     * @return Larger the number, higher the priority it gets disposed
     */
    public int disposePriority() {
        return 0;
    }

    /**
     * @implNote Implement the <code>GL delete</code> logic here
     */
    protected abstract void dispose();

    @Override
    public final int compareTo(@NonNull GLDisposable other) {
        Preconditions.checkNotNull(other);

        return -Integer.compare(this.disposePriority(), other.disposePriority());
    }

    /**
     * Every {@link GLDisposable} resource is guaranteed to be disposed automatically at the end of
     * the whole program lifetime. Only call <code>disposeManually</code> when you want to dispose it right now.
     *
     * <p><b>Warning</b>: You must not access this object anymore after <code>disposeManually</code>, and
     * there's no system level guards that prevent you to access it.</p>
     */
    public final void disposeManually() {
        GLResourceManager.disposeEarly(this);
    }
}

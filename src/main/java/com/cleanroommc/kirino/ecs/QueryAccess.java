package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.api.EntityQuery;
import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import it.unimi.dsi.fastutil.objects.ReferenceSet;

import java.lang.invoke.MethodHandle;
import java.util.concurrent.CompletionException;

final class QueryAccess {
    private QueryAccess() {
    }

    static ReferenceSet<Class<? extends CleanComponent>> mustHave(EntityQuery query) {
        return MethodHolder.conditions(query, true);
    }

    static ReferenceSet<Class<? extends CleanComponent>> mustNotHave(EntityQuery query) {
        return MethodHolder.conditions(query, false);
    }

    private static final class MethodHolder {
        private static final Delegate DELEGATE = new Delegate(
            Preconditions.checkNotNull(ReflectionUtils.getFieldGetter(EntityQuery.class, "mustHave", ReferenceSet.class)),
            Preconditions.checkNotNull(ReflectionUtils.getFieldGetter(EntityQuery.class, "mustNotHave", ReferenceSet.class)));

        @SuppressWarnings("unchecked")
        private static ReferenceSet<Class<? extends CleanComponent>> conditions(EntityQuery query, boolean required) {
            try {
                return required ? (ReferenceSet<Class<? extends CleanComponent>>) DELEGATE.mustHave.invokeExact(query)
                    : (ReferenceSet<Class<? extends CleanComponent>>) DELEGATE.mustNotHave.invokeExact(query);
            } catch (Throwable failure) {
                Throwables.throwIfUnchecked(failure);
                throw new CompletionException(failure);
            }
        }

        private record Delegate(MethodHandle mustHave, MethodHandle mustNotHave) {
        }
    }
}

package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.api.EntityQuery;

import com.cleanroommc.kirino.ecs.api.CleanECSRuntime;
import com.cleanroommc.kirino.ecs.api.EntityCommands;
import com.cleanroommc.kirino.ecs.api.job.JobScheduler;
import com.cleanroommc.kirino.ecs.api.system.ExecutionContainer;
import com.cleanroommc.kirino.ecs.api.system.exegraph.SingleFlow;
import com.cleanroommc.kirino.ecs.api.system.exegraph.SystemExeFlowGraph;
import io.github.classgraph.ClassGraph;
import org.junit.jupiter.api.Test;

import java.lang.reflect.*;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class KernelBoundaryTest {
    @Test
    void kernelTypes_allImplementationClassesAreHidden() {
        int internalCount = 0;
        try (var scan = new ClassGraph().enableClassInfo().ignoreClassVisibility()
            .acceptPackagesNonRecursive("com.cleanroommc.kirino.ecs").scan()) {
            for (var info : scan.getAllClasses()) {
                Class<?> type = info.loadClass(false);
                if (type.getEnclosingClass() != null || !production(type)) {
                    continue;
                }
                assertFalse(Modifier.isPublic(type.getModifiers()), type.getName());
                internalCount++;
            }
        }
        assertTrue(internalCount >= 30);
    }

    @Test
    void ownedObjects_cannotBeConstructedOrExtendedThroughPublicApi() {
        for (Class<?> type : Set.of(CleanECSRuntime.class, EntityManager.class,
            CleanEntityHandle.class, JobScheduler.class)) {
            assertTrue(Modifier.isFinal(type.getModifiers()), type.getName());
            for (Constructor<?> constructor : type.getDeclaredConstructors()) {
                assertFalse(accessible(constructor.getModifiers()), constructor.toString());
            }
        }
        for (Method method : EntityManager.class.getDeclaredMethods()) {
            assertFalse(Modifier.isProtected(method.getModifiers()), method.toString());
        }
        for (Field field : CleanEntityHandle.class.getDeclaredFields()) {
            assertFalse(accessible(field.getModifiers()), field.toString());
        }
    }

    @Test
    void apiEntityContracts_haveOpaqueImplementationsAndNoStructuralHooks() {
        assertTrue(com.cleanroommc.kirino.ecs.api.EntityManager.class.isInterface());
        assertTrue(com.cleanroommc.kirino.ecs.api.CleanEntityHandle.class.isInterface());
        assertTrue(com.cleanroommc.kirino.ecs.api.EntityManager.class.isAssignableFrom(EntityManager.class));
        assertTrue(com.cleanroommc.kirino.ecs.api.CleanEntityHandle.class.isAssignableFrom(CleanEntityHandle.class));
        assertEquals(Set.of("createEntity", "newQuery", "flush"), java.util.Arrays.stream(
            com.cleanroommc.kirino.ecs.api.EntityManager.class.getMethods())
            .map(Method::getName).collect(java.util.stream.Collectors.toSet()));
        assertEquals(0, com.cleanroommc.kirino.ecs.api.CleanEntityHandle.class.getFields().length);
        for (Field field : EntityQuery.class.getDeclaredFields()) {
            assertFalse(accessible(field.getModifiers()), field.toString());
        }
    }

    @Test
    void flowApi_exposesBuilderContractAndHidesGraphImplementation() throws Exception {
        assertTrue(Modifier.isFinal(SingleFlow.class.getModifiers()));
        assertTrue(Modifier.isFinal(ExecutionContainer.class.getModifiers()));
        assertEquals(Object.class, SingleFlow.class.getSuperclass());
        assertEquals(SystemExeFlowGraph.Builder.class, SingleFlow.class.getMethod("newBuilder",
            com.cleanroommc.kirino.ecs.api.world.CleanWorld.class, Class.class).getReturnType());
        assertEquals(Set.of("Builder"), java.util.Arrays.stream(SystemExeFlowGraph.class.getClasses())
            .map(Class::getSimpleName).collect(java.util.stream.Collectors.toSet()));
        assertThrows(NoSuchMethodException.class, () -> SystemExeFlowGraph.class.getMethod("joinSystem",
            com.cleanroommc.kirino.ecs.api.system.CleanSystem.class));
        assertThrows(ClassNotFoundException.class,
            () -> Class.forName("com.cleanroommc.kirino.ecs.api.system.exegraph.AbstractFlow"));
    }

    @Test
    void publicSignatures_doNotExposeKernelTypesOrMutableMetadata() {
        try (var scan = new ClassGraph().enableClassInfo()
            .acceptPackages("com.cleanroommc.kirino.ecs").scan()) {
            for (var info : scan.getAllClasses()) {
                Class<?> type = info.loadClass(false);
                if (!exported(type) || !production(type)) {
                    continue;
                }
                assertTrue(type.getName().startsWith("com.cleanroommc.kirino.ecs.api."),
                    "Public ECS type outside API: " + type.getName());
                Set<Type> visited = new HashSet<>();
                inspect(type.getGenericSuperclass(), visited);
                for (Type variable : type.getTypeParameters()) inspect(variable, visited);
                for (Type implemented : type.getGenericInterfaces()) inspect(implemented, visited);
                for (Field field : type.getDeclaredFields()) {
                    if (accessible(field.getModifiers())) inspect(field.getGenericType(), visited);
                }
                for (Method method : type.getDeclaredMethods()) {
                    if (!accessible(method.getModifiers())) continue;
                    inspect(method.getGenericReturnType(), visited);
                    inspectExecutable(method, visited);
                }
                for (Constructor<?> constructor : type.getDeclaredConstructors()) {
                    if (accessible(constructor.getModifiers())) inspectExecutable(constructor, visited);
                }
            }
        }
    }

    private static void inspectExecutable(Executable executable, Set<Type> visited) {
        for (Type parameter : executable.getGenericParameterTypes()) inspect(parameter, visited);
        for (Type exception : executable.getGenericExceptionTypes()) inspect(exception, visited);
        for (Type variable : executable.getTypeParameters()) inspect(variable, visited);
    }

    private static void inspect(Type type, Set<Type> visited) {
        if (type == null || !visited.add(type)) return;
        if (type instanceof Class<?> clazz) {
            if (clazz.isArray()) {
                inspect(clazz.getComponentType(), visited);
            } else if (clazz.getName().startsWith("com.cleanroommc.kirino.ecs.")) {
                assertTrue(exported(clazz), "Internal type leaked: " + clazz.getName());
                assertTrue(clazz.getName().startsWith("com.cleanroommc.kirino.ecs.api."),
                    "API signature depends on kernel package: " + clazz.getName());
            }
        } else if (type instanceof ParameterizedType parameterized) {
            inspect(parameterized.getRawType(), visited);
            inspect(parameterized.getOwnerType(), visited);
            for (Type argument : parameterized.getActualTypeArguments()) inspect(argument, visited);
        } else if (type instanceof GenericArrayType array) {
            inspect(array.getGenericComponentType(), visited);
        } else if (type instanceof TypeVariable<?> variable) {
            for (Type bound : variable.getBounds()) inspect(bound, visited);
        } else if (type instanceof WildcardType wildcard) {
            for (Type bound : wildcard.getUpperBounds()) inspect(bound, visited);
            for (Type bound : wildcard.getLowerBounds()) inspect(bound, visited);
        }
    }

    private static boolean exported(Class<?> type) {
        return Modifier.isPublic(type.getModifiers())
            && (type.getEnclosingClass() == null || exported(type.getEnclosingClass()));
    }

    private static boolean accessible(int modifiers) {
        return Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers);
    }

    private static boolean production(Class<?> type) {
        return type.getProtectionDomain().getCodeSource().getLocation().equals(
            CleanECSRuntime.class.getProtectionDomain().getCodeSource().getLocation());
    }
}

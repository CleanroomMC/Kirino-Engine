package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.component.CleanComponent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class EntityQueryTest {
    @Test
    void conditions_repeatedWithAndWithout_keepOnlyUniqueTypesInEachSet() {
        EntityQuery query = EntityQuery.query();
        assertSame(query, query.with(A.class).with(A.class).with(B.class).with(B.class));
        assertSame(query, query.without(B.class).without(B.class).without(A.class).without(A.class));
        assertEquals(2, query.mustHave.size());
        assertEquals(2, query.mustNotHave.size());
        assertTrue(query.mustHave.contains(A.class));
        assertTrue(query.mustHave.contains(B.class));
        assertTrue(query.mustNotHave.contains(A.class));
        assertTrue(query.mustNotHave.contains(B.class));
        assertTrue(EntityQuery.query().mustHave.isEmpty());
        assertTrue(EntityQuery.query().mustNotHave.isEmpty());
    }

    @Test
    void conditions_allCandidateCollections_matchRealManagerIncludingDuplicatesAndContradictions() {
        ComponentRegistry registry = new ComponentRegistry(new FieldRegistry(new StructRegistry()));
        registry.registerComponent("a", A.class, new MemberLayout());
        registry.registerComponent("b", B.class, new MemberLayout());
        registry.lock();
        EntityManager entities = new EntityManager(registry);
        entities.createEntity();
        entities.createEntity(new A());
        entities.createEntity(new B());
        entities.createEntity(new A(), new B());
        entities.flush();
        List<ArchetypeDataPool> pools = entities.startQuery(entities.newQuery());
        List<Supplier<Collection<Class<? extends CleanComponent>>>> factories = List.of(
            ArrayList::new, ObjectArrayList::new, ReferenceArrayList::new,
            ReferenceArraySet::new, ReferenceOpenHashSet::new);
        List<List<Class<? extends CleanComponent>>> requirements = List.of(
            List.of(), List.of(A.class), List.of(A.class, A.class), List.of(A.class, B.class));
        List<List<Class<? extends CleanComponent>>> exclusions = List.of(
            List.of(), List.of(B.class), List.of(B.class, B.class), List.of(A.class));
        for (var factory : factories) {
            for (var required : requirements) {
                for (var excluded : exclusions) {
                    Collection<Class<? extends CleanComponent>> mustHave = factory.get();
                    Collection<Class<? extends CleanComponent>> mustNotHave = factory.get();
                    mustHave.addAll(required);
                    mustNotHave.addAll(excluded);
                    EntityQuery query = entities.newQuery();
                    required.forEach(query::with);
                    excluded.forEach(query::without);
                    List<ArchetypeDataPool> expected = entities.startQuery(query);
                    for (ArchetypeDataPool pool : pools) {
                        boolean matches = true;
                        for (Class<? extends CleanComponent> component : mustHave) {
                            if (!pool.components.contains(component)) {
                                matches = false;
                                break;
                            }
                        }
                        if (matches) {
                            for (Class<? extends CleanComponent> component : mustNotHave) {
                                if (pool.components.contains(component)) {
                                    matches = false;
                                    break;
                                }
                            }
                        }
                        assertEquals(expected.contains(pool), matches);
                    }
                }
            }
        }
    }

    public static class A implements CleanComponent {}
    public static class B implements CleanComponent {}
}

package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import com.cleanroommc.kirino.ecs.api.component.scan.CleanComponentSignature;
import com.cleanroommc.kirino.ecs.api.component.scan.event.ComponentScanningEvent;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.FieldInfo;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jspecify.annotations.NonNull;

import java.lang.invoke.MethodHandle;
import java.util.*;

/**
 * Scan component classes and generate a list of register plans.
 * There is no class loading involved at all.
 *
 * @see com.cleanroommc.kirino.ecs.api.CleanECSRuntime
 */
final class ComponentScanningHelper {
    private ComponentScanningHelper() {
    }

    private static ObjectArrayList<FieldInfo> getValidFields(ClassInfo componentClassInfo, FieldRegistry fieldRegistry) {
        ObjectArrayList<FieldInfo> fields = new ObjectArrayList<>();

        for (FieldInfo field : componentClassInfo.getFieldInfo()) {
            String fieldClassName = field.getTypeDescriptor().toString();
            if (fieldRegistry.fieldTypeExists_ClassName(fieldClassName)) {
                fields.add(field);
            }
        }

        fields.sort(Comparator.comparing(FieldInfo::getName));

        return fields;
    }

    private static List<ComponentRegisterPlan> generatePlans(Map<String, ObjectArrayList<FieldInfo>> components, FieldRegistry fieldRegistry) {
        List<ComponentRegisterPlan> plans = new ObjectArrayList<>();

        Object2IntOpenHashMap<String> componentNameDuplicates = new Object2IntOpenHashMap<>();
        Object2ObjectOpenHashMap<String, String> componentNames = new Object2ObjectOpenHashMap<>();

        for (Map.Entry<String, ObjectArrayList<FieldInfo>> entry : components.entrySet()) {
            String componentName = ClassScanUtils.getClassSimpleName(entry.getKey());

            int duplicate = 0;
            if (componentNameDuplicates.containsKey(componentName)) {
                duplicate = componentNameDuplicates.getInt(componentName);
                componentNameDuplicates.put(componentName, duplicate + 1);
            } else {
                componentNameDuplicates.put(componentName, 1);
            }
            if (duplicate > 0) {
                componentName = componentName + "$" + duplicate;
            }
            componentNames.put(entry.getKey(), componentName);
        }

        for (Map.Entry<String, ObjectArrayList<FieldInfo>> entry : components.entrySet()) {
            String componentClass = entry.getKey();
            String componentName = componentNames.get(entry.getKey());
            MemberLayout memberLayout = new MemberLayout(entry.getValue().stream().map(FieldInfo::getName).toList());
            String[] fieldTypeNames = new String[entry.getValue().size()];
            for (int i = 0; i < fieldTypeNames.length; i++) {
                String fieldClassName = entry.getValue().get(i).getTypeDescriptor().toString();
                fieldTypeNames[i] = fieldRegistry.getFieldTypeName_ClassName(fieldClassName);
            }

            plans.add(new ComponentRegisterPlan(componentName, componentClass, memberLayout, fieldTypeNames));
        }

        return plans;
    }

    /**
     * Retrieve a list of component register plans.
     *
     * @param event         The component scanning event
     * @param fieldRegistry The field registry
     * @return A list of component register plans
     */
    @NonNull
    public static List<@NonNull ComponentRegisterPlan> scanComponentClasses(@NonNull ComponentScanningEvent event, @NonNull FieldRegistry fieldRegistry) {

        Preconditions.checkNotNull(event);
        Preconditions.checkNotNull(fieldRegistry);

        // Чёткое указание класса, так как если он поменяет путь будут ошибка которую не сразу можно обнаружить
        String componentSignatureName = CleanComponentSignature.class.getName();
        String cleanComponentName = CleanComponent.class.getName();

        List<String> scanPackageNames = MethodHolder.getScanPackageNames(event);
        Map<String, ClassInfo> allClassInfos = ClassScanUtils.scan(scanPackageNames, componentSignatureName);

        Map<String, ObjectArrayList<FieldInfo>> components = new Object2ObjectAVLTreeMap<>();
        for (Map.Entry<String, ClassInfo> entry : allClassInfos.entrySet()) {
            if (!entry.getValue().implementsInterface(cleanComponentName)) {
                continue;
            }

            components.put(entry.getKey(), getValidFields(entry.getValue(), fieldRegistry));
        }

        return generatePlans(components, fieldRegistry);
    }

    private static final class MethodHolder {
        private static final Delegate DELEGATE;

        static {
            DELEGATE = new Delegate(ReflectionUtils.getFieldGetter(ComponentScanningEvent.class, "scanPackageNames", List.class));

            Preconditions.checkNotNull(DELEGATE.scanPackageNamesGetter);
        }

        @SuppressWarnings("unchecked")
        static List<String> getScanPackageNames(ComponentScanningEvent event) {
            try {
                return (List<String>) DELEGATE.scanPackageNamesGetter.invokeExact(event);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        record Delegate(MethodHandle scanPackageNamesGetter) {
        }
    }
}

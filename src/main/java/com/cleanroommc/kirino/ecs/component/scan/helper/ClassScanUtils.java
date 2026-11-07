package com.cleanroommc.kirino.ecs.component.scan.helper;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A util class that only works for {@link ComponentScanningHelper} and {@link StructScanningHelper}
 *
 * @see ComponentScanningHelper
 * @see StructScanningHelper
 */
final class ClassScanUtils {
    private ClassScanUtils() {
    }

    static String getClassSimpleName(String className) {
        int lastDot = className.lastIndexOf('.');
        return lastDot == -1 ? className : className.substring(lastDot + 1);
    }

    static Map<String, ClassInfo> scan(List<String> packages, String annotation) {
        final Map<String, ClassInfo> allClassInfos = new Object2ObjectAVLTreeMap<>();

        if (packages.isEmpty()) {
            return allClassInfos;
        }

        try (ScanResult scanResult = new ClassGraph()
            .enableAllInfo()
            .acceptPackages(packages.toArray(new String[0]))
            .scan()) {
            for (ClassInfo classInfo : scanResult.getClassesWithAnnotation(annotation)) {
                allClassInfos.put(classInfo.getName(), classInfo);
            }
        }
        return allClassInfos;
    }
}

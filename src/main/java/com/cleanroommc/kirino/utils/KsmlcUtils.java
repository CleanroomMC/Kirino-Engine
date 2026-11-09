package com.cleanroommc.kirino.utils;

import chaos.unity.nenggao.FileReportBuilder;
import com.cleanroommc.ksmlc.SourceFile;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public final class KsmlcUtils {

    @NonNull
    public static SourceFile buildKsmlcSourceFile(net.minecraft.util.@NonNull ResourceLocation fileRl) {
        Preconditions.checkNotNull(fileRl);

        return buildKsmlcSourceFile(new ResourceLocation(fileRl.getNamespace(), fileRl.getPath()));
    }

    @NonNull
    public static SourceFile buildKsmlcSourceFile(@NonNull ResourceLocation fileRl) {
        Preconditions.checkNotNull(fileRl);

        String fileContent = MinecraftResourceUtils.readText(fileRl, MinecraftResourceUtils.NewLineType.BACK_SLASH_N);
        return new SourceFile(
                fileRl.toString(),
                fileContent,
                FileReportBuilder.source(fileContent));
    }
}

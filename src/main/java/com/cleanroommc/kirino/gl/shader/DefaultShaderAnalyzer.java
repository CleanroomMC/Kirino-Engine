package com.cleanroommc.kirino.gl.shader;

import com.cleanroommc.kirino.gl.shader.parser.ASTTranslationUnit;
import com.cleanroommc.kirino.gl.shader.parser.GLSLResourceParser;
import com.cleanroommc.kirino.gl.shader.parser.ParseException;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.StringReader;

public final class DefaultShaderAnalyzer implements ShaderAnalyzer {

    private static final class ShaderMetaBuilder {

        private final ASTTranslationUnit astUnit;

        ShaderMetaBuilder(@NonNull ASTTranslationUnit astUnit) {
            this.astUnit = astUnit;
        }

        @NonNull
        ShaderMeta build() {
            return new ShaderMeta();
        }
    }

    @NonNull
    private static ASTTranslationUnit parse(@NonNull String source) throws ParseException {
        Preconditions.checkNotNull(source);

        return new GLSLResourceParser(new StringReader(source)).TranslationUnit();
    }

    @Nullable
    @Override
    public ShaderMeta analyze(@NonNull String shaderSource) {
        final ASTTranslationUnit astUnit;
        try {
            astUnit = parse(shaderSource);
        } catch (ParseException e) {
            return null;
        }

        return (new ShaderMetaBuilder(astUnit)).build();
    }
}

package com.cleanroommc.kirino.ui.font;

import com.cleanroommc.mcttf.extract.AssetSource;
import com.cleanroommc.mcttf.font.FontStyle;
import com.cleanroommc.mcttf.profile.FontDescriptor;
import com.cleanroommc.mcttf.profile.FontProvider;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

final class McTtfCacheKey {

    private static final int KEY_FORMAT_VERSION = 1;
    private static final int MCTTF_PIPELINE_REVISION = 1;

    private McTtfCacheKey() {
    }

    @NonNull
    static String create(
            @NonNull AssetSource assets,
            @NonNull String fontName,
            @NonNull String familyName,
            @NonNull FontStyle style) throws IOException {

        Preconditions.checkNotNull(assets);
        Preconditions.checkNotNull(fontName);
        Preconditions.checkNotNull(familyName);
        Preconditions.checkNotNull(style);

        DigestBuilder digest = new DigestBuilder();

        digest.putString("kirino:mcttf-font-cache-key");
        digest.putInt(KEY_FORMAT_VERSION);
        digest.putInt(MCTTF_PIPELINE_REVISION);

        digest.putString(fontName);
        digest.putString(familyName);

        digest.putString(style.name());
        digest.putString(style.subfamilyName());
        digest.putBoolean(style.bold());
        digest.putBoolean(style.italic());
        digest.putInt(style.macStyleBits());
        digest.putInt(style.fsSelectionBits());

        FontDescriptor descriptor = FontDescriptor.load(assets, fontName);
        List<FontProvider> providers = descriptor.providers();

        digest.putInt(providers.size());

        Set<Integer> claimedCodepoints = new HashSet<>();

        for (FontProvider provider : providers) {
            digest.putString(provider.type());

            switch (provider.type()) {
                case "bitmap" -> hashBitmapProvider(digest, assets, provider, claimedCodepoints);
                case "legacy_unicode" -> hashLegacyUnicodeProvider(digest, assets, provider, claimedCodepoints);
                case "space" -> hashSpaceProvider(digest, provider, claimedCodepoints);
                default -> throw new IllegalStateException("Unsupported provider type: " + provider.type());
            }
        }

        return digest.finishHex();
    }

    private static void hashBitmapProvider(
            DigestBuilder digest,
            AssetSource assets,
            FontProvider provider,
            Set<Integer> claimedCodepoints) throws IOException {

        digest.putString(provider.file());
        digest.putInt(provider.height());
        digest.putInt(provider.ascent());

        List<String> rows = provider.chars();
        digest.putInt(rows.size());

        int columns = 0;

        for (String row : rows) {
            digest.putString(row);
            columns = Math.max(columns, row.codePointCount(0, row.length()));
        }

        hashRequiredAsset(digest, assets, toTexturePath(provider.file()));

        if (rows.isEmpty() || columns == 0) {
            return;
        }

        for (String row : rows) {
            for (int offset = 0; offset < row.length();) {
                int codepoint = row.codePointAt(offset);
                offset += Character.charCount(codepoint);

                if (codepoint != 0) {
                    claimedCodepoints.add(codepoint);
                }
            }
        }
    }

    private static void hashSpaceProvider(
            DigestBuilder digest,
            FontProvider provider,
            Set<Integer> claimedCodepoints) {

        List<Integer> codepoints = new ArrayList<>(provider.advances().keySet());

        Collections.sort(codepoints);

        digest.putInt(codepoints.size());

        for (int codepoint : codepoints) {
            digest.putInt(codepoint);
            digest.putInt(provider.advances().get(codepoint));

            claimedCodepoints.add(codepoint);
        }
    }

    private static void hashLegacyUnicodeProvider(
            DigestBuilder digest,
            AssetSource assets,
            FontProvider provider,
            Set<Integer> claimedCodepoints) throws IOException {

        digest.putString(provider.template());
        digest.putString(provider.sizes());
        digest.putInt(provider.height());
        digest.putInt(provider.ascent());

        byte[] sizes = hashRequiredAsset(digest, assets, toFontPath(provider.sizes()));

        Map<String, byte[]> pages = new LinkedHashMap<>();

        for (int codepoint = 0; codepoint < sizes.length; codepoint++) {
            int glyphSize = sizes[codepoint] & 0xFF;

            if (glyphSize == 0 || claimedCodepoints.contains(codepoint)) {
                continue;
            }

            int page = codepoint >>> 8;

            String pagePath = toTexturePath(provider.template().replace(
                    "%s",
                    String.format(Locale.ROOT, "%02x", page)));

            if (!pages.containsKey(pagePath)) {
                pages.put(pagePath, assets.exists(pagePath) ? readAllBytes(assets, pagePath) : null);
            }

            if (pages.get(pagePath) != null) {
                claimedCodepoints.add(codepoint);
            }
        }

        digest.putInt(pages.size());

        for (Map.Entry<String, byte[]> entry : pages.entrySet()) {
            digest.putString(entry.getKey());
            digest.putBoolean(entry.getValue() != null);

            if (entry.getValue() != null) {
                digest.putBytes(entry.getValue());
            }
        }
    }

    private static byte[] hashRequiredAsset(
            DigestBuilder digest,
            AssetSource assets,
            String path) throws IOException {

        byte[] bytes = readAllBytes(assets, path);

        digest.putString(path);
        digest.putBytes(bytes);

        return bytes;
    }

    private static byte[] readAllBytes(
            AssetSource assets,
            String path) throws IOException {

        try (InputStream input = assets.open(path)) {
            return ByteStreams.toByteArray(input);
        }
    }

    private static String namespace(String id) {
        int separator = id.indexOf(':');
        return separator < 0 ? "minecraft" : id.substring(0, separator);
    }

    private static String idPath(String id) {
        int separator = id.indexOf(':');
        return separator < 0 ? id : id.substring(separator + 1);
    }

    private static String toTexturePath(String id) {
        return "assets/" + namespace(id) + "/textures/" + idPath(id);
    }

    private static String toFontPath(String id) {
        String path = idPath(id);

        if (path.startsWith("font/")) {
            path = path.substring("font/".length());
        }

        return "assets/" + namespace(id) + "/font/" + path;
    }

    private static final class DigestBuilder {

        private static final char[] HEX = "0123456789abcdef".toCharArray();

        private final MessageDigest digest;

        private DigestBuilder() {
            try {
                digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                throw new AssertionError("SHA-256 is required.", e);
            }
        }

        private void putBoolean(boolean value) {
            digest.update((byte) (value ? 1 : 0));
        }

        private void putInt(int value) {
            digest.update((byte) (value >>> 24));
            digest.update((byte) (value >>> 16));
            digest.update((byte) (value >>> 8));
            digest.update((byte) value);
        }

        private void putString(String value) {
            if (value == null) {
                putInt(-1);
                return;
            }

            putBytes(value.getBytes(StandardCharsets.UTF_16BE));
        }

        private void putBytes(byte[] value) {
            putInt(value.length);
            digest.update(value);
        }

        private String finishHex() {
            byte[] bytes = digest.digest();
            char[] chars = new char[bytes.length * 2];

            for (int i = 0; i < bytes.length; i++) {
                int value = bytes[i] & 0xFF;

                chars[i * 2] = HEX[value >>> 4];
                chars[i * 2 + 1] = HEX[value & 0x0F];
            }

            return new String(chars);
        }
    }
}

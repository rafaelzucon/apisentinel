package br.com.apisentinel.util;

public final class NameNormalizer {
    private NameNormalizer() {
    }

    public static String toKebab(String s) {
        if (s == null) return null;
        String cleaned = s.trim().replaceAll("[^a-zA-Z0-9]+", "-").replaceAll("-{2,}", "-").toLowerCase();
        if (cleaned.startsWith("-")) cleaned = cleaned.substring(1);
        if (cleaned.endsWith("-")) cleaned = cleaned.substring(0, cleaned.length() - 1);
        return cleaned;
    }

    public static String normalize(String name, String friendlyName, String exposure, String accessScope) {
        String result = (name == null || name.isBlank()) ? toKebab(friendlyName) : name;
        if ("EXTERNAL".equalsIgnoreCase(accessScope)) result = result + "-gtw";
        return result;
    }
}

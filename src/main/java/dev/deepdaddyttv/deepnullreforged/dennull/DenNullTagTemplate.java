package dev.deepdaddyttv.deepnullreforged.dennull;

public final class DenNullTagTemplate {
    private DenNullTagTemplate() {
    }

    public static String render(String template, String playerName, DenNullEntry entry, int counter) {
        String safeTemplate = template == null || template.isBlank() ? DenNullUpgradeData.DEFAULT_TAG_TEMPLATE : template;
        String type = entry == null ? "Entity" : entry.displayName();
        String id = entry == null ? "" : entry.entityType().toString();
        String paddedCounter = String.format("%03d", Math.max(1, counter));
        return safeTemplate
                .replace("{player}", playerName == null || playerName.isBlank() ? "Player" : playerName)
                .replace("{type}", type)
                .replace("{entity}", type)
                .replace("{id}", id)
                .replace("{counter}", paddedCounter);
    }
}

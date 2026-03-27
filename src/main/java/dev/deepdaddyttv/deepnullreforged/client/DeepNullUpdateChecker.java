package dev.deepdaddyttv.deepnullreforged.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.deepdaddyttv.deepnullreforged.DeepNullConfig;
import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class DeepNullUpdateChecker {
    private static final String GITHUB_LATEST_RELEASE_API = "https://api.github.com/repos/MMFQDEATH/DeepNull-Reforged/releases/latest";
    private static final String CURSEFORGE_PROJECT_URL = "https://www.curseforge.com/minecraft/mc-mods/deepnull-reforged";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private static boolean started;
    private static boolean notified;
    private static CompletableFuture<ReleaseInfo> pendingRequest;

    private DeepNullUpdateChecker() {
    }

    public static void tick(Minecraft minecraft) {
        if (!DeepNullConfig.isUpdateCheckerEnabled()) {
            pendingRequest = null;
            return;
        }
        if (notified || minecraft.player == null) {
            return;
        }

        if (!started) {
            started = true;
            pendingRequest = requestLatestRelease();
            return;
        }

        if (pendingRequest == null || !pendingRequest.isDone()) {
            return;
        }

        ReleaseInfo info;
        try {
            info = pendingRequest.join();
        } catch (Exception exception) {
            notified = true;
            DeepNullReforged.LOGGER.debug("DeepNull update check failed", exception);
            return;
        }

        notified = true;
        if (info == null || !isNewerVersion(info.version(), DeepNullReforged.MOD_VERSION)) {
            return;
        }

        minecraft.player.sendSystemMessage(buildUpdateMessage(info.version()));
    }

    private static CompletableFuture<ReleaseInfo> requestLatestRelease() {
        HttpRequest request = HttpRequest.newBuilder(URI.create(GITHUB_LATEST_RELEASE_API))
                .GET()
                .timeout(Duration.ofSeconds(5))
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .header("User-Agent", "DeepNull-Reforged-Update-Checker")
                .build();

        return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() < 200 || response.statusCode() >= 300) {
                        throw new IllegalStateException("Unexpected GitHub response: " + response.statusCode());
                    }
                    return parseReleaseInfo(response.body());
                })
                .exceptionally(exception -> {
                    DeepNullReforged.LOGGER.debug("Unable to fetch DeepNull release information", exception);
                    return null;
                });
    }

    private static ReleaseInfo parseReleaseInfo(String body) {
        JsonObject root = JsonParser.parseString(body).getAsJsonObject();
        String tagName = root.has("tag_name") ? root.get("tag_name").getAsString() : "";
        String normalizedVersion = normalizeVersion(tagName);
        if (normalizedVersion.isEmpty()) {
            throw new IllegalStateException("GitHub latest release did not include a usable tag_name");
        }
        return new ReleaseInfo(normalizedVersion);
    }

    private static MutableComponent buildUpdateMessage(String latestVersion) {
        MutableComponent prefix = Component.literal("[DeepNull Reforged] ").withStyle(ChatFormatting.AQUA);
        MutableComponent body = Component.literal("A newer version is available on CurseForge: v" + latestVersion + " ")
                .withStyle(ChatFormatting.YELLOW);
        MutableComponent link = Component.literal("Open CurseForge")
                .setStyle(Style.EMPTY
                        .withColor(ChatFormatting.GOLD)
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent.OpenUrl(URI.create(CURSEFORGE_PROJECT_URL)))
                        .withHoverEvent(new HoverEvent.ShowText(Component.literal(CURSEFORGE_PROJECT_URL))));
        return prefix.append(body).append(link);
    }

    private static boolean isNewerVersion(String remoteVersion, String localVersion) {
        List<Integer> remoteParts = versionParts(remoteVersion);
        List<Integer> localParts = versionParts(localVersion);
        int max = Math.max(remoteParts.size(), localParts.size());
        for (int i = 0; i < max; i++) {
            int remote = i < remoteParts.size() ? remoteParts.get(i) : 0;
            int local = i < localParts.size() ? localParts.get(i) : 0;
            if (remote != local) {
                return remote > local;
            }
        }
        return false;
    }

    private static List<Integer> versionParts(String version) {
        String normalized = normalizeVersion(version);
        String[] rawParts = normalized.split("[^0-9]+");
        List<Integer> parts = new ArrayList<>(rawParts.length);
        for (String rawPart : rawParts) {
            if (!rawPart.isEmpty()) {
                parts.add(Integer.parseInt(rawPart));
            }
        }
        return parts;
    }

    private static String normalizeVersion(String version) {
        String normalized = version == null ? "" : version.trim();
        if (normalized.startsWith("v") || normalized.startsWith("V")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    private record ReleaseInfo(String version) {
    }
}

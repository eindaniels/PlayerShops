package de.eindaniel.playerShops.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.BiConsumer;
import java.util.logging.Level;

/**
 * VersionChecker – checks a GitHub repository for the latest release
 * and compares it against the plugin's current version.
 */
public class VersionChecker {

    private static final String GITHUB_API = "https://api.github.com/repos/%s/%s/releases/latest";

    public enum Result {
        UP_TO_DATE,
        OUTDATED,
        UNKNOWN
    }

    private final JavaPlugin plugin;
    private final String user;
    private final String repo;

    public VersionChecker(JavaPlugin plugin, String githubUser, String githubRepo) {
        this.plugin = plugin;
        this.user   = githubUser;
        this.repo   = githubRepo;
    }

    /**
     * Runs the version check asynchronously.
     * The callback is called on the main thread with the result and the latest version string.
     *
     * @param callback BiConsumer<Result, String> – result + latest tag name (or current version on error)
     */
    public void check(BiConsumer<Result, String> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String current = plugin.getDescription().getVersion();
            try {
                String apiUrl  = String.format(GITHUB_API, user, repo);
                String tagName = fetchLatestTag(apiUrl);

                // Strip common prefixes like "v1.2.3" → "1.2.3"
                String latest = tagName.replaceFirst("^[vV]", "");
                String clean  = current.replaceFirst("^[vV]", "");

                Result result = compareVersions(clean, latest) < 0 ? Result.OUTDATED : Result.UP_TO_DATE;

                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(result, latest));

            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "[VersionChecker] Failed to check for updates: " + e.getMessage());
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(Result.UNKNOWN, current));
            }
        });
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private String fetchLatestTag(String apiUrl) throws Exception {
        HttpURLConnection con = (HttpURLConnection) new URL(apiUrl).openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Accept", "application/vnd.github+json");
        con.setRequestProperty("User-Agent", plugin.getName() + "-VersionChecker");
        con.setConnectTimeout(5_000);
        con.setReadTimeout(5_000);

        int status = con.getResponseCode();
        if (status != 200) {
            throw new RuntimeException("GitHub API returned HTTP " + status);
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }

        // Minimal JSON parsing – no external library needed
        String body = sb.toString();
        return extractJsonString(body, "tag_name");
    }

    /**
     * Extracts the value of a simple string field from a JSON body.
     * Works for flat fields like "tag_name":"v1.2.3".
     */
    private String extractJsonString(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx == -1) throw new RuntimeException("Key '" + key + "' not found in response.");

        int start = json.indexOf('"', idx + search.length() + 1); // skip : and whitespace
        int end   = json.indexOf('"', start + 1);
        if (start == -1 || end == -1) throw new RuntimeException("Could not parse value for '" + key + "'.");

        return json.substring(start + 1, end);
    }

    /**
     * Semantic version comparison (major.minor.patch).
     * Returns negative if a < b, 0 if equal, positive if a > b.
     */
    private int compareVersions(String a, String b) {
        String[] pa = a.split("[.\\-]");
        String[] pb = b.split("[.\\-]");
        int len = Math.max(pa.length, pb.length);
        for (int i = 0; i < len; i++) {
            int na = i < pa.length ? parseIntSafe(pa[i]) : 0;
            int nb = i < pb.length ? parseIntSafe(pb[i]) : 0;
            if (na != nb) return Integer.compare(na, nb);
        }
        return 0;
    }

    private int parseIntSafe(String s) {
        try { return Integer.parseInt(s); }
        catch (NumberFormatException e) { return 0; }
    }
}
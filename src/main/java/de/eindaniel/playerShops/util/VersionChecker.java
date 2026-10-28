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
 *
 * Optionally includes pre-releases (e.g. "1.2.0-beta.1") in the check.
 */
public class VersionChecker {

    /** Latest stable release only (ignores pre-releases and drafts). */
    private static final String GITHUB_API_STABLE =
            "https://api.github.com/repos/%s/%s/releases/latest";

    /** Newest release of any kind (stable or pre-release), newest first. Only the first entry is needed. */
    private static final String GITHUB_API_ALL =
            "https://api.github.com/repos/%s/%s/releases?per_page=1";

    public enum Result {
        UP_TO_DATE,
        OUTDATED,
        UNKNOWN
    }

    private final JavaPlugin plugin;
    private final String user;
    private final String repo;
    private final boolean includePrereleases;

    /** Stable releases only (previous behaviour). */
    public VersionChecker(JavaPlugin plugin, String githubUser, String githubRepo) {
        this(plugin, githubUser, githubRepo, false);
    }

    /**
     * @param includePrereleases if true, pre-releases are also considered as "latest version"
     */
    public VersionChecker(JavaPlugin plugin, String githubUser, String githubRepo, boolean includePrereleases) {
        this.plugin = plugin;
        this.user   = githubUser;
        this.repo   = githubRepo;
        this.includePrereleases = includePrereleases;
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
                String template = includePrereleases ? GITHUB_API_ALL : GITHUB_API_STABLE;
                String apiUrl   = String.format(template, user, repo);
                String tagName  = fetchLatestTag(apiUrl);

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

        // Minimal JSON parsing – no external library needed.
        // For the list endpoint (JSON array) the first "tag_name" belongs to the newest release,
        // so the same simple extraction works for both endpoints.
        String body = sb.toString();
        return extractJsonString(body, "tag_name");
    }

    /**
     * Extracts the value of a simple string field from a JSON body.
     * Returns the first occurrence of the key.
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
     * Schema: <core>-build.<n>-<channel>   z. B. 26.3-build.1-beta, 26.3-build.1-stable
     * Reihenfolge: core (26.3) → Build-Nummer → Channel (alpha < beta < rc < stable)
     */
    private int compareVersions(String a, String b) {
        int[] va = parseVersion(a);
        int[] vb = parseVersion(b);
        for (int i = 0; i < va.length; i++) {
            if (va[i] != vb[i]) return Integer.compare(va[i], vb[i]);
        }
        return 0;
    }

    /** Liefert {major, minor, patch, build, channelRank}. */
    private int[] parseVersion(String raw) {
        String v = raw.replaceFirst("^[vV]", "").split("\\+", 2)[0];
        String[] parts = v.split("-");

        String[] core = parts[0].split("\\.");
        int major = core.length > 0 ? parseIntSafe(core[0]) : 0;
        int minor = core.length > 1 ? parseIntSafe(core[1]) : 0;
        int patch = core.length > 2 ? parseIntSafe(core[2]) : 0;

        int build = 0;
        int channel = 100; // kein Channel angegeben = stable

        for (int i = 1; i < parts.length; i++) {
            String p = parts[i].toLowerCase();
            if (p.startsWith("build.")) {
                build = parseIntSafe(p.substring(6));
            } else if (p.matches("[a-z]+\\.\\d+")) {
                // Altes Schema: beta.11, rc.2 → Channel + Build-Nummer
                String[] x = p.split("\\.");
                channel = channelRank(x[0]);
                build = parseIntSafe(x[1]);
            } else {
                channel = channelRank(p);
            }
        }

        return new int[] { major, minor, patch, build, channel };
    }

    private int channelRank(String name) {
        switch (name) {
            case "alpha":  return 0;
            case "beta":   return 1;
            case "rc":     return 2;
            case "stable": return 100;
            default:       return 1; // unbekannt = wie beta
        }
    }

    private int parseIntSafe(String s) {
        try { return Integer.parseInt(s); }
        catch (NumberFormatException e) { return 0; }
    }
}
package com.dekhi.dekhi.util;

import android.util.Log;
import com.dekhi.dekhi.data.entity.Channel;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class M3UParser {
    private static final String TAG = "IPTV_DEBUG";
    private static final String EXTINF_PREFIX = "#EXTINF:";
    private static final String EXTGRP_PREFIX = "#EXTGRP:";
    private static final int BATCH_SIZE = 1000;

    private static final Pattern LOGO_PATTERN = Pattern.compile("(?:tvg-logo|logo|thumb|image)=(?:\"([^\"]*)\"|'([^']*)'|([^\\s,]*))", Pattern.CASE_INSENSITIVE);
    private static final Pattern GROUP_PATTERN = Pattern.compile("group-title=(?:\"([^\"]*)\"|'([^']*)'|([^\\s,]*))", Pattern.CASE_INSENSITIVE);
    private static final Pattern NAME_PATTERN = Pattern.compile("tvg-name=(?:\"([^\"]*)\"|'([^']*)'|([^\\s,]*))", Pattern.CASE_INSENSITIVE);

    public interface BatchListener {
        void onBatchParsed(List<Channel> batch);
    }

    public static class ParseResult {
        public final String previewSnippet;
        public final int groupCount;
        public final int channelCount;

        public ParseResult(String previewSnippet, int groupCount, int channelCount) {
            this.previewSnippet = previewSnippet;
            this.groupCount = groupCount;
            this.channelCount = channelCount;
        }
    }

    public static ParseResult parse(InputStream inputStream, long playlistId, BatchListener listener) throws Exception {
        Log.d(TAG, "Parser: Starting optimized parse process...");
        List<Channel> currentBatch = new ArrayList<>(BATCH_SIZE);
        List<String> firstNames = new ArrayList<>();
        java.util.Set<String> groups = new java.util.HashSet<>();
        int totalChannels = 0;
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String firstLine = reader.readLine();
            if (firstLine == null || !firstLine.trim().startsWith("#EXTM3U")) {
                throw new Exception("Invalid M3U: Missing #EXTM3U header.");
            }

            String line;
            String currentName = "";
            String currentLogo = "";
            String currentGroup = "Uncategorized";

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.startsWith(EXTINF_PREFIX)) {
                    int lastComma = line.lastIndexOf(',');
                    if (lastComma != -1) {
                        currentName = line.substring(lastComma + 1).trim();
                    }
                    
                    currentLogo = fastExtract(line, LOGO_PATTERN);
                    String groupAttr = fastExtract(line, GROUP_PATTERN);
                    if (!groupAttr.isEmpty()) currentGroup = groupAttr;
                    
                    if (currentName.isEmpty()) {
                        currentName = fastExtract(line, NAME_PATTERN);
                    }
                } else if (line.startsWith(EXTGRP_PREFIX)) {
                    String group = line.substring(EXTGRP_PREFIX.length()).trim();
                    if (!group.isEmpty()) currentGroup = group;
                } else if (!line.startsWith("#")) {
                    if (!line.isEmpty() && isSafeUrl(line)) {
                        String name = currentName.isEmpty() ? "Channel " + (totalChannels + 1) : currentName;
                        currentBatch.add(new Channel(playlistId, name, isSafeUrl(currentLogo) ? currentLogo : "", line, currentGroup));
                        groups.add(currentGroup);
                        
                        if (firstNames.size() < 5) firstNames.add(name);
                        totalChannels++;

                        if (currentBatch.size() >= BATCH_SIZE) {
                            if (listener != null) listener.onBatchParsed(new ArrayList<>(currentBatch));
                            currentBatch.clear();
                        }
                    }
                    currentName = "";
                    currentLogo = "";
                    currentGroup = "Uncategorized";
                }
            }
            
            if (!currentBatch.isEmpty() && listener != null) {
                listener.onBatchParsed(currentBatch);
            }
        }

        StringBuilder snippet = new StringBuilder();
        for (int i = 0; i < firstNames.size(); i++) {
            snippet.append(firstNames.get(i));
            if (i < firstNames.size() - 1) snippet.append(", ");
        }
        if (totalChannels > 5) snippet.append("...");

        return new ParseResult(snippet.toString(), groups.size(), totalChannels);
    }

    private static boolean isSafeUrl(String url) {
        if (url == null || url.isEmpty()) return false;
        String lower = url.toLowerCase();
        return lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("content://");
    }

    private static String fastExtract(String line, Pattern pattern) {
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            if (matcher.group(1) != null) return matcher.group(1).trim();
            if (matcher.group(2) != null) return matcher.group(2).trim();
            if (matcher.group(3) != null) return matcher.group(3).trim();
        }
        return "";
    }
}

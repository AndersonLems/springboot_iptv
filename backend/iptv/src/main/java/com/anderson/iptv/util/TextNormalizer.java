package com.anderson.iptv.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public final class TextNormalizer {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern BRACKETS = Pattern.compile("\\[.*?\\]");
    private static final Pattern PARENS = Pattern.compile("\\(.*?\\)");
    private static final Pattern SEASON_EPISODE = Pattern.compile("(?i)\\bS(\\d{1,2})E(\\d{1,2})\\b");
    private static final Pattern EPISODE = Pattern.compile("(?i)\\bEP\\s*\\d{1,3}\\b");
    private static final Pattern BAD_TOKENS = Pattern.compile("(?i)\\b(4k|hdr|dublado|dubbed|legenado|legendado)\\b");
    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9\\s]");
    private static final Pattern MULTI_SPACE = Pattern.compile("\\s+");

    private TextNormalizer() {}

    public static String normalizeTitle(String input) {
        if (input == null) {
            return "";
        }
        String s = input.toLowerCase();
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = DIACRITICS.matcher(s).replaceAll("");
        s = SEASON_EPISODE.matcher(s).replaceAll("");
        s = EPISODE.matcher(s).replaceAll("");
        s = BRACKETS.matcher(s).replaceAll("");
        s = PARENS.matcher(s).replaceAll("");
        s = BAD_TOKENS.matcher(s).replaceAll("");
        s = NON_ALNUM.matcher(s).replaceAll(" ");
        s = MULTI_SPACE.matcher(s).replaceAll(" ").trim();
        return s;
    }
}

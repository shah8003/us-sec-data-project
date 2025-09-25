package com.x2iq.secdataextractionbackend.services.forms;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class Form8KExtractorService {
    public String extract8KSectionByText(String html, String startItemKey) {
        if (html == null || html.isEmpty()) return "";

        // Normalize HTML quirks and Unicode spaces early
        String cleanedHtml = html
                .replace("&nbsp;", " ")
                .replace("&#160;", " ")
                .replace("&#8201;", " ")
                .replaceAll("(?i)item\\s*THPS", "Item ")
                .replaceAll("[\\u00A0\\u2007\\u2009\\u202F\\uFEFF]", " ");

        Document doc = Jsoup.parse(cleanedHtml);
        String text = doc.text();

        // Normalize whitespace
        text = text.replaceAll("[ \\t\\x0B\\f\\r]+", " ").trim();

        // Clean or normalize specific phrases
        text = cleanAndNormalizeText(text);

        // Pattern for matching spaces including non‐breaking etc:
        String spacePattern = "[ \\u00A0\\u2007\\u2009\\u202F\\uFEFF]*";

        String escapedStartItem = Pattern.quote(startItemKey);

        // Improved start pattern: allows optional symbol before, then "Item <startItemKey>"
        // followed by either period, space, or directly a letter, not strictly requiring the dot
        Pattern startPattern = Pattern.compile(
                "(?i)"  // case-insensitive
                        + "(?:[\\p{Punct}\\p{So}]" + spacePattern + ")*"  // optional symbol(s) or bullet/box etc
                        + "Item" + spacePattern + escapedStartItem
                        + "(?:\\.|\\b|\\s)"  // accept a period, or word boundary, or space after number
                , Pattern.DOTALL
        );

        Matcher startMatcher = startPattern.matcher(text);
        int start = -1;
        while (startMatcher.find()) {
            start = startMatcher.start();  // use last occurrence if multiple
        }
        if (start == -1) {
            return "Data not found for item "+startItemKey;
        }

        int end = text.length();
        double startNum = parseItemNumber(startItemKey);

        // Find next greater item to mark the end boundary
        for (String item : get8KItemKeys()) {
            double itemNum = parseItemNumber(item);
            if (itemNum > startNum) {
                String escapedNextItem = Pattern.quote(item);
                Pattern nextItemPattern = Pattern.compile(
                        "(?i)"
                                + "(?:[\\p{Punct}\\p{So}]" + spacePattern + ")*"
                                + "Item" + spacePattern + escapedNextItem
                                + "(?:\\.|\\b|\\s)"
                        , Pattern.DOTALL
                );
                Matcher nextMatcher = nextItemPattern.matcher(text);
                if (nextMatcher.find(start + 1)) {
                    end = nextMatcher.start();
                    break;
                }
            }
        }

        // Stop early if "SIGNATURE" appears before end
        int sigIdx = text.toUpperCase().lastIndexOf("SIGNATURES");
        if (sigIdx != -1 && sigIdx < end) {
            end = sigIdx;
        }

        return text.substring(start, end).trim().replaceFirst("^\\P{Alnum}+\\s+", "");
    }

    private String cleanAndNormalizeText(String text) {
        if (text == null || text.isEmpty()) return "";

        // Normalize unicode spaces etc.
        text = text.replaceAll("[\\u00A0\\u2007\\u2009\\u202F\\uFEFF]", " ");

        // Remove duplicate "item item"
        text = text.replaceAll("(?i)\\bitem\\s+item\\b", "item");

        // Specific phrase replacements only — do NOT inject generic "Item"
        text = text.replaceAll("(?i)\\bitem\\s+(\\d+\\.\\d{2})\\s+of\\b", "item-$1-of");
        text = text.replaceAll("(?i)\\bsection\\s+item\\s+(\\d+\\.\\d{2})\\b", "section-item-$1");
        text = text.replaceAll("(?i)\\bsection\\s+of\\s+item\\b", "section-for-item-");
        text = text.replaceAll("(?i)\\bin\\s+item\\s+(\\d+\\.\\d{2})\\b", "in-item-$1");
        text = text.replaceAll("(?i)\\bin\\s+this\\s+item\\s+(\\d+\\.\\d{2})\\b", "in-this-item-$1");
        text = text.replaceAll("(?i)\\bin\\s+this\\s+item\\s+(\\d+\\.\\d{2})\\.", "in-this-item-$1");
        // Avoid matching phrases like "this Item 3.03" by converting them into non-matching format
        text = text.replaceAll("(?i)\\bthis\\s+item\\s+(\\d+\\.\\d{2})\\b", "this item-$1-");
        // Prevent false positives for references to items
        text = text.replaceAll("(?i)\\binto\\s+item\\s+(\\d+\\.\\d{2})\\b", "into item-$1-");
        text = text.replaceAll("(?i)\\bunder\\s+item\\s+(\\d+\\.\\d{2})\\b", "under item-$1-");
        text = text.replaceAll("(?i)\\bsee\\s+item\\s+(\\d+\\.\\d{2})\\b", "see item-$1-");
        text = text.replaceAll("(?i)\\bunder\\s+item\\s+(\\d+\\.\\d{2})\\b", "under item-$1-");
        text = text.replaceAll("(?i)\\bsee\\s+item\\s+(\\d+\\.\\d{2})\\b", "see item-$1-");
        text = text.replaceAll("(?i)\\bpursuant to\\s+item\\s+(\\d+\\.\\d{2})\\b", "pursuant to item-$1-");
        text = text.replaceAll("(?i)\\baccording to\\s+item\\s+(\\d+\\.\\d{2})\\b", "according to item-$1-");
        text = text.replaceAll("(?i)\\bper\\s+item\\s+(\\d+\\.\\d{2})\\b", "per item-$1-");
        text = text.replaceAll("(?i)\\bas described in\\s+item\\s+(\\d+\\.\\d{2})\\b", "as described in item-$1-");
        text = text.replaceAll("(?i)\\bsubject to\\s+item\\s+(\\d+\\.\\d{2})\\b", "subject to item-$1-");
        text = text.replace("Item 8.01Other Events", "Item 8.01 Other Events");
        text = text.replaceFirst("^\\P{Alnum}+\\s+", "");


        // Lowercase "Items Item" to "items"
        text = text.replaceAll("(?i)Items\\s+Item", "items");

        // Remove redundant multiple spaces
        text = text.replaceAll("\\s+", " ").trim();

        return text;
    }


    private double parseItemNumber(String item) {
        try {
            return Double.parseDouble(item.trim());
        } catch (NumberFormatException e) {
            return -1.0;
        }
    }

    private List<String> get8KItemKeys() {
        return Arrays.asList(
                "1.01", "1.02", "1.03", "1.04",
                "2.01", "2.02", "2.03", "2.04", "2.05", "2.06",
                "3.01", "3.02", "3.03",
                "4.01", "4.02",
                "5.01", "5.02", "5.03", "5.04", "5.05", "5.06", "5.07", "5.08",
                "6.01", "6.02", "6.03", "6.04", "6.05",
                "7.01",
                "8.01",
                "9.01"
        );
    }
}

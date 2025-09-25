package com.x2iq.secdataextractionbackend.services.forms;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class Form10QExtractorService {
    public String extractAll10QSections(String html, String itemNumber) {
        String startItem;

        // 🔹 Switch only for startItem
        switch (itemNumber) {
            case "1":
                startItem = "part1item1";
                break;
            case "2":
                startItem = "part1item2";
                break;
            case "3":
                startItem = "part1item3";
                break;
            case "4":
                startItem = "part1item4";
                break;
            case "1L":
                startItem = "part2item1";
                break;
            case "1A":
                startItem = "part2item1a";
                break;
            case "2U":
                startItem = "part2item2";
                break;
            case "3D":
                startItem = "part2item3";
                break;
            case "4M":
                startItem = "part2item4";
                break;
            case "5O":
                startItem = "part2item5";
                break;
            case "6E":
                startItem = "part2item6";
                break;
            default:
                return "Invalid item number";
        }

        // 🔹 Reuse your existing items list to find the endItem
        List<String> items = Arrays.asList(
                "part1item1", "part1item2", "part1item3", "part1item4",
                "part2item1", "part2item1a", "part2item2", "part2item3",
                "part2item4", "part2item5", "part2item6"
        );

        int idx = items.indexOf(startItem);
        String endItem = (idx + 1 < items.size()) ? items.get(idx + 1) : null;

        return extractSectionByHeading(html, startItem, endItem);
    }

    public String extractSectionByHeading(String html, String startItemKey, String endItemKey) {
        if (html == null || html.isEmpty()) {
            return "";
        }
        Document doc = Jsoup.parse(html);
        String text = doc.text();
        text = text.replace("'","’");
        text = text.replaceAll("\\s+", " ");
        text = text.replaceAll("\\.(?=\\S)", ". ");
        text = text.replaceAll(". — ",". ");
        // Normalize "ITEM X—Heading" → "Item X. Heading"
        text = text.replaceAll("(?i)ITEM\\s*(\\d+[A-Z]?)\\s*[—–-]\\s*", "Item $1. ");
        text = text.replace("to Item 2","to-Item-2");
        text = text.replace("Item ","Item ");
        text = text.replace("ITEM ","Item ");
        text = text.replace("SIGNATURES 43 i","Item 1. Financial Statements");
        text = text.replace("SIGNATURES 95 2","Item 1. Financial Statements");
        text = text.replace("SIGNATURES 64 iii","Item 1. Financial Statements");
        text = text.replace("PART I FINANCIAL INFORMATION","Item 1. Financial Statements");
        text = text.replace("Part I. Financial Information","Item 1. Financial Statements");
        text = text.replace("PART I – FINANCIAL INFORMATION","Item 1. Financial Statements");
        text = text.replace("Item 1: Financial Statements CULP","Item 1. Financial Statements");
        text = text.replace("FINANCIAL INFORMATION Item 1.","Item 1. Financial Statements");
        text = text.replace("ITEM 1: Financial Statements","Item 1. Financial Statements");
        text = text.replace("Financial Statements F-6 F-1","Item 1. Financial Statements");
        text = text.replace("SIGNATURES 43","Item 1. Financial Statements");
        text = text.replace("See “Part II, Item 2","See “Part II-Item-2");
        text = text.replace("EVERSOURCE ENERGY AND SUBSIDIARIES Management’s Discussion and Analysis of Financial Condition and Results of Operations","Item 2. Management’s Discussion and Analysis of Financial Condition and Results of Operations");
        text = text.replace("12 Item 2. Management’s Discussion and Analysis of Financial Conditions and Results","Item 2. Management’s Discussion and Analysis of Financial Condition and Results of Operations");
        text = text.replace("15 Item 2. Management's Discussion and Analysis of Financial Condition and Results of Operations","Item 2. Management’s Discussion and Analysis of Financial Conditions and Results of Operations");
        text = text.replace("15 Table of Contents ITEM 2. MANAGEMENT'S DISCUSSION AND ANALYSIS OF FINANCIAL CONDITION AND RESULTS OF OPERATIONS" ,"Item 2. Management’s Discussion and Analysis of Financial Conditions and Results of Operations");
        text = text.replace("Item 2: Management Discussion and Analysis of Financial Condition and Results of Operations", "Item 2. Management’s Discussion and Analysis of Financial Condition and Results of Operations");
        text = text.replace("Item 2. Unregistered Sales of Equity Securities, Use of Proceeds,","Item 2. Unregistered Sales of Equity Securities and Use of Proceeds");
        text = text.replace("Item 2. Unregistered Sales of Equity Securities","Item 2. Unregistered Sales of Equity Securities and Use of Proceeds");
        text = text.replace("Item 2: Management’s Discussion and Analysis of Financial Condition and Results of Operations","Item 2. Management’s Discussion and Analysis of Financial Condition and Results of Operations");
        text = text.replace("Item 3. Qualitative and Quantitative Disclosures About Market Risk Market","Item 3. Quantitative and Qualitative Disclosures About Market Risk");
        text = text.replace("Item lA. Risk Factors.","Item 1A. Risk Factors");
        text = text.replace("Item 1. Legal Proceeding","Item 1. Legal Proceedings");
        text = text.replace("Item 4. Mine Safety Disclosure","Item 4. Mine Safety Disclosures");
        Map<String, Pattern> tolerantPatterns = new HashMap<>();
        tolerantPatterns.put("part1item1", Pattern.compile("(?is)item\\\\s*1\\\\b.*?unaudited.*?condensed.*?financial.*?statements"));
        tolerantPatterns.put("part1item1", Pattern.compile("(?is)\\bitem\\s*1\\s*[-.:)]?\\s*(?:\\s|<[^>]+>)*financial\\s*(?:\\s|<[^>]+>)*statements"));
        Map<String, String> fullHeadings = new HashMap<>();
        fullHeadings.put("part1item1", "Item 1. Financial Statements");
        fullHeadings.put("part1item2", "Item 2. Management’s Discussion and Analysis of Financial Condition and Results of Operations");
        fullHeadings.put("part1item3", "Item 3. Quantitative and Qualitative Disclosures About Market Risk");
        fullHeadings.put("part1item4", "Item 4. Controls and Procedures");
        fullHeadings.put("part2item1", "Item 1. Legal Proceedings");
        fullHeadings.put("part2item1a", "Item 1A. Risk Factors");
        fullHeadings.put("part2item2", "Item 2. Unregistered Sales of Equity Securities and Use of Proceeds");
        fullHeadings.put("part2item3", "Item 3. Defaults Upon Senior Securities");
        fullHeadings.put("part2item4", "Item 4. Mine Safety Disclosures");
        fullHeadings.put("part2item5", "Item 5. Other Information");
        fullHeadings.put("part2item6", "Item 6. Exhibits");
        Pattern startTolerant = tolerantPatterns.get(startItemKey);
        String startHeading = null;

        if (startTolerant != null && startTolerant.matcher(text).find()) {
            startHeading = fullHeadings.get(startItemKey);
        } else {
            startHeading = fullHeadings.get(startItemKey);
        }

        if (startHeading == null) return "Item not available";

        String endHeading = null;
        if (endItemKey != null) {
            Pattern endTolerant = tolerantPatterns.get(endItemKey);
            if (endTolerant != null && endTolerant.matcher(text).find()) {
                endHeading = fullHeadings.get(endItemKey);
            } else {
                endHeading = fullHeadings.get(endItemKey);
            }
        }

        Pattern startPattern = Pattern.compile("(?i)" + Pattern.quote(startHeading));
        Pattern endPattern = (endHeading != null) ? Pattern.compile("(?i)" + Pattern.quote(endHeading)) : null;
        Matcher startMatcher = startPattern.matcher(text);
        if (!startMatcher.find()) {
            return "Item not found"; // start heading not in file
        }
        int start = -1;
        while (startMatcher.find()) {
            start = startMatcher.start();
        }

        if (start == -1) return "";

        int end = text.length();
// If a specific endHeading was given, try that first
        if (endPattern != null) {
            Matcher endMatcher = endPattern.matcher(text);
            if (endMatcher.find(start + 1)) {
                end = endMatcher.start();
            }
        }
// If no specific endHeading found, fall back to the next heading in order
        if (end == text.length()) {
            for (String heading : fullHeadings.values()) {
                Matcher m = Pattern.compile("(?i)" + Pattern.quote(heading)).matcher(text);
                if (m.find(start + 1)) {
                    end = Math.min(end, m.start());
                }
            }
        }

        // --- fallback: SIGNATURES marker ---
        int cutIndex = text.indexOf("SIGNATURES", start);
        if (cutIndex != -1 && cutIndex < end) {
            end = cutIndex;
        }

        // --- final safety check ---
        if (end > text.length()) {
            end = text.length();
        }
        if (end <= start) {
            return "";
        }

        String section = text.substring(start, end).trim();
        return section;

    }
}

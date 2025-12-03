package com.x2iq.secdataextractionbackend.services.forms;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.*;

@Service
public class SecDef14AExtractorService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // =========================
    // HEADINGS
    // =========================

    private static final Set<String> BENEFICIAL_HEADINGS = new HashSet<>(Arrays.asList(
            "SECURITY OWNERSHIP OF CERTAIN BENEFICIAL OWNERS AND MANAGEMENT",
            "SECURITY OWNERSHIP OF NEWS CORPORATION",
            "VOTING SECURITIES AND PRINCIPAL SHAREHOLDERS",
            "SECURITIES OWNERSHIP OF CERTAIN BENEFICIAL OWNERS AND MANAGEMENT",
            "BENEFICIAL OWNERSHIP OF SECURITIES",
            "SECURITY OWNERSHIP OF CERTAIN BENEFICIAL OWNERS AND MANAGEMENT AND RELATED STOCKHOLDER MATTERS",
            "STOCK OWNERSHIP",
            "BENEFICIAL OWNERSHIP OF COMPANY COMMON STOCK BY DIRECTORS, OFFICERS AND PRINCIPAL STOCKHOLDERS",
            "SECURITY OWNERSHIP OF CERTAIN BENEFICIAL OWNERS AND MANAGEMENT AND RELATED SHAREHOLDER MATTERS"
    ));

    private static final Set<String> EXEC_COMP_HEADINGS = new HashSet<>(Arrays.asList(
            "EXECUTIVE COMPENSATION",
            "EXECUTIVE COMPENSATION TABLES",
            "COMPENSATION OF EXECUTIVE OFFICERS",
            "EXECUTIVE COMPENSATION AND RELATED INFORMATION",
            "EXECUTIVE COMPENSATION — COMPENSATION DISCUSSION AND ANALYSIS",
            "EXECUTIVE COMPENSATION AND OTHER INFORMATION"
    ));

    private static final Set<String> DIRECTOR_COMP_HEADINGS = new HashSet<>(Arrays.asList(
            "COMPENSATION OF DIRECTORS",
            "DIRECTOR COMPENSATION",
            "TRUSTEE COMPENSATION",
            "ELEMENTS OF DIRECTOR COMPENSATION",
            "DIRECTOR COMPENSATION PROGRAM",
            "Director Compensation"
    ));

    private static final Set<String> EXEC_DIRECTOR_COMP_HEADINGS = new HashSet<>(Arrays.asList(
            "EXECUTIVE AND DIRECTOR COMPENSATION",
            "EXECUTIVE OFFICER AND DIRECTOR COMPENSATION"
    ));

    private static final Set<String> EXEC_OFFICER_HEADINGS = new HashSet<>(Arrays.asList(
            "EXECUTIVE OFFICERS",
            "EXECUTIVE OFFICERS OF THE COMPANY",
            "EXECUTIVE OFFICERS AND DIRECTORS",
            "CURRENT DIRECTORS",
            "INFORMATION ABOUT EXECUTIVE OFFICERS WHO ARE NOT DIRECTORS",
            "EXECUTIVE OFFICER COMPENSATION"
    ));

    private static final Set<String> AUDIT_HEADINGS = new HashSet<>(Arrays.asList(
            "AUDIT FEES",
            "PRINCIPAL ACCOUNTANT FEES AND SERVICES",
            "AUDIT FIRM FEES AND SERVICES",
            "FEES PAID TO INDEPENDENT REGISTERED PUBLIC ACCOUNTING FIRM",
            "FEES OF INDEPENDENT REGISTERED PUBLIC ACCOUNTING FIRM",
            "COMPANY’S INDEPENDENT REGISTERED PUBLIC ACCOUNTING FIRM",
            "THE COMPANY'S INDEPENDENT REGISTERED PUBLIC ACCOUNTING FIRM",
            "INDEPENDENT REGISTERED PUBLIC ACCOUNTING FIRM",
            "AUDIT AND NON-AUDIT FEES"
    ));

    // =========================
    // PUBLIC API
    // =========================

    public String extractSections(String html, String sectionKey) {
        Map<String, String> sections = new LinkedHashMap<>();
        if (html == null || html.isEmpty()) return "";

        Document doc = Jsoup.parse(html);
        if (doc.body() == null) return "";

        doc.select("br, p, div, tr, li").append("\\n");
        String raw = doc.body().text();
        if (raw == null) return "";

        String normalized = raw.replace("\\n", "\n")
                .replace('\u00A0', ' ')
                .replace('’', '\'')
                .replace('‘', '\'')
                .replace('“', '"')
                .replace('”', '"')
                .replaceAll("[ \t]+", " ")
                .trim();

        if (normalized.isEmpty()) return "";

        String[] lines = normalized.split("\\n+");

        boolean[] inToc = detectTableOfContentsBlocks(lines);

        List<HeadingCandidate> allHeadings = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            if (inToc[i]) continue;

            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            String key = classifySectionByAllowedHeadings(line);
            boolean isHeading = (key != null) || looksLikeHeading(line);
            if (!isHeading) continue;

            HeadingCandidate h = new HeadingCandidate();
            h.lineIndex = i;
            h.rawText = line;
            h.sectionKey = key;
            allHeadings.add(h);
        }

        if (allHeadings.isEmpty()) return "";

        Map<String, String> tocSections = extractSectionsUsingToc(lines, inToc, allHeadings);
        sections.putAll(tocSections);

        if (sections.isEmpty()) {
            Map<String, Integer> bestIndexByKey = new HashMap<>();
            Map<String, Integer> bestScoreByKey = new HashMap<>();

            for (int i = 0; i < allHeadings.size(); i++) {
                HeadingCandidate start = allHeadings.get(i);
                if (start.sectionKey == null) continue;

                int startLine = start.lineIndex + 1;
                int boundaryLine = findBoundaryLine(allHeadings, i, lines);

                if (startLine >= boundaryLine) continue;

                int score = 0;
                for (int li = startLine; li < boundaryLine; li++) {
                    score += lines[li].length();
                }

                if (score <= 0) continue;

                Integer prevBest = bestScoreByKey.get(start.sectionKey);
                if (prevBest == null || score > prevBest) {
                    bestScoreByKey.put(start.sectionKey, score);
                    bestIndexByKey.put(start.sectionKey, i);
                }
            }

            for (Map.Entry<String, Integer> entry : bestIndexByKey.entrySet()) {
                String key = entry.getKey();
                int idx = entry.getValue();
                HeadingCandidate start = allHeadings.get(idx);

                int startLine = start.lineIndex + 1;
                int boundaryLine = findBoundaryLine(allHeadings, idx, lines);

                if (startLine >= boundaryLine) continue;

                StringBuilder body = new StringBuilder();
                for (int li = startLine; li < boundaryLine; li++) {
                    body.append(lines[li]).append("\n");
                }

                String bodyText = body.toString().trim();
                if (!bodyText.isEmpty()) {
                    sections.put(key, bodyText);
                }
            }
        }

        if (sectionKey != null && !sectionKey.isEmpty()) {
            String s = sections.get(sectionKey.toLowerCase());
            return (s != null) ? s : "";
        }

        StringBuilder merged = new StringBuilder();
        for (Map.Entry<String, String> entry : sections.entrySet()) {
            String key = entry.getKey();
            String body = entry.getValue();

            String title = key.toUpperCase().replace('_', ' ');

            if (merged.length() > 0) {
                merged.append("\n\n----------------------------------------\n\n");
            }

            merged.append("=== ").append(title).append(" ===").append("\n\n");
            merged.append(body);
        }

        return merged.toString().trim();
    }

    public String extractSections(String html) {
        return extractSections(html, null);
    }

    // =========================
    // HEADING NORMALIZATION
    // =========================

    private String normalizeHeading(String heading) {
        if (heading == null) return "";
        return heading.toUpperCase(Locale.ROOT)
                .replace('\u00A0', ' ')
                .replace('’', '\'')
                .replace('‘', '\'')
                .replace('“', '"')
                .replace('”', '"')
                .replaceAll("[^A-Z0-9 ]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean matchesCanonicalHeading(String norm, Set<String> canonical) {
        for (String h : canonical) {
            String normH = normalizeHeading(h);
            if (norm.equals(normH)) return true;
            if (norm.matches(normH + " \\d+")) return true; // headings with numbers
        }
        return false;
    }

    private String classifySectionByAllowedHeadings(String rawHeading) {
        String norm = normalizeHeading(rawHeading);

        if (matchesCanonicalHeading(norm, BENEFICIAL_HEADINGS)) return "beneficial_ownership";
        if (matchesCanonicalHeading(norm, EXEC_COMP_HEADINGS)) return "executive_compensation";
        if (matchesCanonicalHeading(norm, DIRECTOR_COMP_HEADINGS)) return "director_compensation";
        if (matchesCanonicalHeading(norm, EXEC_DIRECTOR_COMP_HEADINGS))
            return "executive_and_director_compensation";
        if (matchesCanonicalHeading(norm, EXEC_OFFICER_HEADINGS)) return "executive_officers";
        if (matchesCanonicalHeading(norm, AUDIT_HEADINGS)) return "audit_fees";

        return null;
    }

    // =========================
    // TOC DETECTION
    // =========================

    private boolean[] detectTableOfContentsBlocks(String[] lines) {
        boolean[] inToc = new boolean[lines.length];

        for (int i = 0; i < lines.length; i++) {
            String t = lines[i].trim();
            if (!t.equalsIgnoreCase("TABLE OF CONTENTS")) continue;

            boolean looksLikeToc = false;
            for (int k = i + 1; k < Math.min(lines.length, i + 6); k++) {
                String lk = lines[k].toLowerCase(Locale.ROOT);
                if (lk.contains("page")) {
                    looksLikeToc = true;
                    break;
                }
            }
            if (!looksLikeToc) continue;

            for (int j = i; j < lines.length; j++) {
                String s = lines[j].trim();
                if (s.isEmpty() || s.equals("* * *")) break;
                inToc[j] = true;
            }
        }

        return inToc;
    }

    private boolean looksLikeHeading(String line) {
        String t = line.trim();
        if (t.isEmpty()) return false;

        int len = t.length();
        if (len < 5 || len > 200) return false;

        String[] words = t.split("\\s+");
        if (words.length < 2 || words.length > 20) return false;

        if (t.contains(".") && !t.endsWith(".")) return false;

        int letters = 0, upper = 0;
        for (char c : t.toCharArray()) {
            if (Character.isLetter(c)) {
                letters++;
                if (Character.isUpperCase(c)) upper++;
            }
        }
        double upperRatio = (letters == 0) ? 0 : (double) upper / letters;

        int titleWords = 0;
        for (String w : words) {
            if (w.isEmpty()) continue;
            char ch = w.charAt(0);
            if (Character.isLetter(ch) && Character.isUpperCase(ch)) {
                titleWords++;
            }
        }
        double titleRatio = (double) titleWords / words.length;

        boolean looksLikeTableHeader =
                t.toLowerCase(Locale.ROOT).contains("shares") ||
                        t.toLowerCase(Locale.ROOT).contains("percentage") ||
                        t.toLowerCase(Locale.ROOT).contains("beneficially") ||
                        t.toLowerCase(Locale.ROOT).contains("owned") ||
                        t.toLowerCase(Locale.ROOT).contains("outstanding") ||
                        t.toLowerCase(Locale.ROOT).contains("ordinary") ||
                        t.toLowerCase(Locale.ROOT).contains("class a") ||
                        t.toLowerCase(Locale.ROOT).contains("class b");

        if (upperRatio > 0.7) return true;
        boolean hasDigits = t.matches(".*\\d.*");
        if (!looksLikeTableHeader && !hasDigits && titleRatio > 0.7) return true;

        return false;
    }

    // =========================
    // HEADING BOUNDARY
    // =========================

    private int findBoundaryLine(List<HeadingCandidate> allHeadings, int startIdx, String[] lines) {
        HeadingCandidate start = allHeadings.get(startIdx);
        int boundaryLine = lines.length;

        for (int j = startIdx + 1; j < allHeadings.size(); j++) {
            HeadingCandidate next = allHeadings.get(j);
            if (next.lineIndex <= start.lineIndex) continue;

            if (!isMajorBoundaryHeading(start, next)) continue;

            boundaryLine = next.lineIndex;
            break;
        }
        return boundaryLine;
    }

    private boolean isMajorBoundaryHeading(HeadingCandidate start, HeadingCandidate next) {
        if (next.sectionKey != null) {
            if (!next.sectionKey.equals(start.sectionKey)) return true;
            return false;
        }

        if (next.rawText == null) return false;

        String t = next.rawText;
        String lettersOnly = t.replaceAll("[^A-Za-z]", "");
        if (lettersOnly.length() < 8) return false;

        int upper = 0;
        for (char c : lettersOnly.toCharArray()) {
            if (Character.isUpperCase(c)) upper++;
        }
        double ratio = (lettersOnly.isEmpty()) ? 0.0 : (double) upper / lettersOnly.length();

        if (ratio > 0.8) return true;

        String norm = normalizeHeading(t);
        if (norm.contains("PAY VS PERFORMANCE") || norm.contains("PAY VERSUS PERFORMANCE")) return true;

        return false;
    }

    private Map<String, String> extractSectionsUsingToc(String[] lines,
                                                        boolean[] inToc,
                                                        List<HeadingCandidate> allHeadings) {
        Map<String, String> result = new LinkedHashMap<>();

        class TocEntry {
            int lineIndex;
            String rawText;
            String sectionKey;
        }

        List<TocEntry> tocEntries = new ArrayList<>();

        for (int i = 0; i < lines.length; i++) {
            if (!inToc[i]) continue;

            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            String sectionKey = classifySectionByAllowedHeadings(line);
            if (sectionKey == null) continue;

            TocEntry e = new TocEntry();
            e.lineIndex = i;
            e.rawText = line;
            e.sectionKey = sectionKey;
            tocEntries.add(e);
        }

        if (tocEntries.isEmpty()) return result;

        Map<String, HeadingCandidate> bodyHeadingByKeyOrdered = new LinkedHashMap<>();
        for (TocEntry te : tocEntries) {
            if (bodyHeadingByKeyOrdered.containsKey(te.sectionKey)) continue;

            HeadingCandidate found = null;
            for (HeadingCandidate h : allHeadings) {
                if (!te.sectionKey.equals(h.sectionKey)) continue;
                if (h.lineIndex <= te.lineIndex) continue;
                found = h;
                break;
            }

            if (found != null) bodyHeadingByKeyOrdered.put(te.sectionKey, found);
        }

        List<Map.Entry<String, HeadingCandidate>> ordered = new ArrayList<>(bodyHeadingByKeyOrdered.entrySet());
        for (int i = 0; i < ordered.size(); i++) {
            String key = ordered.get(i).getKey();
            HeadingCandidate start = ordered.get(i).getValue();

            int startLine = start.lineIndex + 1;
            int boundaryLine = lines.length;
            if (i + 1 < ordered.size()) {
                HeadingCandidate nextStart = ordered.get(i + 1).getValue();
                boundaryLine = nextStart.lineIndex;
            }

            if (startLine >= boundaryLine) continue;

            StringBuilder body = new StringBuilder();
            for (int li = startLine; li < boundaryLine; li++) {
                if (inToc[li]) continue;
                body.append(lines[li]).append("\n");
            }

            String bodyText = body.toString().trim();
            if (!bodyText.isEmpty()) result.put(key, bodyText);
        }

        return result;
    }

    private static class HeadingCandidate {
        int lineIndex;
        String rawText;
        String sectionKey;
    }
}

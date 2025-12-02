package com.x2iq.secdataextractionbackend.services.forms;


import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
//import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URL;
import java.util.*;

@Service
public class SecDef14AExtractorService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final Set<String> BENEFICIAL_HEADINGS = new HashSet<>(Arrays.asList(
            "SECURITY OWNERSHIP OF CERTAIN BENEFICIAL OWNERS AND MANAGEMENT",
            "SECURITY OWNERSHIP OF NEWS CORPORATION",
            "VOTING SECURITIES AND PRINCIPAL SHAREHOLDERS",
            "SECURITIES OWNERSHIP OF CERTAIN BENEFICIAL OWNERS AND MANAGEMENT",
            "BENEFICIAL OWNERSHIP OF SECURITIES",
            "SECURITY OWNERSHIP OF CERTAIN BENEFICIAL OWNERS AND MANAGEMENT AND RELATED STOCKHOLDER MATTERS",
            "STOCK OWNERSHIP",
            "BENEFICIAL OWNERSHIP OF COMPANY COMMON STOCK BY DIRECTORS, OFFICERS AND PRINCIPAL STOCKHOLDERS"
    ));

    // EXECUTIVE compensation only
    private static final Set<String> EXEC_COMP_HEADINGS = new HashSet<>(Arrays.asList(
            "EXECUTIVE COMPENSATION",
            "EXECUTIVE COMPENSATION TABLES",
            "COMPENSATION OF EXECUTIVE OFFICERS",
            "EXECUTIVE COMPENSATION AND RELATED INFORMATION",
            "EXECUTIVE COMPENSATION — COMPENSATION DISCUSSION AND ANALYSIS",
            "EXECUTIVE COMPENSATION AND OTHER INFORMATION"
    ));

    // DIRECTOR / TRUSTEE compensation only
    private static final Set<String> DIRECTOR_COMP_HEADINGS = new HashSet<>(Arrays.asList(
            "COMPENSATION OF DIRECTORS",
            "DIRECTOR COMPENSATION",
            "TRUSTEE COMPENSATION",
            "ELEMENTS OF DIRECTOR COMPENSATION",
            "DIRECTOR COMPENSATION PROGRAM",
            "Director Compensation" // will normalize to DIRECTOR COMPENSATION
    ));

    // Combined executive + director compensation
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

    // Optional: treat PAY VS PERFORMANCE as its own canonical section
    private static final Set<String> PAY_VS_PERFORMANCE_HEADINGS = new HashSet<>(Arrays.asList(
//            "PAY VERSUS PERFORMANCE",
//            "PAY VS. PERFORMANCE",
//            "PAY VERSUS PERFORMANCE TABLE"
    ));



////    @PostConstruct
//    public void start() {
//        // Demo only – disable in production
//        String url = "https://www.sec.gov/Archives/edgar/data/22444/000119312525294507/cmc-20251125.htm";
////        String html = proxyRotationService.fetchWithProxyRotation(url);
//        Map<String, String> sections = extractSections(url);
//        logger.info("Extracted {} sections", sections.size());
//        sections.forEach((k, v) -> logger.info(
//                "Section Key: {}\nBody (first 300 chars): {}\n",
//                k,
//                v.substring(0, Math.min(300, v.length()))
//        ));
//    }

    // =========================
    // PUBLIC API
    // =========================
public String sendGetRequestWebClient(String url) throws IOException {
    String response;
    try (WebClient webClient = new WebClient()) {
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setPrintContentOnFailingStatusCode(false);
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.waitForBackgroundJavaScript(10000);
        webClient.getOptions().setTimeout(60000);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        WebRequest requestSettings = new WebRequest(new URL(url), HttpMethod.GET);
        response = webClient.getPage(requestSettings).getWebResponse().getContentAsString();
    }
    return response;
}
    public String extractSections(String html) {
        try {
            html = sendGetRequestWebClient(html); // here 'html' is actually the URL
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return ""; // bail out if fetch fails
        }

        Map<String, String> sections = new LinkedHashMap<>();
        if (html == null || html.isEmpty()) return "";

        Document doc = Jsoup.parse(html);
        if (doc.body() == null) return "";

        // Preserve line-ish boundaries
        doc.select("br, p, div, tr, li").append("\\n");

        String raw = doc.body().text();
        if (raw == null) return "";

        String normalized = raw.replace("\\n", "\n")
                .replace('\u00A0', ' ')
                .replace('’', '\'')
                .replace('“', '"')
                .replace('”', '"')
                .replaceAll("[ \t]+", " ")
                .trim();

        if (normalized.isEmpty()) return "";

        String[] lines = normalized.split("\\n+");

        // Detect TOC blocks
        boolean[] inToc = detectTableOfContentsBlocks(lines);

        // 1) Collect ALL heading-like lines (for boundaries)
        List<HeadingCandidate> allHeadings = new ArrayList<>();

        for (int i = 0; i < lines.length; i++) {
            if (inToc[i]) continue; // skip TOC for body headings

            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            String sectionKey = classifySectionByAllowedHeadings(line);

            boolean isHeading = (sectionKey != null) || looksLikeHeading(line);
            if (!isHeading) continue;

            HeadingCandidate h = new HeadingCandidate();
            h.lineIndex = i;
            h.rawText = line;
            h.sectionKey = sectionKey;
            allHeadings.add(h);

            logger.debug("Heading-like line at {}: '{}' -> sectionKey={}",
                    i, line, sectionKey);
        }

        if (allHeadings.isEmpty()) {
            logger.info("No heading-like lines detected.");
            return "";
        }

        // 2) Try TOC-driven extraction first (only if TOC exists and matches)
        Map<String, String> tocSections = extractSectionsUsingToc(lines, inToc, allHeadings);
        if (!tocSections.isEmpty()) {
            logger.info("Using TOC-driven extraction, sections: {}", tocSections.keySet());
            sections.putAll(tocSections);
        } else {
            // 3) Fallback: best-body heuristic using allHeadings,
            //    but only stop at MAJOR headings (and different canonical keys).
            Map<String, Integer> bestIndexByKey = new HashMap<>();
            Map<String, Integer> bestScoreByKey = new HashMap<>();

            for (int i = 0; i < allHeadings.size(); i++) {
                HeadingCandidate start = allHeadings.get(i);
                if (start.sectionKey == null) continue; // only canonical headings are section starts

                int startLine = start.lineIndex + 1;
                int boundaryLine = findBoundaryLine(allHeadings, i, lines);

                if (startLine >= boundaryLine) {
                    logger.debug("Candidate for key {} at heading index {} has empty body",
                            start.sectionKey, i);
                    continue;
                }

                int score = 0;
                for (int li = startLine; li < boundaryLine; li++) {
                    score += lines[li].length();
                }

                logger.debug("Candidate for key {} at heading index {} has body score {}",
                        start.sectionKey, i, score);

                if (score <= 0) continue;

                Integer prevBest = bestScoreByKey.get(start.sectionKey);
                if (prevBest == null || score > prevBest) {
                    bestScoreByKey.put(start.sectionKey, score);
                    bestIndexByKey.put(start.sectionKey, i);
                }
            }

            // 4) Extract sections only for the best candidate per key
            for (Map.Entry<String, Integer> entry : bestIndexByKey.entrySet()) {
                String key = entry.getKey();
                int idx = entry.getValue();
                HeadingCandidate start = allHeadings.get(idx);

                int startLine = start.lineIndex + 1;
                int boundaryLine = findBoundaryLine(allHeadings, idx, lines);

                if (startLine >= boundaryLine) {
                    logger.debug("Best candidate for key {} ended up empty (unexpected)", key);
                    continue;
                }

                StringBuilder body = new StringBuilder();
                for (int li = startLine; li < boundaryLine; li++) {
                    body.append(lines[li]).append("\n");
                }

                String bodyText = body.toString().trim();
                if (bodyText.isEmpty()) continue;

                sections.put(key, bodyText);

                logger.info("Extracted BEST section '{}' from lines {}..{}. Length={}",
                        key, startLine, boundaryLine, bodyText.length());
            }
        }

        logger.info("DEF14A extract (allowed headings + best-body heuristic): {} logical sections",
                sections.size());

        if (sections.isEmpty()) {
            return "";
        }

        // 5) Merge all sections into a single string WITH section name and gap
        StringBuilder merged = new StringBuilder();
        for (Map.Entry<String, String> entry : sections.entrySet()) {
            String key = entry.getKey();       // e.g. "executive_compensation"
            String body = entry.getValue();

            // Human-readable section title
            String title = key.toUpperCase().replace('_', ' '); // EXECUTIVE COMPENSATION

            if (merged.length() > 0) {
                // gap between sections
                merged.append("\n\n----------------------------------------\n\n");
            }

            merged.append("=== ").append(title).append(" ===").append("\n\n");
            merged.append(body);
        }

        return merged.toString().trim();
    }



    // Backwards-compatible signature
    public String extractSections(String html,
                                  List<String> headingsA,
                                  List<String> stopHeadingsB) {
        // For now we ignore headingsA / stopHeadingsB, same as before,
        // and just reuse the main merged-extract method.
        return extractSections(html);
    }


    // =========================
    // TOC-BASED EXTRACTION
    // =========================

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
            if (sectionKey == null) continue; // not one of our targets

            TocEntry e = new TocEntry();
            e.lineIndex = i;
            e.rawText = line;
            e.sectionKey = sectionKey;
            tocEntries.add(e);
        }

        if (tocEntries.isEmpty()) {
            return result; // nothing useful in TOC
        }

        // Map TOC entries to body headings
        Map<String, HeadingCandidate> bodyHeadingByKeyOrdered = new LinkedHashMap<>();

        for (TocEntry te : tocEntries) {
            if (bodyHeadingByKeyOrdered.containsKey(te.sectionKey)) continue;

            HeadingCandidate found = null;
            for (HeadingCandidate h : allHeadings) {
                if (!te.sectionKey.equals(h.sectionKey)) continue;
                if (h.lineIndex <= te.lineIndex) continue; // must be below TOC
                found = h;
                break;
            }

            if (found != null) {
                bodyHeadingByKeyOrdered.put(te.sectionKey, found);
            }
        }

        if (bodyHeadingByKeyOrdered.isEmpty()) {
            return result;
        }

        List<Map.Entry<String, HeadingCandidate>> ordered =
                new ArrayList<>(bodyHeadingByKeyOrdered.entrySet());

        for (int i = 0; i < ordered.size(); i++) {
            String key = ordered.get(i).getKey();
            HeadingCandidate start = ordered.get(i).getValue();

            int startLine = start.lineIndex + 1;
            int boundaryLine = lines.length;

            if (i + 1 < ordered.size()) {
                HeadingCandidate nextStart = ordered.get(i + 1).getValue();
                boundaryLine = nextStart.lineIndex;
            }

            if (startLine >= boundaryLine) {
                logger.debug("TOC-based section {} would be empty (lines {}..{})",
                        key, startLine, boundaryLine);
                continue;
            }

            StringBuilder body = new StringBuilder();
            for (int li = startLine; li < boundaryLine; li++) {
                if (inToc[li]) continue;
                body.append(lines[li]).append("\n");
            }

            String bodyText = body.toString().trim();
            if (bodyText.isEmpty()) continue;

            result.put(key, bodyText);

            logger.info("TOC-based extraction '{}' from lines {}..{}. length={}",
                    key, startLine, boundaryLine, bodyText.length());
        }

        return result;
    }

    // =========================
    // BOUNDARY HELPERS
    // =========================

    /**
     * Find the boundary line (exclusive) for a canonical heading at index startIdx.
     */
    private int findBoundaryLine(List<HeadingCandidate> allHeadings,
                                 int startIdx,
                                 String[] lines) {
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

    /**
     * Decide if "next" should act as a **section boundary** for the section
     * that started at "start".
     *
     * Rules:
     *  - If next.sectionKey != null AND different from start.sectionKey → boundary.
     *  - If same sectionKey → NOT a boundary (second Director Compensation stays inside).
     *  - Otherwise, strong ALL-CAPS-ish headings or PAY VS/PAY VERSUS PERFORMANCE
     *    behave as major boundaries.
     */
    private boolean isMajorBoundaryHeading(HeadingCandidate start, HeadingCandidate next) {
        if (next.sectionKey != null) {
            // Different canonical section ends the current one
            if (!next.sectionKey.equals(start.sectionKey)) {
                return true;
            }
            // Same canonical key: treat as internal subheading
            return false;
        }

        if (next.rawText == null) return false;

        String t = next.rawText;
        String lettersOnly = t.replaceAll("[^A-Za-z]", "");
        if (lettersOnly.length() < 8) {
            // Very short stuff like "All Other" won't qualify as major
            return false;
        }

        int upper = 0;
        for (char c : lettersOnly.toCharArray()) {
            if (Character.isUpperCase(c)) upper++;
        }
        double ratio = (lettersOnly.isEmpty()) ? 0.0 : (double) upper / lettersOnly.length();

        // Strong ALL-CAPS heading → major
        if (ratio > 0.8) {
            return true;
        }

        // Explicitly treat PAY VS / PAY VERSUS PERFORMANCE as a major boundary
        String norm = normalizeHeading(t);
        if (norm.contains("PAY VS. PERFORMANCE") || norm.contains("PAY VERSUS PERFORMANCE")) {
            return true;
        }

        return false;
    }

    // =========================
    // TOC DETECTION
    // =========================

    private boolean[] detectTableOfContentsBlocks(String[] lines) {
        boolean[] inToc = new boolean[lines.length];

        for (int i = 0; i < lines.length; i++) {
            String t = lines[i].trim();
            if (!t.equalsIgnoreCase("TABLE OF CONTENTS")) continue;

            // Check if this looks like a real TOC (has "Page" nearby)
            boolean looksLikeToc = false;
            for (int k = i + 1; k < Math.min(lines.length, i + 6); k++) {
                String lk = lines[k].toLowerCase(Locale.ROOT);
                if (lk.contains("page")) {
                    looksLikeToc = true;
                    break;
                }
            }
            if (!looksLikeToc) continue;

            // Mark from "TABLE OF CONTENTS" down until we hit a blank or a separator
            for (int j = i; j < lines.length; j++) {
                String s = lines[j].trim();
                if (s.isEmpty() || s.equals("* * *")) {
                    break;
                }
                inToc[j] = true;
            }
        }

        return inToc;
    }

    // =========================
    // HEURISTICS
    // =========================

    private boolean looksLikeHeading(String line) {
        String t = line.trim();
        if (t.isEmpty()) return false;

        int len = t.length();
        if (len < 5 || len > 200) return false;

        String[] words = t.split("\\s+");
        if (words.length < 2 || words.length > 20) return false;

        // sentence-ish: dot in the middle
        if (t.contains(".") && !t.endsWith(".")) {
            return false;
        }

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
            if (w.length() == 0) continue;
            char ch = w.charAt(0);
            if (Character.isLetter(ch) && Character.isUpperCase(ch)) {
                titleWords++;
            }
        }
        double titleRatio = (double) titleWords / words.length;

        String lower = t.toLowerCase(Locale.ROOT);

        boolean looksLikeTableHeader =
                lower.contains("shares") ||
                        lower.contains("percentage") ||
                        lower.contains("beneficially") ||
                        lower.contains("owned") ||
                        lower.contains("outstanding") ||
                        lower.contains("ordinary") ||
                        lower.contains("class a") ||
                        lower.contains("class b");

        if (upperRatio > 0.7) return true;

        boolean hasDigits = t.matches(".*\\d.*");
        if (!looksLikeTableHeader && !hasDigits && titleRatio > 0.7) {
            return true;
        }

        return false;
    }

    private String normalizeHeading(String heading) {
        if (heading == null) return "";
        return heading.toUpperCase(Locale.ROOT)
                .replace('\u00A0', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean matchesCanonicalHeading(String norm, Set<String> canonical) {
        for (String h : canonical) {
            if (norm.equals(h)) return true;
            if (norm.matches(h + " \\d+")) return true;
            String tocPrefix = "TABLE OF CONTENTS " + h;
            if (norm.equals(tocPrefix)) return true;
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
        if (matchesCanonicalHeading(norm, PAY_VS_PERFORMANCE_HEADINGS)) return "pay_vs_performance";

        return null;
    }

    // =========================
    // SUPPORTING TYPES
    // =========================

    private static class HeadingCandidate {
        int lineIndex;
        String rawText;
        String sectionKey; // null if not one of the canonical headings
    }

    // =========================
    // TextAnalyzer impl
    // =========================

}

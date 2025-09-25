package com.x2iq.secdataextractionbackend.services.forms;

import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class Form10KExtractorService {
    private String extractSectionWithFallback(String data, List<String> regexPatterns) {
        for (String patternStr : regexPatterns) {
            Pattern pattern = Pattern.compile(patternStr, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(data);

            List<String> matches = new ArrayList<>();

            while (matcher.find()) {
                matches.add(matcher.group());
            }

            int count = matches.size();

            if (count == 0) {
                return null; // no match found
            }
            else if (count == 1) {
                return matches.get(0); // only one → return it
            }
            else {
                // two or more → return the longest one
                String longest = matches.get(0);
                for (String m : matches) {
                    if (m != null && m.length() > longest.length()) {
                        longest = m;
                    }
                }
                return longest;
            }
        }
        return null;
    }


    public String DataOf10KExtracted(String html, String itemName) {
        Map<String, String> itemsMap = new LinkedHashMap<>();
        String mainDocData = null;
            mainDocData = String.valueOf(Jsoup.parse(html).text());
            mainDocData = normalizeInvisibleCharacters(mainDocData);

        if (mainDocData == null || !mainDocData.toLowerCase().contains("10b")) {
            // return null;
        }
        String lowerCaseData = (mainDocData.toLowerCase());

        String item1 = null, item1A = null, item1B = null, item1C = null,
                item2 = null, item3 = null, item4 = null, item5 = null,
                item6 = null, item7 = null, item7A = null, item8 = null,
                item9 = null, item9A = null, item9B = null, item9C = null,
                item10 = null, item11 = null, item12 = null, item13 = null,
                item14 = null, item15 = null, item16 = null;

        if (lowerCaseData.contains("item 1.")) {
            String extracted1 = extractSectionWithFallback(
                    mainDocData,
                    Arrays.asList("(?i)Item 1\\..*?(?i:(?=Item (?!1\\.)\\d{1,2}\\.?[A-Z]?\\.)|(?=Item 1\\. Business)|(?=Item 1\\. Description of Business)|(?=Item 1A\\. Risk Factors)|(?=Item lA\\. Risk Factors)|(?=Item LA\\. Risk Factors)|(?=Item 1B\\. Unresolved Staff Comments)|(?=Item lB\\. Unresolved Staff Comments)|(?=Item LB\\. Unresolved Staff Comments)|(?=Item [1lI]C\\.?\\s*(–|—|-)?\\s*cybersecurity)|(?=Item [1lI]C\\.?\\s*(–|—|-)?\\s*CYBERSECURITY)|(?=Item [1lI]C\\.?\\s*(–|—|-)?\\s*Cybersecurity)|(?=Item 1C\\. Cybersecurity)(?=Item 1C\\ Cybersecurity)|(?=Item lC\\. Cybersecurity)|(?=Item LC\\. Cybersecurity)|(?=Item 2\\. Properties)|(?=Item 2\\. Description of Property)|(?=Item 3\\. Legal Proceedings)|(?=Item 4\\. Mine Safety Disclosures)|(?=Item 5\\. Market for Registrant's Common Equity, Related Stockholder Matters and Issuer Purchases of Equity Securities)|(?=Item 6\\. \\[Reserved\\])|(?=Item 6\\. Reserved)|(?=Item 6\\. Selected Financial Data)|(?=Item 7\\. Management's Discussion and Analysis of Financial Condition and Results of Operations)|(?=Item 7A\\. Quantitative and Qualitative Disclosures About Market Risk)|(?=Item 8\\. Financial Statements and Supplementary Data)|(?=Item 9\\. Changes in and Disagreements With Accountants on Accounting and Financial Disclosure)|(?=Item 9A\\. Controls and Procedures)|(?=Item 9B\\. Other Information)|(?=Item 9C\\. Disclosure Regarding Foreign Jurisdictions that Prevent Inspections)|(?=Item 10\\. Directors, Executive Officers and Corporate Governance)|(?=Item 11\\. Executive Compensation)|(?=Item 12\\. Security Ownership of Certain Beneficial Owners and Management and Related Stockholder Matters)|(?=Item 13\\. Certain Relationships and Related Transactions, and Director Independence)|(?=Item 14\\. Principal Accounting Fees and Services)|(?=Item 15\\. Exhibits, Financial Statement Schedules)|(?=Item 16\\. Form 10-K Summary))")
            );
            if (extracted1 != null) {
                item1 = String.valueOf(Jsoup.parse(extracted1).text());
                itemsMap.put("1", item1);
            }
        }

        if (lowerCaseData.contains("item 1a.")) {
            String extracted1A = extractSectionWithFallback(
                    mainDocData,
                    Arrays.asList("(?i)Item [1l]A\\.?\\s*(–|—|-|:)?\\s*.*?(?i:(?=Item (?!1\\.|[1lI]A\\.)\\d{1,2}\\.?[A-Z]?\\.)|(?=Item 1B\\. Unresolved Staff Comments)|(?=Item lB\\. Unresolved Staff Comments)|(?=Item LB\\. Unresolved Staff Comments)|(?=Item [1lI]C\\.?\\s*(–|—|-|:)?\\s*cybersecurity)|(?=Item [1lI]C\\.?\\s*(–|—|-|:)?\\s*CYBERSECURITY)|(?=Item [1lI]C\\.?\\s*(–|—|-|:)?\\s*Cybersecurity)|(?=Item 1C\\. Cybersecurity)(?=Item 1C\\ Cybersecurity)|(?=Item lC\\. Cybersecurity)|(?=Item LC\\. Cybersecurity)|(?=Item 2\\. Properties)|(?=Item 2\\. Description of Property)|(?=Item 3\\. Legal Proceedings)|(?=Item 4\\. Mine Safety Disclosures)|(?=Item 5\\. Market for Registrant's Common Equity, Related Stockholder Matters and Issuer Purchases of Equity Securities)|(?=Item 6\\. \\[Reserved\\])|(?=Item 6\\. Reserved)|(?=Item 6\\. Selected Financial Data)|(?=Item 7\\. Management's Discussion and Analysis of Financial Condition and Results of Operations)|(?=Item 7A\\. Quantitative and Qualitative Disclosures About Market Risk)|(?=Item 8\\. Financial Statements and Supplementary Data)|(?=Item 9\\. Changes in and Disagreements With Accountants on Accounting and Financial Disclosure)|(?=Item 9A\\. Controls and Procedures)|(?=Item 9B\\. Other Information)|(?=Item 9C\\. Disclosure Regarding Foreign Jurisdictions that Prevent Inspections)|(?=Item 10\\. Directors, Executive Officers and Corporate Governance)|(?=Item 11\\. Executive Compensation)|(?=Item 12\\. Security Ownership of Certain Beneficial Owners and Management and Related Stockholder Matters)|(?=Item 13\\. Certain Relationships and Related Transactions, and Director Independence)|(?=Item 14\\. Principal Accounting Fees and Services)|(?=Item 15\\. Exhibits, Financial Statement Schedules)|(?=Item 16\\. Form 10-K Summary))")
            );
            if (extracted1A != null) {
                item1A = String.valueOf(Jsoup.parse(extracted1A).text());
                itemsMap.put("1A", item1A);
            }
        }

        if (lowerCaseData.contains("item 1b.")) {
            String extracted1B = extractSectionWithFallback(
                    mainDocData,
                    Arrays.asList("(?i)Item [1lI]B\\.?\\s*(–|—|-|:)?\\s*.*?(?i:(?=Item (?!1\\.|[1lI]A\\.|[1lI]B\\.)\\d{1,2}\\.?[A-Z]?\\.)|(?=Item [1lI]C\\.?\\s*(–|—|-|:)?\\s*cybersecurity)|(?=Item [1lI]C\\.?\\s*(–|—|-|:)?\\s*CYBERSECURITY)|(?=Item [1lI]C\\.?\\s*(–|—|-|:)?\\s*Cybersecurity)|(?=Item 1C\\. Cybersecurity)|(?=Item 1C\\ Cybersecurity)|(?=Item lC\\. Cybersecurity)|(?=Item LC\\. Cybersecurity)|(?=Item 2\\. Properties)|(?=Item 2\\. Description of Property)|(?=Item 3\\. Legal Proceedings)|(?=Item 4\\. Mine Safety Disclosures)|(?=Item 5\\. Market for Registrant's Common Equity, Related Stockholder Matters and Issuer Purchases of Equity Securities)|(?=Item 6\\. \\[Reserved\\])|(?=Item 6\\. Reserved)|(?=Item 6\\. Selected Financial Data)|(?=Item 7\\. Management's Discussion and Analysis of Financial Condition and Results of Operations)|(?=Item 7A\\. Quantitative and Qualitative Disclosures About Market Risk)|(?=Item 8\\. Financial Statements and Supplementary Data)|(?=Item 9\\. Changes in and Disagreements With Accountants on Accounting and Financial Disclosure)|(?=Item 9A\\. Controls and Procedures)|(?=Item 9B\\. Other Information)|(?=Item 9C\\. Disclosure Regarding Foreign Jurisdictions that Prevent Inspections)|(?=Item 10\\. Directors, Executive Officers and Corporate Governance)|(?=Item 11\\. Executive Compensation)|(?=Item 12\\. Security Ownership of Certain Beneficial Owners and Management and Related Stockholder Matters)|(?=Item 13\\. Certain Relationships and Related Transactions, and Director Independence)|(?=Item 14\\. Principal Accounting Fees and Services)|(?=Item 15\\. Exhibits, Financial Statement Schedules)|(?=Item 16\\. Form 10-K Summary))")
            );
            if (extracted1B != null) {
                item1B = String.valueOf(Jsoup.parse(extracted1B).text());
                itemsMap.put("1B", item1B);
            }
        }

        if (lowerCaseData.contains("item 1c.")) {//?\s*(–|—|-|:)?\s*
            String extracted1C = extractSectionWithFallback(
                    mainDocData,
                    Arrays.asList(
                            "(?i)Item [1lI]C\\.?\\s*(–|—|-|:)?\\s*.*?(?i:(?=Item (?!1\\.|[1lI]A\\.|[1lI]B\\.|[1lI]C\\.)\\d{1,2}\\.?[A-Z]?\\.)|(?=Item 2\\. Properties)|(?=Item 2\\. Description of Property)|(?=Item 3\\. Legal Proceedings)|(?=Item 4\\. Mine Safety Disclosures)|(?=Item 5\\. Market for Registrant's Common Equity, Related Stockholder Matters and Issuer Purchases of Equity Securities)|(?=Item 6\\. \\[Reserved\\])|(?=Item 6\\. Reserved)|(?=Item 6\\. Selected Financial Data)|(?=Item 7\\. Management's Discussion and Analysis of Financial Condition and Results of Operations)|(?=Item 7A\\. Quantitative and Qualitative Disclosures About Market Risk)|(?=Item 8\\. Financial Statements and Supplementary Data)|(?=Item 9\\. Changes in and Disagreements With Accountants on Accounting and Financial Disclosure)|(?=Item 9A\\. Controls and Procedures)|(?=Item 9B\\. Other Information)|(?=Item 9C\\. Disclosure Regarding Foreign Jurisdictions that Prevent Inspections)|(?=Item 10\\. Directors, Executive Officers and Corporate Governance)|(?=Item 11\\. Executive Compensation)|(?=Item 12\\. Security Ownership of Certain Beneficial Owners and Management and Related Stockholder Matters)|(?=Item 13\\. Certain Relationships and Related Transactions, and Director Independence)|(?=Item 14\\. Principal Accounting Fees and Services)|(?=Item 15\\. Exhibits, Financial Statement Schedules)|(?=Item 16\\. Form 10-K Summary))")
            );
            if (extracted1C != null) {
                item1C = String.valueOf(Jsoup.parse(extracted1C).text());
                itemsMap.put("1C", item1C);
            }
        }

        if (lowerCaseData.contains("item 2.")) {
            String extracted2 = extractSectionWithFallback(
                    mainDocData,
                    Arrays.asList(
                            "(?i)Item 2\\..*?(?i:(?=Item (?!1\\.|1A\\.|1B\\.|1C\\.|2\\.)\\d{1,2}\\.?[A-Z]?\\.)|(?=Item 3\\. Legal Proceedings)|(?=Item 4\\. Mine Safety Disclosures)|(?=Item 5\\. Market for Registrant's Common Equity, Related Stockholder Matters and Issuer Purchases of Equity Securities)|(?=Item 6\\. \\[Reserved\\])|(?=Item 6\\. Reserved)|(?=Item 6\\. Selected Financial Data)|(?=Item 7\\. Management's Discussion and Analysis of Financial Condition and Results of Operations)|(?=Item 7A\\. Quantitative and Qualitative Disclosures About Market Risk)|(?=Item 8\\. Financial Statements and Supplementary Data)|(?=Item 9\\. Changes in and Disagreements With Accountants on Accounting and Financial Disclosure)|(?=Item 9A\\. Controls and Procedures)|(?=Item 9B\\. Other Information)|(?=Item 9C\\. Disclosure Regarding Foreign Jurisdictions that Prevent Inspections)|(?=Item 10\\. Directors, Executive Officers and Corporate Governance)|(?=Item 11\\. Executive Compensation)|(?=Item 12\\. Security Ownership of Certain Beneficial Owners and Management and Related Stockholder Matters)|(?=Item 13\\. Certain Relationships and Related Transactions, and Director Independence)|(?=Item 14\\. Principal Accounting Fees and Services)|(?=Item 15\\. Exhibits, Financial Statement Schedules)|(?=Item 16\\. Form 10-K Summary))"
                    )
            );
            if (extracted2 != null) {
                item2 = String.valueOf(Jsoup.parse(extracted2).text());
                itemsMap.put("2", item2);
            }
        }

        if (lowerCaseData.contains("item 3.")) {
            String extracted3 = extractSectionWithFallback(
                    mainDocData,
                    Arrays.asList(
                            "(?i)Item 3\\..*?(?i:(?=Item (?!1\\.|1A\\.|1B\\.|1C\\.|2\\.|3\\.)\\d{1,2}\\.?[A-Z]?\\.)|(?=Item 4\\. Mine Safety Disclosures)|(?=Item 5\\. Market for Registrant's Common Equity, Related Stockholder Matters and Issuer Purchases of Equity Securities)|(?=Item 6\\. \\[Reserved\\])|(?=Item 6\\. Reserved)|(?=Item 6\\. Selected Financial Data)|(?=Item 7\\. Management's Discussion and Analysis of Financial Condition and Results of Operations)|(?=Item 7A\\. Quantitative and Qualitative Disclosures About Market Risk)|(?=Item 8\\. Financial Statements and Supplementary Data)|(?=Item 9\\. Changes in and Disagreements With Accountants on Accounting and Financial Disclosure)|(?=Item 9A\\. Controls and Procedures)|(?=Item 9B\\. Other Information)|(?=Item 9C\\. Disclosure Regarding Foreign Jurisdictions that Prevent Inspections)|(?=Item 10\\. Directors, Executive Officers and Corporate Governance)|(?=Item 11\\. Executive Compensation)|(?=Item 12\\. Security Ownership of Certain Beneficial Owners and Management and Related Stockholder Matters)|(?=Item 13\\. Certain Relationships and Related Transactions, and Director Independence)|(?=Item 14\\. Principal Accounting Fees and Services)|(?=Item 15\\. Exhibits, Financial Statement Schedules)|(?=Item 16\\. Form 10-K Summary))"
                    )
            );
            if (extracted3 != null) {
                item3 = String.valueOf(Jsoup.parse(extracted3).text());
                itemsMap.put("3", item3);
            }
        }

        if (lowerCaseData.contains("item 4.")) {
            String extracted4 = extractSectionWithFallback(
                    mainDocData,
                    Arrays.asList("(?i)Item 4\\..*?(?i:(?=Item (?!1\\.|1A\\.|1B\\.|1C\\.|2\\.|3\\.|4\\.)\\d{1,2}\\.?[A-Z]?\\.)|(?=Item 5\\. Market for Registrant's Common Equity, Related Stockholder Matters and Issuer Purchases of Equity Securities)|(?=Item 6\\. \\[Reserved\\])|(?=Item 6\\. Reserved)|(?=Item 6\\. Selected Financial Data)|(?=Item 7\\. Management's Discussion and Analysis of Financial Condition and Results of Operations)|(?=Item 7A\\. Quantitative and Qualitative Disclosures About Market Risk)|(?=Item 8\\. Financial Statements and Supplementary Data)|(?=Item 9\\. Changes in and Disagreements With Accountants on Accounting and Financial Disclosure)|(?=Item 9A\\. Controls and Procedures)|(?=Item 9B\\. Other Information)|(?=Item 9C\\. Disclosure Regarding Foreign Jurisdictions that Prevent Inspections)|(?=Item 10\\. Directors, Executive Officers and Corporate Governance)|(?=Item 11\\. Executive Compensation)|(?=Item 12\\. Security Ownership of Certain Beneficial Owners and Management and Related Stockholder Matters)|(?=Item 13\\. Certain Relationships and Related Transactions, and Director Independence)|(?=Item 14\\. Principal Accounting Fees and Services)|(?=Item 15\\. Exhibits, Financial Statement Schedules)|(?=Item 16\\. Form 10-K Summary))")
            );
            if (extracted4 != null) {
                item4 = String.valueOf(Jsoup.parse(extracted4).text());
                itemsMap.put("4", item4);
            }
        }

        if (lowerCaseData.contains("item 5.")) {
            String extracted5 = extractSectionWithFallback(
                    mainDocData,
                    Arrays.asList(
                            "(?i)Item 5\\..*?(?i:(?=Item (?!1\\.|1A\\.|1B\\.|1C\\.|2\\.|3\\.|4\\.|5\\.)\\d{1,2}\\.?[A-Z]?\\.)|(?=Item 6\\. \\[Reserved\\])|(?=Item 6\\. Reserved)|(?=Item 6\\. Selected Financial Data)|(?=Item 7\\. Management's Discussion and Analysis of Financial Condition and Results of Operations)|(?=Item 7A\\. Quantitative and Qualitative Disclosures About Market Risk)|(?=Item 8\\. Financial Statements and Supplementary Data)|(?=Item 9\\. Changes in and Disagreements With Accountants on Accounting and Financial Disclosure)|(?=Item 9A\\. Controls and Procedures)|(?=Item 9B\\. Other Information)|(?=Item 9C\\. Disclosure Regarding Foreign Jurisdictions that Prevent Inspections)|(?=Item 10\\. Directors, Executive Officers and Corporate Governance)|(?=Item 11\\. Executive Compensation)|(?=Item 12\\. Security Ownership of Certain Beneficial Owners and Management and Related Stockholder Matters)|(?=Item 13\\. Certain Relationships and Related Transactions, and Director Independence)|(?=Item 14\\. Principal Accounting Fees and Services)|(?=Item 15\\. Exhibits, Financial Statement Schedules)|(?=Item 16\\. Form 10-K Summary))"
                    )
            );
            if (extracted5 != null) {
                item5 = String.valueOf(Jsoup.parse(extracted5).text());
                itemsMap.put("5", item5);
            }
        }

        if (lowerCaseData.contains("item 6.")) {
            String extracted6 = extractSectionWithFallback(
                    mainDocData,
                    Arrays.asList(
                            "(?i)Item 6\\..*?(?i:(?=Item (?!1\\.|1A\\.|1B\\.|1C\\.|2\\.|3\\.|4\\.|5\\.|6\\.)\\d{1,2}\\.?[A-Z]?\\.)|(?=Item 7\\. Management's Discussion and Analysis of Financial Condition and Results of Operations)|(?=Item 7A\\. Quantitative and Qualitative Disclosures About Market Risk)|(?=Item 8\\. Financial Statements and Supplementary Data)|(?=Item 9\\. Changes in and Disagreements With Accountants on Accounting and Financial Disclosure)|(?=Item 9A\\. Controls and Procedures)|(?=Item 9B\\. Other Information)|(?=Item 9C\\. Disclosure Regarding Foreign Jurisdictions that Prevent Inspections)|(?=Item 10\\. Directors, Executive Officers and Corporate Governance)|(?=Item 11\\. Executive Compensation)|(?=Item 12\\. Security Ownership of Certain Beneficial Owners and Management and Related Stockholder Matters)|(?=Item 13\\. Certain Relationships and Related Transactions, and Director Independence)|(?=Item 14\\. Principal Accounting Fees and Services)|(?=Item 15\\. Exhibits, Financial Statement Schedules)|(?=Item 16\\. Form 10-K Summary))"
                    )
            );
            if (extracted6 != null) {
                item6 = String.valueOf(Jsoup.parse(extracted6).text());
                itemsMap.put("6", item6);
            }
        }

        if (lowerCaseData.contains("item 7.")) {
            String extracted7 = extractSectionWithFallback(
                    mainDocData,
                    Arrays.asList("(?i)Item 7\\..*?(?i:(?=Item (?!1\\.|1A\\.|1B\\.|1C\\.|2\\.|3\\.|4\\.|5\\.|6\\.|7\\.)\\d{1,2}\\.?[A-Z]?\\.)|(?=Item 7A\\. Quantitative and Qualitative Disclosures About Market Risk)|(?=Item 8\\. Financial Statements and Supplementary Data)|(?=Item 9\\. Changes in and Disagreements With Accountants on Accounting and Financial Disclosure)|(?=Item 9A\\. Controls and Procedures)|(?=Item 9B\\. Other Information)|(?=Item 9C\\. Disclosure Regarding Foreign Jurisdictions that Prevent Inspections)|(?=Item 10\\. Directors, Executive Officers and Corporate Governance)|(?=Item 11\\. Executive Compensation)|(?=Item 12\\. Security Ownership of Certain Beneficial Owners and Management and Related Stockholder Matters)|(?=Item 13\\. Certain Relationships and Related Transactions, and Director Independence)|(?=Item 14\\. Principal Accounting Fees and Services)|(?=Item 15\\. Exhibits, Financial Statement Schedules)|(?=Item 16\\. Form 10-K Summary))"
                    )
            );
            if (extracted7 != null) {
                item7 = String.valueOf(Jsoup.parse(extracted7).text());
                itemsMap.put("7", item7);
            }
        }

        if (lowerCaseData.contains("item 7a.")) {
            String extracted7A = extractSectionWithFallback(
                    mainDocData,
                    Arrays.asList("(?i)Item 7A\\..*?(?i:(?=Item (?!1\\.|1A\\.|1B\\.|1C\\.|2\\.|3\\.|4\\.|5\\.|6\\.|7\\.|7A\\.)\\d{1,2}\\.?[A-Z]?\\.)|(?=Item 8\\. Financial Statements and Supplementary Data)|(?=Item 9\\. Changes in and Disagreements With Accountants on Accounting and Financial Disclosure)|(?=Item 9A\\. Controls and Procedures)|(?=Item 9B\\. Other Information)|(?=Item 9C\\. Disclosure Regarding Foreign Jurisdictions that Prevent Inspections)|(?=Item 10\\. Directors, Executive Officers and Corporate Governance)|(?=Item 11\\. Executive Compensation)|(?=Item 12\\. Security Ownership of Certain Beneficial Owners and Management and Related Stockholder Matters)|(?=Item 13\\. Certain Relationships and Related Transactions, and Director Independence)|(?=Item 14\\. Principal Accounting Fees and Services)|(?=Item 15\\. Exhibits, Financial Statement Schedules)|(?=Item 16\\. Form 10-K Summary))"
                    )
            );
            if (extracted7A != null) {
                item7A = String.valueOf(Jsoup.parse(extracted7A).text());
                itemsMap.put("7A", item7A);
            }
        }

        if (lowerCaseData.contains("item 8.")) {
            String extracted8 = extractSectionWithFallback(
                    mainDocData,
                    Arrays.asList("(?i)Item 8\\..*?(?i:(?=Item (?!1\\.|1A\\.|1B\\.|1C\\.|2\\.|3\\.|4\\.|5\\.|6\\.|7\\.|7A\\.|8\\.)\\d{1,2}\\.?[A-Z]?\\.)|(?=Item 9\\. Changes in and Disagreements With Accountants on Accounting and Financial Disclosure)|(?=Item 9A\\. Controls and Procedures)|(?=Item 9B\\. Other Information)|(?=Item 9C\\. Disclosure Regarding Foreign Jurisdictions that Prevent Inspections)|(?=Item 10\\. Directors, Executive Officers and Corporate Governance)|(?=Item 11\\. Executive Compensation)|(?=Item 12\\. Security Ownership of Certain Beneficial Owners and Management and Related Stockholder Matters)|(?=Item 13\\. Certain Relationships and Related Transactions, and Director Independence)|(?=Item 14\\. Principal Accounting Fees and Services)|(?=Item 15\\. Exhibits, Financial Statement Schedules)|(?=Item 16\\. Form 10-K Summary))")
            );
            if (extracted8 != null) {
                item8 = String.valueOf(Jsoup.parse(extracted8).text());
                itemsMap.put("8", item8);
            }
        }

        if (lowerCaseData.contains("item 9.")) {
            String extracted9 = extractSectionWithFallback(
                    mainDocData,
                    Arrays.asList(
                            "(?i)Item 9\\..*?(?i:(?=Item (?!1\\.|1A\\.|1B\\.|1C\\.|2\\.|3\\.|4\\.|5\\.|6\\.|7\\.|7A\\.|8\\.|9\\.)\\d{1,2}\\.?[A-Z]?\\.)|(?=Item 9A\\. Controls and Procedures)|(?=Item 9B\\. Other Information)|(?=Item 9C\\. Disclosure Regarding Foreign Jurisdictions that Prevent Inspections)|(?=Item 10\\. Directors, Executive Officers and Corporate Governance)|(?=Item 11\\. Executive Compensation)|(?=Item 12\\. Security Ownership of Certain Beneficial Owners and Management and Related Stockholder Matters)|(?=Item 13\\. Certain Relationships and Related Transactions, and Director Independence)|(?=Item 14\\. Principal Accounting Fees and Services)|(?=Item 15\\. Exhibits, Financial Statement Schedules)|(?=Item 16\\. Form 10-K Summary))"
                    )
            );
            if (extracted9 != null) {
                item9 = String.valueOf(Jsoup.parse(extracted9).text());
                itemsMap.put("9", item9);
            }
        }

        if (lowerCaseData.contains("item 9a.")) {
            String extracted9A = extractSectionWithFallback(
                    mainDocData,
                    Arrays.asList(
                            "(?i)Item 9A\\..*?(?i:(?=Item (?!1\\.|1A\\.|1B\\.|1C\\.|2\\.|3\\.|4\\.|5\\.|6\\.|7\\.|7A\\.|8\\.|9\\.|9A\\.)\\d{1,2}\\.?[A-Z]?\\.)|(?=Item 9B\\. Other Information)|(?=Item 9C\\. Disclosure Regarding Foreign Jurisdictions that Prevent Inspections)|(?=Item 10\\. Directors, Executive Officers and Corporate Governance)|(?=Item 11\\. Executive Compensation)|(?=Item 12\\. Security Ownership of Certain Beneficial Owners and Management and Related Stockholder Matters)|(?=Item 13\\. Certain Relationships and Related Transactions, and Director Independence)|(?=Item 14\\. Principal Accounting Fees and Services)|(?=Item 15\\. Exhibits, Financial Statement Schedules)|(?=Item 16\\. Form 10-K Summary))"
                    )
            );
            if (extracted9A != null) {
                item9A = String.valueOf(Jsoup.parse(extracted9A).text());
                itemsMap.put("9A", item9A);
            }
        }

        if (lowerCaseData.contains("item 9b.")) {
            String extracted9B = extractSectionWithFallback(
                    mainDocData,
                    Arrays.asList(
                            "(?i)Item 9B\\..*?(?i:(?=Item (?!1\\.|1A\\.|1B\\.|1C\\.|2\\.|3\\.|4\\.|5\\.|6\\.|7\\.|7A\\.|8\\.|9\\.|9A\\.|9B\\.)\\d{1,2}\\.?[A-Z]?\\.)|(?=Item 9C\\. Disclosure Regarding Foreign Jurisdictions that Prevent Inspections)|(?=Item 10\\. Directors, Executive Officers and Corporate Governance)|(?=Item 11\\. Executive Compensation)|(?=Item 12\\. Security Ownership of Certain Beneficial Owners and Management and Related Stockholder Matters)|(?=Item 13\\. Certain Relationships and Related Transactions, and Director Independence)|(?=Item 14\\. Principal Accounting Fees and Services)|(?=Item 15\\. Exhibits, Financial Statement Schedules)|(?=Item 16\\. Form 10-K Summary))"
                    )
            );
            if (extracted9B != null) {
                item9B = String.valueOf(Jsoup.parse(extracted9B).text());
                itemsMap.put("9B", item9B);
            }
        }

        if (lowerCaseData.contains("item 9c.")) {
            String extracted9C = extractSectionWithFallback(
                    mainDocData,
                    Arrays.asList(
                            "(?i)Item 9C\\..*?(?i:(?=Item (?!1\\.|1A\\.|1B\\.|1C\\.|2\\.|3\\.|4\\.|5\\.|6\\.|7\\.|7A\\.|8\\.|9\\.|9A\\.|9B\\.|9C\\.|9C\\.)\\d{1,2}\\.?[A-Z]?\\.)|(?=Item 10\\. Directors, Executive Officers and Corporate Governance)|(?=Item 11\\. Executive Compensation)|(?=Item 12\\. Security Ownership of Certain Beneficial Owners and Management and Related Stockholder Matters)|(?=Item 13\\. Certain Relationships and Related Transactions, and Director Independence)|(?=Item 14\\. Principal Accounting Fees and Services)|(?=Item 15\\. Exhibits, Financial Statement Schedules)|(?=Item 16\\. Form 10-K Summary))"
                    )
            );
            if (extracted9C != null) {
                item9C = String.valueOf(Jsoup.parse(extracted9C).text());
                itemsMap.put("9C", item9C);
            }
        }




        if (lowerCaseData.contains("item 10.")) {
            String extracted10 = extractSectionWithFallback(
                    mainDocData,
                    Arrays.asList(
                            "(?i)Item 10\\..*?(?i:(?=Item (?!10\\.|1\\.|1A\\.|1B\\.|1C\\.|2\\.|3\\.|4\\.|5\\.|6\\.|7\\.|7A\\.|8\\.|9\\.|9A\\.|9B\\.|9C\\.)\\d{1,2}\\.?[A-Z]?\\.)|(?=Item 11\\. Executive Compensation)|(?=Item 12\\. Security Ownership of Certain Beneficial Owners and Management and Related Stockholder Matters)|(?=Item 13\\. Certain Relationships and Related Transactions, and Director Independence)|(?=Item 14\\. Principal Accounting Fees and Services))"
                    )
            );
            if (extracted10 != null) {
                item10 = String.valueOf(Jsoup.parse(extracted10).text());
                itemsMap.put("10", item10);
            }
        }

        if (lowerCaseData.contains("item 11.")) {
            String extracted11 = extractSectionWithFallback(
                    mainDocData,
                    Arrays.asList(
                            "(?i)Item 11\\..*?(?i:(?=Item (?!11\\.|10\\.|1\\.|1A\\.|1B\\.|1C\\.|2\\.|3\\.|4\\.|5\\.|6\\.|7\\.|7A\\.|8\\.|9\\.|9A\\.|9B\\.|9C\\.)\\d{1,2}\\.?[A-Z]?\\.)|(?=Item 12\\. Security Ownership of Certain Beneficial Owners and Management and Related Stockholder Matters)|(?=Item 13\\. Certain Relationships and Related Transactions, and Director Independence)|(?=Item 14\\. Principal Accounting Fees and Services))"
                    )
            );
            if (extracted11 != null) {
                item11 = String.valueOf(Jsoup.parse(extracted11).text());
                itemsMap.put("11", item11);
            }
        }

        if (lowerCaseData.contains("item 12.")) {
            String extracted12 = extractSectionWithFallback(
                    mainDocData,
                    Arrays.asList(
                            "(?i)Item 12\\..*?(?i:(?=Item (?!12\\.|11\\.|10\\.|1\\.|1A\\.|1B\\.|1C\\.|2\\.|3\\.|4\\.|5\\.|6\\.|7\\.|7A\\.|8\\.|9\\.|9A\\.|9B\\.|9C\\.)\\d{1,2}\\.?[A-Z]?\\.)|(?=Item 13\\. Certain Relationships and Related Transactions, and Director Independence)|(?=Item 14\\. Principal Accounting Fees and Services))"
                    )
            );
            if (extracted12 != null) {
                item12 = String.valueOf(Jsoup.parse(extracted12).text());
                itemsMap.put("12", item12);
            }
        }

        if (lowerCaseData.contains("item 13.")) {
            String extracted13 = extractSectionWithFallback(
                    mainDocData,
                    Arrays.asList(
                            "(?i)Item 13\\..*?(?i:(?=Item (?!13\\.|12\\.|11\\.|10\\.|1\\.|1A\\.|1B\\.|1C\\.|2\\.|3\\.|4\\.|5\\.|6\\.|7\\.|7A\\.|8\\.|9\\.|9A\\.|9B\\.|9C\\.)\\d{1,2}\\.?[A-Z]?\\.)|(?=Item 14\\. Principal Accounting Fees and Services))"
                    )
            );
            if (extracted13 != null) {
                item13 = String.valueOf(Jsoup.parse(extracted13).text());
                itemsMap.put("13", item13);
            }
        }

        if (lowerCaseData.contains("item 14.")) {
            String extracted14 = extractSectionWithFallback(
                    mainDocData,
                    Arrays.asList(
                            "(?i)Item 14\\..*?(?i:(?=Item (?!14\\.|13\\.|12\\.|11\\.|10\\.|1\\.|1A\\.|1B\\.|1C\\.|2\\.|3\\.|4\\.|5\\.|6\\.|7\\.|7A\\.|8\\.|9\\.|9A\\.|9B\\.|9C\\.)\\d{1,2}\\.?[A-Z]?\\.)|(?=Item 15\\. Exhibits, Financial Statement Schedules)|(?=Item 16\\. Form 10-K Summary)|(?=SIGNATURES))"
                    )
            );
            if (extracted14 != null) {
                item14 = String.valueOf(Jsoup.parse(extracted14).text());
                itemsMap.put("14", item14);
            }
        }

        if (lowerCaseData.contains("item 15.")) {
            String extracted15 = extractSectionWithFallback(
                    mainDocData,
                    Arrays.asList(
                            "(?i)Item 15\\..*?(?i:(?=Item (?!15\\.|14\\.|13\\.|12\\.|11\\.|10\\.|1\\.|1A\\.|1B\\.|1C\\.|2\\.|3\\.|4\\.|5\\.|6\\.|7\\.|7A\\.|8\\.|9\\.|9A\\.|9B\\.|9C\\.)\\d{1,2}\\.?[A-Z]?\\.)|(?=Item 16\\. Form 10-K Summary)|(?=SIGNATURES))"
                    )
            );
            if (extracted15 != null) {
                item15 = String.valueOf(Jsoup.parse(extracted15).text());
                itemsMap.put("15", item15);
            }
        }

        if (lowerCaseData.contains("item 16.")) {
            String extracted16 = extractSectionWithFallback(
                    mainDocData,
                    Arrays.asList(
                            "(?i)Item 16\\..*?(?i:(?=Item (?!16\\.|15\\.|14\\.|13\\.|12\\.|11\\.|10\\.|1\\.|1A\\.|1B\\.|1C\\.|2\\.|3\\.|4\\.|5\\.|6\\.|7\\.|7A\\.|8\\.|9\\.|9A\\.|9B\\.|9C\\.)\\d{1,2}\\.?[A-Z]?\\.)|(?=SIGNATURES))"
                    )
            );
            if (extracted16 != null) {
                item16 = String.valueOf(Jsoup.parse(extracted16).text());
                itemsMap.put("16", item16);
            }
        }
        return itemsMap.get(itemName);

    }


    private String normalizeInvisibleCharacters(String text) {
        if (text == null) return null;
        // Replace invisible Unicode characters with regular space
        return text.replaceAll("[\\u00A0\\u2000-\\u200F\\u202F\\u205F\\u3000]", " ");
    }
}

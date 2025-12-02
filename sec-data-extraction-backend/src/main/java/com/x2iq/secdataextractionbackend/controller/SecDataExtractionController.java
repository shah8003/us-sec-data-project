package com.x2iq.secdataextractionbackend.controller;

import com.x2iq.secdataextractionbackend.services.forms.SecDataExtractionService;
import com.x2iq.secdataextractionbackend.services.forms.SecDef14AExtractorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class SecDataExtractionController {

    private final SecDataExtractionService secDataExtractionService;
    private final SecDef14AExtractorService secDef14AExtractorService;

    public SecDataExtractionController(SecDataExtractionService secDataExtractionService,
                                       SecDef14AExtractorService secDef14AExtractorService) {
        this.secDataExtractionService = secDataExtractionService;
        this.secDef14AExtractorService = secDef14AExtractorService;
    }

    @PostMapping("/getData")
    public ResponseEntity<String> getCustomData(@RequestParam String url,
                                                @RequestParam String formType,
                                                @RequestParam String selection) throws IOException {
        return ResponseEntity.ok(
                secDataExtractionService.extractFilingSourceUrl(url, formType, selection)
        );
    }

    /**
     * New endpoint: extract DEF 14A sections by allowed headings.
     * <p>
     * You have two options for input:
     * 1) Pass raw HTML (easier for debugging)
     * 2) Or extend later to fetch HTML via url + some service
     * <p>
     * For now, I’ll keep it simple: HTML via @RequestParam.
     */
    @GetMapping("/def14a/extractSections")
    public ResponseEntity<String> extractDef14ASections(
            @RequestParam String url) {

        String sections = secDef14AExtractorService.extractSections(url);
        return ResponseEntity.ok(sections);
    }
}

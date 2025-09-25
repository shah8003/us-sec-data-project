package com.x2iq.secdataextractionbackend.controller;

import com.x2iq.secdataextractionbackend.services.forms.SecDataExtractionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class SecDataExtractionController {
    private final SecDataExtractionService secDataExtractionService;

    public SecDataExtractionController(SecDataExtractionService secDataExtractionService) {
        this.secDataExtractionService = secDataExtractionService;
    }


    @PostMapping("/getData")
    public ResponseEntity<String> getCustomData(@RequestParam String url,
                                                @RequestParam String formType,
                                                @RequestParam String selection) throws IOException {
        return ResponseEntity.ok(secDataExtractionService.extractFilingSourceUrl(url, formType, selection));
    }
}

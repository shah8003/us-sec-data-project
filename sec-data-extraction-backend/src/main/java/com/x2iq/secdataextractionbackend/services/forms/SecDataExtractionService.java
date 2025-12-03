package com.x2iq.secdataextractionbackend.services.forms;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.net.URL;

@Service
public class SecDataExtractionService {
    private final Form8KExtractorService form8KExtractorService;
    private  final Form10QExtractorService form10QExtractorService;
    private final Form10KExtractorService form10KExtractorService;
    private final SecDef14AExtractorService secDef14AExtractorService;

    public SecDataExtractionService(Form8KExtractorService form8KExtractorService, Form10QExtractorService form10QExtractorService, Form10KExtractorService form10KExtractorService, SecDef14AExtractorService secDef14AExtractorService) {
        this.form8KExtractorService = form8KExtractorService;
        this.form10QExtractorService = form10QExtractorService;
        this.form10KExtractorService = form10KExtractorService;
        this.secDef14AExtractorService = secDef14AExtractorService;
    }

    public String extractFilingSourceUrl(String fullUrl, String formType, String itemNo) throws IOException {
        String response = "";
        String html = sendGetRequestWebClient(fullUrl);
        if (formType.equals("8-k")) {
           response = form8KExtractorService.extract8KSectionByText(html, itemNo);
        }
        if (formType.equals("10-q")) {
            response = form10QExtractorService.extractAll10QSections(html, itemNo);
        }
        if (formType.equals("10-k")) {
            response =form10KExtractorService.DataOf10KExtracted(html, itemNo);
        }

        if (formType.equals("def14a")) {
            response = secDef14AExtractorService.extractSections(html, itemNo);
        }

       return response;
    }


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


}

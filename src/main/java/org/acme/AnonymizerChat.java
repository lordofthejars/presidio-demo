package org.acme;

import io.quarkiverse.presidio.runtime.Analyzer;
import io.quarkiverse.presidio.runtime.Anonymizer;
import io.quarkiverse.presidio.runtime.model.AnalyzeRequest;
import io.quarkiverse.presidio.runtime.model.AnonymizeRequest;
import io.quarkiverse.presidio.runtime.model.AnonymizeResponse;
import io.quarkiverse.presidio.runtime.model.Mask;
import io.quarkiverse.presidio.runtime.model.RecognizerResultWithAnaysisExplanation;
import io.quarkiverse.presidio.runtime.model.Redact;
import io.quarkiverse.presidio.runtime.model.Replace;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;
import java.util.Collections;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@WebSocket(path = "/")
public class AnonymizerChat {



    @RestClient
    Analyzer analyzer;

    @RestClient
    Anonymizer anonymizer;

    @OnTextMessage(broadcast = true)
    public String onMessage(String word) {
        final List<RecognizerResultWithAnaysisExplanation> analyzed = analyze(word, "en");
        if (analyzed.isEmpty()) {
            return word;
        }
        return anonymize(word, analyzed).getText();
    }

    private List<RecognizerResultWithAnaysisExplanation> analyze(String word, String language) {
        AnalyzeRequest analyzeRequest = new AnalyzeRequest();
        analyzeRequest.text(word);
        analyzeRequest.language(language);

        return analyzer
            .analyzePost(analyzeRequest);
    }

    static Replace REPLACE = new Replace("ANONYMIZED");
    static Mask MASK = new Mask("*", 4, true);
    static Redact REDACT = new Redact();

    private AnonymizeResponse anonymize(String word, List<RecognizerResultWithAnaysisExplanation> recognizerResults) {

        AnonymizeRequest anonymizeRequest = new AnonymizeRequest();

        anonymizeRequest.setText(word);

        anonymizeRequest.putAnonymizersItem("DEFAULT", REPLACE);
        anonymizeRequest.putAnonymizersItem("PHONE_NUMBER", MASK);
        anonymizeRequest.putAnonymizersItem("EMAIL_ADDRESS", REDACT);
        anonymizeRequest.analyzerResults(
            Collections.unmodifiableList(recognizerResults));

        return this.anonymizer.anonymizePost(anonymizeRequest);
    }

}

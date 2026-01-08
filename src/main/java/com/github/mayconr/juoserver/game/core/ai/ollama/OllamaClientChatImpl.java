package com.github.mayconr.juoserver.game.core.ai.ollama;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OllamaClientChatImpl implements OllanaClient {
    private final String apiUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OllamaClientChatImpl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    @Override
    public Response chat(List<Message> messages, int maxTokens) {
        try {
            URL url = URI.create(apiUrl + "/api/chat").toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            // jobautomation/OpenEuroLLM-Portuguese:latest
            // mistral:instruct
            String json =
                    objectMapper.writeValueAsString(
                            new Payload(
                                    "jobautomation/OpenEuroLLM-Portuguese:latest",
                                    messages,
                                    false,
                                    maxTokens));

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = json.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int code = conn.getResponseCode();
            InputStream is =
                    (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();

            try (BufferedReader br =
                    new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    stringBuilder.append(line.trim());
                }
                final var responsePayload =
                        new ObjectMapper()
                                .readValue(stringBuilder.toString(), ResponsePayload.class)
                                .getMessage()
                                .getContent()
                                .replaceAll("```json", "")
                                .replaceAll("```", "");
                try {
                    return objectMapper.readValue(responsePayload, Response.class);
                } catch (JsonParseException exception) {
                    log.warn("Unable to parse json [" + responsePayload + "]");
                    return new Response(responsePayload, null);
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to reach Ollama", exception);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class Payload {
        private String model;
        private List<Message> messages;
        private Boolean stream;

        @JsonProperty("num_predict")
        private int maxTokens;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ResponsePayload {
        private Message message;
    }
}

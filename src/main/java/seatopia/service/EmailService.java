package seatopia.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class EmailService {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final String API_URL = "https://api.resend.com/emails";


    private static final String FROM = "Seatopia <onboarding@resend.dev>";

    private String apiKey() {
        return System.getenv("RESEND_API_KEY");
    }

    public void sendEmail(String to, String subject, String htmlContent) {
        String key = apiKey();
        if (key == null || key.isBlank()) {
            System.out.println("RESEND_API_KEY missing. Skipping email.");
            return;
        }
        if (to == null || to.isBlank()) return;

        String json = """
        {
          "from": "%s",
          "to": ["%s"],
          "subject": "%s",
          "html": "%s"
        }
        """.formatted(
                jsonEscape(FROM),
                jsonEscape(to.trim()),
                jsonEscape(subject),
                jsonEscape(htmlContent)
        );

        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + key)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            CLIENT.sendAsync(req, HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            System.out.println("Email failed: " + e.getMessage());
        }
    }

    private String jsonEscape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "")
                .replace("\n", "\\n");
    }
}

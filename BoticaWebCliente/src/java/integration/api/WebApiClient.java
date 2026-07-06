package integration.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class WebApiClient {

    private static final String DEFAULT_BASE_URL = "http://localhost:8081/BoticaAPIREST";

    private final Gson gson = new Gson();
    private final String baseUrl;

    public WebApiClient() {
        String configured = System.getProperty("botica.api.baseUrl");
        if (configured == null || configured.trim().isEmpty()) {
            configured = System.getenv("BOTICA_API_BASE_URL");
        }
        baseUrl = normalize(configured == null || configured.trim().isEmpty()
                ? DEFAULT_BASE_URL
                : configured.trim());
    }

    public ApiResult post(String path, Object body) throws IOException {
        return sendWithBody(path, "POST", body);
    }

    public ApiResult put(String path, Object body) throws IOException {
        return sendWithBody(path, "PUT", body);
    }

    public ApiResult delete(String path) throws IOException {
        HttpURLConnection connection = open(path, "DELETE");
        return read(connection);
    }

    private ApiResult sendWithBody(String path, String method, Object body) throws IOException {
        HttpURLConnection connection = open(path, method);
        connection.setDoOutput(true);
        byte[] payload = gson.toJson(body).getBytes(StandardCharsets.UTF_8);
        connection.setRequestProperty("Content-Length", String.valueOf(payload.length));
        try (OutputStream out = connection.getOutputStream()) {
            out.write(payload);
        }
        return read(connection);
    }

    public ApiResult get(String path) throws IOException {
        HttpURLConnection connection = open(path, "GET");
        return read(connection);
    }

    private HttpURLConnection open(String path, String method) throws IOException {
        URL url = new URL(baseUrl + (path.startsWith("/") ? path : "/" + path));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(6000);
        connection.setReadTimeout(10000);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        return connection;
    }

    private ApiResult read(HttpURLConnection connection) throws IOException {
        int status = connection.getResponseCode();
        InputStream stream = status >= 200 && status < 300
                ? connection.getInputStream()
                : connection.getErrorStream();
        String raw = readAll(stream);
        JsonObject json = raw == null || raw.trim().isEmpty()
                ? new JsonObject()
                : JsonParser.parseString(raw).getAsJsonObject();
        boolean success = json.has("success") && json.get("success").getAsBoolean();
        String message = json.has("message") && !json.get("message").isJsonNull()
                ? json.get("message").getAsString()
                : "Respuesta sin mensaje";
        return new ApiResult(status, success, message, json);
    }

    private String readAll(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    private String normalize(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    public static class ApiResult {
        private final int status;
        private final boolean success;
        private final String message;
        private final JsonObject json;

        ApiResult(int status, boolean success, String message, JsonObject json) {
            this.status = status;
            this.success = success;
            this.message = message;
            this.json = json;
        }

        public int getStatus() {
            return status;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public JsonObject getJson() {
            return json;
        }
    }
}

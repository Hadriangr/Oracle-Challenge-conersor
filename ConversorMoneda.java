package com.uniacc;

import com.google.gson.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Locale;
import java.util.Scanner;

public class ConversorMoneda {

    // Sugerencia: exporta tu API key como variable de entorno EXCHANGE_RATE_API_KEY
    private static final String API_KEY = System.getenv("EXCHANGE_RATE_API_KEY");

    public static void main(String[] args) {
        Locale.setDefault(new Locale("es", "CL"));
        Scanner sc = new Scanner(System.in);

        System.out.println("=== Conversor de Moneda (ExchangeRate-API) ===");
        System.out.print("Monto a convertir: ");
        double amount = leerDoubleSeguro(sc);

        System.out.print("Moneda origen (ISO 4217, ej. USD, EUR, CLP): ");
        String from = sc.next().trim().toUpperCase();

        System.out.print("Moneda destino (ISO 4217, ej. USD, EUR, CLP): ");
        String to = sc.next().trim().toUpperCase();

        try {
            if (API_KEY != null && !API_KEY.isBlank()) {
                convertirConPairEndpoint(from, to, amount);
            } else {
                System.out.println("\n(No se encontró API key; usando endpoint OPEN sólo para prueba diaria)");
                convertirConOpenEndpoint(from, to, amount);
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error de red: " + e.getMessage());
        } catch (ApiException e) {
            System.out.println("Error de API: " + e.getMessage());
        }
    }

    private static void convertirConPairEndpoint(String from, String to, double amount)
            throws IOException, InterruptedException, ApiException {

        // Endpoint 'pair' devuelve conversion_rate y conversion_result si pasas AMOUNT
        // GET https://v6.exchangerate-api.com/v6/YOUR-API-KEY/pair/EUR/GBP/AMOUNT
        String url = String.format(
                "https://v6.exchangerate-api.com/v6/%s/pair/%s/%s/%.8f",
                API_KEY, from, to, amount
        );

        JsonObject json = doGetJson(url);

        String result = json.get("result").getAsString(); // "success" | "error"
        if (!"success".equalsIgnoreCase(result)) {
            // error-type: unsupported-code | malformed-request | invalid-key | inactive-account | quota-reached ...
            String errorType = json.has("error-type") ? json.get("error-type").getAsString() : "unknown";
            throw new ApiException("result=error (" + errorType + ")");
        }

        double rate = json.get("conversion_rate").getAsDouble();
        double converted = json.get("conversion_result").getAsDouble();

        System.out.printf(Locale.ROOT,
                "\nTasa %s -> %s: %.6f\n%.4f %s = %.4f %s\n",
                from, to, rate, amount, from, converted, to);
    }

    private static void convertirConOpenEndpoint(String from, String to, double amount)
            throws IOException, InterruptedException, ApiException {

        // OPEN endpoint (sin key) devuelve todas las tasas desde una base:
        // GET https://open.er-api.com/v6/latest/USD  (actualiza 1 vez al día)
        // Tomamos base=from y multiplicamos monto * rate[to]
        String url = String.format("https://open.er-api.com/v6/latest/%s", from);
        JsonObject json = doGetJson(url);

        String result = json.get("result").getAsString();
        if (!"success".equalsIgnoreCase(result)) {
            String errorType = json.has("error-type") ? json.get("error-type").getAsString() : "unknown";
            throw new ApiException("result=error (" + errorType + ")");
        }

        JsonObject rates = json.getAsJsonObject("rates");
        if (!rates.has(to)) {
            throw new ApiException("Moneda destino no soportada: " + to);
        }
        double rate = rates.get(to).getAsDouble();
        double converted = amount * rate;

        System.out.printf(Locale.ROOT,
                "\n[OPEN] Tasa %s -> %s: %.6f\n%.4f %s = %.4f %s\n",
                from, to, rate, amount, from, converted, to);
        System.out.println("(Nota: el endpoint OPEN se actualiza 1 vez al día y requiere atribución en UI)");
    }

    private static JsonObject doGetJson(String url) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();
        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() == 429) {
            throw new IOException("Rate limit alcanzado (HTTP 429). Intenta más tarde.");
        }
        if (res.statusCode() >= 400) {
            throw new IOException("HTTP " + res.statusCode() + " al llamar " + url);
        }
        return JsonParser.parseString(res.body()).getAsJsonObject();
    }

    private static double leerDoubleSeguro(Scanner sc) {
        while (!sc.hasNextDouble()) {
            System.out.print("Por favor, ingresa un número válido: ");
            sc.next();
        }
        return sc.nextDouble();
    }

    private static class ApiException extends Exception {
        public ApiException(String msg) { super(msg); }
    }
}

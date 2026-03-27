import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) throws Exception {
        char[] pfxPassword = "poshacme".toCharArray();

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream("./.cert/cert.pfx")) {
            keyStore.load(fis, pfxPassword);
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, pfxPassword);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);

        HttpsServer server = HttpsServer.create(new InetSocketAddress(8443), 0);
        server.setHttpsConfigurator(new HttpsConfigurator(sslContext));
        
        server.createContext("/", new StaticFileHandler());
        server.createContext("/berechne", new BerechneHandler());
        server.createContext("/expert-berechne", new ExpertBerechneHandler());
        
        server.setExecutor(null);
        server.start();
        
        System.out.println("==================================================");
        System.out.println("Server gestartet!");
        System.out.println("Öffne https://localhost:8443 in deinem Browser.");
        System.out.println("==================================================");

        System.in.read(); 
        
        System.out.println("Server wird beendet...");
        server.stop(0); 
        System.out.println("Server erfolgreich gestoppt.");
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestedFile = exchange.getRequestURI().getPath();
            
            if (requestedFile.equals("/")) {
                requestedFile = "/index.html";
            } else if (requestedFile.equals("/favicon.ico")) {
                requestedFile = "/favicon/favicon.ico";
            }
            
            String filePath = "." + requestedFile; 
            byte[] fileBytes;
            try {
                fileBytes = Files.readAllBytes(Paths.get(filePath));
            } catch (IOException e) {
                String response = "404 - File Not Found";
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return;
            }

            String contentType = "text/plain";
            if (requestedFile.endsWith(".html")) {
                contentType = "text/html; charset=UTF-8";
            } else if (requestedFile.endsWith(".css")) {
                contentType = "text/css";
            } else if (requestedFile.endsWith(".js")) {
                contentType = "application/javascript";
            } else if (requestedFile.endsWith(".png")) {
                contentType = "image/png";
            } else if (requestedFile.endsWith(".ico")) {
                contentType = "image/x-icon";
            } else if (requestedFile.endsWith(".svg")) {
                contentType = "image/svg+xml";
            } else if (requestedFile.endsWith(".json") || requestedFile.endsWith(".webmanifest")) {
                contentType = "application/json";
            }
            
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, fileBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(fileBytes);
            }
        }
    }

    static class BerechneHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String requestBody = "";
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
                requestBody = reader.lines().collect(Collectors.joining("\n"));
            }

            try {
                Punkt p1 = parsePunkt(requestBody, "p1");
                Punkt p2 = parsePunkt(requestBody, "p2");
                
                Rechteck rechteck = new Rechteck(p1, p2);
                
                int breite = rechteck.getBreite();
                int hoehe = rechteck.getHoehe();
                int umfang = rechteck.getUmfang();
                boolean istQuadratisch = rechteck.istQuadratisch();

                String jsonResponse = String.format(
                    "{\"breite\": %d, \"hoehe\": %d, \"umfang\": %d, \"istQuadratisch\": %b}",
                    breite, hoehe, umfang, istQuadratisch
                );
                
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(jsonResponse.getBytes());
                }

            } catch (Exception e) {
                String response = "500 - Internal Server Error: " + e.getMessage();
                exchange.sendResponseHeaders(500, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }
        
        private Punkt parsePunkt(String json, String punktName) {
            Pattern pattern = Pattern.compile("\"" + punktName + "\":\\{\"x\":(\\d+),\"y\":(\\d+)\\}");
            Matcher matcher = pattern.matcher(json.replaceAll("\\s", ""));
            
            if (matcher.find()) {
                int x = Integer.parseInt(matcher.group(1));
                int y = Integer.parseInt(matcher.group(2));
                return new Punkt(x, y);
            }
            throw new IllegalArgumentException("Konnte " + punktName + " nicht im JSON finden oder parsen.");
        }
    }

    static class ExpertBerechneHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1); 
                return;
            }

            String requestBody = "";
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
                requestBody = reader.lines().collect(Collectors.joining("\n"));
            }

            try {
                Rechteck rA = parseFlatRechteck(requestBody, "a"); 
                Rechteck rB = parseFlatRechteck(requestBody, "b"); 

                String jsonResponse = "";

                if (requestBody.contains("\"operation\":\"intersect\"")) {
                    Rechteck schnitt = rA.schneiden(rB);
                    
                    if (schnitt != null) {
                        jsonResponse = String.format(
                            "{\"type\":\"intersect\",\"result\": {\"x\": %d, \"y\": %d, \"breite\": %d, \"hoehe\": %d, \"istQuadratisch\": %b}}",
                            schnitt.getLinksOben().getX(), schnitt.getLinksOben().getY(),
                            schnitt.getBreite(), schnitt.getHoehe(), schnitt.istQuadratisch()
                        );
                    } else {
                        jsonResponse = "{\"type\":\"intersect\",\"result\": null}";
                    }
                }

                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(jsonResponse.getBytes());
                }

            } catch (Exception e) {
                String response = "500 - Internal Server Error: " + e.getMessage();
                exchange.sendResponseHeaders(500, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }
        
        private Rechteck parseFlatRechteck(String json, String prefix) {
            Pattern pattern = Pattern.compile("\"" + prefix + "X\":(\\d+),\"" + prefix + "Y\":(\\d+),\"" + prefix + "Breite\":(\\d+),\"" + prefix + "Hoehe\":(\\d+)");
            Matcher matcher = pattern.matcher(json.replaceAll("\\s", ""));
            
            if (matcher.find()) {
                int x = Integer.parseInt(matcher.group(1));
                int y = Integer.parseInt(matcher.group(2));
                int w = Integer.parseInt(matcher.group(3));
                int h = Integer.parseInt(matcher.group(4));
                return new Rechteck(new Punkt(x, y), new Punkt(x + w, y + h));
            }
            throw new IllegalArgumentException("Konnte Rechteck " + prefix + " nicht parsen.");
        }
    }
}
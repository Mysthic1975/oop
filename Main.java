import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Main {

    public static void main(String[] args) throws IOException {
        // Erstellt einen HTTP-Server, der auf Port 8080 lauscht.
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        // Definiert die "Routen" oder "Pfade" und welche Logik dafür zuständig ist.
        // 1. Der "Root-Handler" liefert die statischen Dateien (index.html, style.css, script.js) aus.
        server.createContext("/", new StaticFileHandler());
        // 2. Der "API-Handler" für die Berechnungen.
        server.createContext("/berechne", new BerechneHandler());
        // 3. Der "API Handler" für die Experten-Berechnungen.
        server.createContext("/expert-berechne", new ExpertBerechneHandler());
        
        server.setExecutor(null); // Verwendet den Standard-Executor
        server.start();
        
        System.out.println("==================================================");
        System.out.println("Server gestartet!");
        System.out.println("Öffne http://localhost:8080 in deinem Browser.");
        System.out.println("==================================================");

        // Wartet, bis du in der Konsole Enter drückst
        System.in.read(); 
        
        // Fährt den Server sauber herunter (0 = sofort)
        System.out.println("Server wird beendet...");
        server.stop(0); 
        System.out.println("Server erfolgreich gestoppt.");
    }

    /**
     * Ein Handler, der statische Dateien wie HTML, CSS und JavaScript ausliefert.
     */
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestedFile = exchange.getRequestURI().getPath();
            
            // Wenn der Pfad "/" ist, liefere "index.html"
            if (requestedFile.equals("/")) {
                requestedFile = "/index.html";
            }
            
            // Lese die Datei vom Dateisystem
            String filePath = "." + requestedFile; // z.B. ./index.html
            byte[] fileBytes;
            try {
                fileBytes = Files.readAllBytes(Paths.get(filePath));
            } catch (IOException e) {
                // Wenn die Datei nicht gefunden wird, sende einen 404-Fehler
                String response = "404 - File Not Found";
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return;
            }

            // Setze den korrekten "Content-Type" basierend auf der Dateiendung
            String contentType = "text/plain";
            if (requestedFile.endsWith(".html")) {
                contentType = "text/html; charset=UTF-8";
            } else if (requestedFile.endsWith(".css")) {
                contentType = "text/css";
            } else if (requestedFile.endsWith(".js")) {
                contentType = "application/javascript";
            }
            
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, fileBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(fileBytes);
            }
        }
    }

    /**
     * Ein Handler, der die Berechnungsanfragen vom Frontend verarbeitet.
     * Erwartet eine POST-Anfrage mit JSON-Daten.
     */
    static class BerechneHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Nur POST-Anfragen erlauben
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
                return;
            }

            // Lese den JSON-Body aus der Anfrage
            String requestBody = "";
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
                requestBody = reader.lines().collect(Collectors.joining("\n"));
                }

            // Sehr einfache, manuelle JSON-Analyse, um externe Bibliotheken zu vermeiden
            try {
                Punkt p1 = parsePunkt(requestBody, "p1");
                Punkt p2 = parsePunkt(requestBody, "p2");
                
                // Unsere bestehende Logik verwenden
                Rechteck rechteck = new Rechteck(p1, p2);
                
                int breite = rechteck.getBreite();
                int hoehe = rechteck.getHoehe();
                int umfang = rechteck.getUmfang();
                boolean istQuadratisch = rechteck.istQuadratisch();

                // Erstelle die JSON-Antwort
                String jsonResponse = String.format(
                    "{\"breite\": %d, \"hoehe\": %d, \"umfang\": %d, \"istQuadratisch\": %b}",
                    breite, hoehe, umfang, istQuadratisch
                );
                
                // Sende die Antwort zurück an das Frontend
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(jsonResponse.getBytes());
                }

            } catch (Exception e) {
                // Fehlerbehandlung, falls die Analyse fehlschlägt
                String response = "500 - Internal Server Error: " + e.getMessage();
                exchange.sendResponseHeaders(500, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }
        
        /**
         * Eine Hilfsmethode, um einen Punkt (p1 oder p2) aus dem JSON-String zu parsen.
         */
        private Punkt parsePunkt(String json, String punktName) {
            // Verwendet einen regulären Ausdruck, um die x- und y-Werte für p1 oder p2 zu finden
            // Beispiel-Pattern für p1: "p1":\{"x":(\d+),"y":(\d+)\}
            Pattern pattern = Pattern.compile("\"" + punktName + "\":\\{\"x\":(\\d+),\"y\":(\\d+)\\}");
            Matcher matcher = pattern.matcher(json.replaceAll("\\s", "")); // Entfernt Leerzeichen
            
            if (matcher.find()) {
                int x = Integer.parseInt(matcher.group(1));
                int y = Integer.parseInt(matcher.group(2));
                return new Punkt(x, y);
            }
            throw new IllegalArgumentException("Konnte " + punktName + " nicht im JSON finden oder parsen.");
        }
    }

    /**
     * Neuer Handler für Experten-Berechnungen.
     */
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

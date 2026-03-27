# Rechteck-Rechner PRO

Eine webbasierte Java-Anwendung zur Berechnung und Visualisierung von Rechtecken. Das Projekt besteht aus einem eigenen Java-HTTP-Backend und einem responsiven HTML/JS/CSS-Frontend.

## 🚀 Funktionen

Das Programm bietet zwei Ansichten, zwischen denen nahtlos gewechselt werden kann:

### 1. Basis-Modus
* Eingabe von Startkoordinaten (X/Y), Breite und Höhe.
* Automatische Berechnung von:
  * Breite & Höhe
  * Umfang
  * Prüfung, ob es sich um ein Quadrat handelt
* Sofortige grafische Visualisierung auf einer Zeichenfläche.

### 2. Experten-Modus
* Gleichzeitige Eingabe von zwei Rechtecken (Rechteck A und Rechteck B).
* **Schnittfläche:** Berechnet und zeichnet die exakte Überschneidung beider Rechtecke.
* **Skalierung:** Vergrößert oder verkleinert Rechteck A um einen wählbaren Faktor.
* **Drehung:** Dreht Rechteck A um 90 Grad um einen wählbaren Ankerpunkt.
* Visuelle Darstellung mit durchsichtigen Farben (Blau/Grün) und einer massiven Hervorhebung für Schnittmengen.

## 🛠️ Technologien
* **Backend:** Java (Core). Nutzt den integrierten `com.sun.net.httpserver.HttpServer` für die REST-API und das Ausliefern der Dateien.
* **Frontend:** HTML5, CSS3, Vanilla JavaScript (keine externen Frameworks).
* **Architektur:** Objektorientierte Programmierung (Klassen `Rechteck`, `Punkt`) mit sauberer Trennung von Logik und Darstellung.

## 🏁 Starten der Anwendung

1. Kompiliere die Java-Dateien:
   Konsole:
   javac Main.java Punkt.java Rechteck.java

2. Starte die Anwendung:
   Konsole:
   java Main

3. Öffne deinen Webbrowser und navigiere zu:
   https://localhost:8080

4. Um den Server zu beenden, drücke in der Server-Konsole einfach die ENTER-Taste.

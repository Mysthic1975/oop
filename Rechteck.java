import java.lang.Math;

/**
 * Repräsentiert ein zweidimensionales, achsenparalleles Rechteck.
 * Das Rechteck wird durch zwei gegenüberliegende Eckpunkte definiert.
 * Intern wird es immer durch den linken oberen (minimalen x, y) und den
 * rechten unteren (maximalen x, y) Punkt gespeichert, um Berechnungen zu vereinfachen.
 */
public class Rechteck {
    // p1 ist der Eckpunkt mit den kleinsten Koordinaten (links oben).
    private Punkt p1;
    // p2 ist der Eckpunkt mit den größten Koordinaten (rechts unten).
    private Punkt p2;

    /**
     * Erzeugt ein Rechteck aus zwei beliebigen, gegenüberliegenden Eckpunkten.
     * Die Punkte werden automatisch normalisiert, sodass intern immer der linke obere
     * und der rechte untere Eckpunkt für die Repräsentation des Rechtecks verwendet werden.
     *
     * @param punktA Ein Eckpunkt des Rechtecks.
     * @param punktB Der gegenüberliegende Eckpunkt.
     */
    public Rechteck(Punkt punktA, Punkt punktB) {
        setzeEckenNormalisiert(punktA, punktB);
    }

    /**
     * Gibt den linken oberen Eckpunkt zurück.
     * @return Ein Punkt-Objekt, das die kleinste x- und y-Koordinate darstellt.
     */
    public Punkt getLinksOben() {
        return p1;
    }

    /**
     * Gibt den rechten unteren Eckpunkt zurück.
     * @return Ein Punkt-Objekt, das die größte x- und y-Koordinate darstellt.
     */
    public Punkt getRechtsUnten() {
        return p2;
    }
    
    /**
     * Berechnet die Breite des Rechtecks.
     * @return Die Differenz zwischen der größten und kleinsten x-Koordinate.
     */
    public int getBreite() {
        return this.p2.getX() - this.p1.getX();
    }

    /**
     * Berechnet die Höhe des Rechtecks.
     * @return Die Differenz zwischen der größten und kleinsten y-Koordinate.
     */
    public int getHoehe() {
        return this.p2.getY() - this.p1.getY();
    }

    /**
     * Verschiebt das gesamte Rechteck im Koordinatensystem.
     * Beide Eckpunkte werden um die gleichen Delta-Werte verschoben.
     *
     * @param dx Die Verschiebung in x-Richtung.
     * @param dy Die Verschiebung in y-Richtung.
     */
    public void verschieben(int dx, int dy) {
        this.p1.verschieben(dx, dy);
        this.p2.verschieben(dx, dy);
    }

    /**
     * Prüft, ob das Rechteck quadratisch ist (d.h. Breite ist gleich Höhe).
     *
     * @return true, wenn Breite und Höhe gleich sind, sonst false.
     */
    public boolean istQuadratisch() {
        return getBreite() == getHoehe();
    }

    /**
     * Bestimmt den Umfang des Rechtecks.
     * Formel: 2 * (Breite + Höhe).
     *
     * @return Der berechnete Umfang.
     */
    public int getUmfang() {
        return 2 * (getBreite() + getHoehe());
    }

    /**
     * Gibt eine textuelle Darstellung des Rechtecks zurück.
     * @return Ein String, der die Eckpunkte, Breite und Höhe enthält.
     */
    @Override
    public String toString() {
        return "Rechteck[p1=" + p1 + ", p2=" + p2 + ", Breite=" + getBreite() + ", Hoehe=" + getHoehe() + "]";
    }

    // --- Private Hilfsmethoden ---

    /**
     * Normalisiert zwei Punkte, um den linken oberen und rechten unteren Eckpunkt zu bestimmen und zu setzen.
     * @param punktA Ein Eckpunkt.
     * @param punktB Der andere Eckpunkt.
     */
    private void setzeEckenNormalisiert(Punkt punktA, Punkt punktB) {
        int minX = Math.min(punktA.getX(), punktB.getX());
        int minY = Math.min(punktA.getY(), punktB.getY());
        int maxX = Math.max(punktA.getX(), punktB.getX());
        int maxY = Math.max(punktA.getY(), punktB.getY());
        
        this.p1 = new Punkt(minX, minY);
        this.p2 = new Punkt(maxX, maxY);
    }


    // --- Experten-Methoden (optional) ---

    /**
     * Skaliert das Rechteck um einen gegebenen Faktor.
     * Die Skalierung erfolgt relativ zum linken oberen Eckpunkt, d.h. dieser Punkt bleibt an seiner Position.
     *
     * @param faktor Der ganzzahlige Faktor, um den vergroessert/verkleinert wird.
     * @throws IllegalArgumentException wenn der Faktor nicht positiv ist.
     */
    public void skalieren(int faktor) {
        if (faktor <= 0) {
            throw new IllegalArgumentException("Skalierungsfaktor muss positiv sein (> 0).");
        }
        
        int neueBreite = getBreite() * faktor;
        int neueHoehe = getHoehe() * faktor;

        // Konsistent: Neues Punkt-Objekt erzeugen, statt Setter zu verwenden
        this.p2 = new Punkt(this.p1.getX() + neueBreite, this.p1.getY() + neueHoehe);
    }

    /**
     * Dreht das Rechteck um 90 Grad im Uhrzeigersinn um einen seiner Eckpunkte.
     * Nach der Drehung werden Breite und Höhe vertauscht. Die Position wird relativ zum Ankerpunkt neu berechnet.
     *
     * @param anker Der Eckpunkt, um den gedreht wird. Muss einer der vier Eckpunkte des Rechtecks sein.
     */
    public void drehen(Punkt anker) {
        int breite = getBreite();
        int hoehe = getHoehe();

        // Identifiziere die vier Eckpunkte des Rechtecks
        Punkt linksOben = this.p1;
        Punkt rechtsUnten = this.p2;
        Punkt linksUnten = new Punkt(p1.getX(), p2.getY());
        Punkt rechtsOben = new Punkt(p2.getX(), p1.getY());

        Punkt newP1 = null;
        Punkt newP2 = null;

        // Führe die Drehung basierend auf dem Ankerpunkt durch
        if (anker.equals(linksOben)) { // Anker: links-oben
            newP1 = new Punkt(anker.getX(), anker.getY() - breite);
            newP2 = new Punkt(anker.getX() + hoehe, anker.getY());
        } else if (anker.equals(rechtsUnten)) { // Anker: rechts-unten
            newP1 = new Punkt(anker.getX() - hoehe, anker.getY());
            newP2 = new Punkt(anker.getX(), anker.getY() + breite);
        } else if (anker.equals(linksUnten)) { // Anker: links-unten
            newP1 = new Punkt(anker.getX(), anker.getY());
            newP2 = new Punkt(anker.getX() + hoehe, anker.getY() + breite);
        } else if (anker.equals(rechtsOben)) { // Anker: rechts-oben
            newP1 = new Punkt(anker.getX() - hoehe, anker.getY() - breite);
            newP2 = new Punkt(anker.getX(), anker.getY());
        } else {
            System.out.println("Ankerpunkt ist keine Ecke des Rechtecks. Drehung nicht möglich.");
            return;
        }
        
        // Normalisiere die neuen Punkte, um die interne Konsistenz sicherzustellen
        setzeEckenNormalisiert(newP1, newP2);
    }


    /**
     * Bestimmt die Schnittfläche dieses Rechtecks mit einem anderen.
     *
     * @param anderes Das andere Rechteck, mit dem die Schnittmenge gebildet werden soll.
     * @return Ein neues Rechteck-Objekt, das die Schnittfläche darstellt, 
     *         oder null, wenn es keine Überschneidung gibt.
     */
    public Rechteck schneiden(Rechteck anderes) {
        // Finde die maximalen "linken oberen" Koordinaten
        int schnittX1 = Math.max(this.p1.getX(), anderes.p1.getX());
        int schnittY1 = Math.max(this.p1.getY(), anderes.p1.getY());

        // Finde die minimalen "rechten unteren" Koordinaten
        int schnittX2 = Math.min(this.p2.getX(), anderes.p2.getX());
        int schnittY2 = Math.min(this.p2.getY(), anderes.p2.getY());

        // Prüfen, ob eine gültige, positive Schnittfläche existiert (linker Rand muss kleiner als rechter sein etc.)
        if (schnittX1 < schnittX2 && schnittY1 < schnittY2) {
            Punkt schnittP1 = new Punkt(schnittX1, schnittY1);
            Punkt schnittP2 = new Punkt(schnittX2, schnittY2);
            return new Rechteck(schnittP1, schnittP2);
        } else {
            // Keine Überschneidung
            return null;
        }
    }
}

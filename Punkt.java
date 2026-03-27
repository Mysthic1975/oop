/**
 * Repräsentiert einen Punkt in einem zweidimensionalen, kartesischen Koordinatensystem.
 * Die Koordinaten sind ganzzahlige Werte.
 */
public class Punkt {
    private int x;
    private int y;

    /**
     * Erzeugt ein neues Punkt-Objekt mit den angegebenen Koordinaten.
     *
     * @param x Die x-Koordinate des Punktes.
     * @param y Die y-Koordinate des Punktes.
     */
    public Punkt(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Gibt die x-Koordinate des Punktes zurück.
     *
     * @return Die x-Koordinate.
     */
    public int getX() {
        return x;
    }

    /**
     * Setzt die x-Koordinate des Punktes auf einen neuen Wert.
     *
     * @param x Der neue Wert für die x-Koordinate.
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Gibt die y-Koordinate des Punktes zurück.
     *
     * @return Die y-Koordinate.
     */
    public int getY() {
        return y;
    }

    /**
     * Setzt die y-Koordinate des Punktes auf einen neuen Wert.
     *
     * @param y Der neue Wert für die y-Koordinate.
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * Verschiebt den Punkt um die angegebenen Delta-Werte.
     * Die neuen Koordinaten sind (x + dx, y + dy).
     *
     * @param dx Die Änderung in x-Richtung.
     * @param dy Die Änderung in y-Richtung.
     */
    public void verschieben(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    /**
     * Gibt eine textuelle Darstellung des Punktes zurück.
     *
     * @return Ein String im Format "Punkt(x, y)".
     */
    @Override
    public String toString() {
        return "Punkt(" + x + ", " + y + ")";
    }

    /**
     * Vergleicht diesen Punkt mit einem anderen Objekt auf Gleichheit.
     * Zwei Punkte sind gleich, wenn ihre x- und y-Koordinaten übereinstimmen.
     *
     * @param obj Das Objekt, mit dem verglichen werden soll.
     * @return true, wenn die Objekte gleiche Punkte repräsentieren, sonst false.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Punkt punkt = (Punkt) obj;
        return x == punkt.x && y == punkt.y;
    }
}

document.addEventListener('DOMContentLoaded', () => {
    // === BASIS-MODUS LOGIK ===
    const form = document.getElementById('rechteck-form');
    const ergebnisDiv = document.getElementById('ergebnis');
    const zeichnungsflaeche = document.getElementById('zeichnungsflaeche');

    if (form) { // Prüfen ob das Element existiert
        handleBerechnung();
        form.addEventListener('submit', (event) => {
            event.preventDefault();
            handleBerechnung();
        });
    }

    /**
     * Diese Funktion liest die Eingaben des Benutzers, validiert sie, 
     * zeichnet das Rechteck und sendet die Daten an das Java-Backend, 
     * um Berechnungen (Fläche, Umfang, etc.) durchzuführen.
     */
    function handleBerechnung() {
        const startX = parseInt(document.getElementById('startX').value);
        const startY = parseInt(document.getElementById('startY').value);
        const breite = parseInt(document.getElementById('breite').value);
        const hoehe = parseInt(document.getElementById('hoehe').value);

        if (breite <= 0 || hoehe <= 0) {
            ergebnisDiv.innerHTML = '<p style="color: red;">Fehler: Breite und Höhe müssen positive Zahlen sein!</p>';
            zeichnungsflaeche.innerHTML = '';
            return;
        }

        zeichneRechteck(startX, startY, breite, hoehe);

        const daten = {
            p1: { x: startX, y: startY },
            p2: { x: startX + breite, y: startY + hoehe }
        };

        // Ruft das Backend asynchron (im Hintergrund) auf. 
        // Wir senden einen POST-Request an '/berechne' und wandeln unser JS-Objekt 'daten' in einen JSON-String um.
        fetch('/berechne', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(daten),
        })
            .then(response => {
                if (!response.ok) return response.text().then(text => { throw new Error('Server-Fehler: ' + text) });
                return response.json();
            })
            .then(ergebnisse => zeigeErgebnisse(ergebnisse))
            .catch(error => {
                console.error('Fehler bei der Kommunikation mit dem Backend:', error);
                ergebnisDiv.innerHTML = `<p style="color: red;">Kommunikationsfehler: Konnte das Backend nicht erreichen.<br><small>${error.message}</small></p>`;
            });
    }

    /**
     * Zeichnet ein Rechteck auf der Basis-Zeichnungsfläche im HTML.
     * Erstellt dazu ein neues div-Element und positioniert es mithilfe von CSS und
     * den abgerufenen x/y-Koordinaten.
     */
    function zeichneRechteck(x, y, breite, hoehe) {
        zeichnungsflaeche.innerHTML = '';
        const rectShape = document.createElement('div');
        rectShape.className = 'rechteck-shape';
        rectShape.style.left = x + 'px';
        rectShape.style.top = y + 'px';
        rectShape.style.width = breite + 'px';
        rectShape.style.height = hoehe + 'px';
        zeichnungsflaeche.appendChild(rectShape);
    }

    /**
     * Nimmt das JSON-Ergebnis vom Backend und stellt es als HTML formatiert im Browser dar,
     * damit der Benutzer die Eigenschaften des Rechtecks als Text lesen kann.
     */
    function zeigeErgebnisse(ergebnisse) {
        ergebnisDiv.innerHTML = `
            <p><strong>Breite:</strong> ${ergebnisse.breite}</p>
            <p><strong>Höhe:</strong> ${ergebnisse.hoehe}</p>
            <p><strong>Umfang:</strong> ${ergebnisse.umfang}</p>
            <p><strong>Ist ein Quadrat?</strong> ${ergebnisse.istQuadratisch ? 'Ja' : 'Nein'}</p>
        `;
    }

    // === NAVIGATION & EXPERTEN-MODUS ===
    setupNavigation();
    initExpertView();

    function setupNavigation() {
        const navBasic = document.getElementById('nav-basic');
        const navExpert = document.getElementById('nav-expert');
        const basicView = document.getElementById('basic-view');
        const expertView = document.getElementById('expert-view');

        navBasic.addEventListener('click', () => {
            navBasic.classList.add('active'); navExpert.classList.remove('active');
            basicView.classList.add('active'); expertView.classList.remove('active');
        });
        navExpert.addEventListener('click', () => {
            navExpert.classList.add('active'); navBasic.classList.remove('active');
            expertView.classList.add('active'); basicView.classList.remove('active');
            updateExpertCanvas();
        });
    }

    function initExpertView() {
        const inputIds = ['startX_A', 'startY_A', 'breite_A', 'hoehe_A', 'startX_B', 'startY_B', 'breite_B', 'hoehe_B'];
        inputIds.forEach(id => {
            const el = document.getElementById(id);
            if (el) el.addEventListener('input', updateExpertCanvas);
        });

        const btnSchnitt = document.getElementById('btn-schnitt');
        if (btnSchnitt) btnSchnitt.addEventListener('click', () => handleExpertOp('intersect'));

        const btnSkaliere = document.getElementById('btn-skaliere');
        if (btnSkaliere) btnSkaliere.addEventListener('click', () => handleExpertOp('scale'));

        const btnDrehe = document.getElementById('btn-drehe');
        if (btnDrehe) btnDrehe.addEventListener('click', () => handleExpertOp('rotate'));
    }

    function updateExpertCanvas() {
        const flaeche = document.getElementById('expert-flaeche');
        if (!flaeche) return;

        flaeche.innerHTML = '';

        const getVal = (id) => parseInt(document.getElementById(id).value) || 0;

        zeichneZusaetzlichesRechteck(flaeche, getVal('startX_A'), getVal('startY_A'), getVal('breite_A'), getVal('hoehe_A'), 'rechteck-shape-a');
        zeichneZusaetzlichesRechteck(flaeche, getVal('startX_B'), getVal('startY_B'), getVal('breite_B'), getVal('hoehe_B'), 'rechteck-shape-b');
    }

    function zeichneZusaetzlichesRechteck(targetFlaeche, x, y, breite, hoehe, className) {
        if (breite <= 0 || hoehe <= 0) return;
        const rect = document.createElement('div');
        rect.className = `rechteck-shape ${className}`;
        rect.style.left = x + 'px'; rect.style.top = y + 'px';
        rect.style.width = breite + 'px'; rect.style.height = hoehe + 'px';
        targetFlaeche.appendChild(rect);
    }

    /**
     * Führt eine "Experten-Operation" (Schneiden, Skalieren, Drehen) aus.
     * Sammelt die Daten der beiden Rechtecke und der spezifischen Operation,
     * sendet diese an das Backend und aktualisiert die Ansicht basierend auf der Antwort.
     */
    function handleExpertOp(operation) {
        const getVal = (id) => parseInt(document.getElementById(id).value);
        const flaeche = document.getElementById('expert-flaeche');
        const ergebnisDiv = document.getElementById('ergebnis-expert');

        const daten = {
            operation: operation,
            aX: getVal('startX_A'), aY: getVal('startY_A'), aBreite: getVal('breite_A'), aHoehe: getVal('hoehe_A'),
            bX: getVal('startX_B'), bY: getVal('startY_B'), bBreite: getVal('breite_B'), bHoehe: getVal('hoehe_B')
        };

        if (operation === 'scale') {
            daten.scaleFactor = parseInt(document.getElementById('expScaleFactor').value);
        } else if (operation === 'rotate') {
            daten.rotateCorner = document.getElementById('expRotateCorner').value;
        }

        fetch('/expert-berechne', {
            method: 'POST', headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(daten)
        })
            .then(response => response.json())
            .then(daten => {
                if (daten.type === 'intersect') {
                    updateExpertCanvas();
                    if (daten.result) {
                        zeichneZusaetzlichesRechteck(flaeche, daten.result.x, daten.result.y, daten.result.breite, daten.result.hoehe, 'rechteck-shape-schnitt');
                        ergebnisDiv.innerHTML = `<p><strong>Schnittfläche gefunden:</strong> Breite: ${daten.result.breite}, Höhe: ${daten.result.hoehe} (Quadrat: ${daten.result.istQuadratisch})</p>`;
                    } else {
                        ergebnisDiv.innerHTML = '<p>Keine Schnittfläche vorhanden.</p>';
                    }
                } else if (daten.type === 'scale' || daten.type === 'rotate') {
                    if (daten.result) {
                        document.getElementById('startX_A').value = daten.result.x;
                        document.getElementById('startY_A').value = daten.result.y;
                        document.getElementById('breite_A').value = daten.result.breite;
                        document.getElementById('hoehe_A').value = daten.result.hoehe;

                        updateExpertCanvas();
                        const opName = daten.type === 'scale' ? 'Skalierung' : 'Drehung';
                        ergebnisDiv.innerHTML = `<p><strong>${opName} erfolgreich:</strong> Rechteck A angepasst (Breite: ${daten.result.breite}, Höhe: ${daten.result.hoehe})</p>`;
                    } else if (daten.error) {
                        ergebnisDiv.innerHTML = `<p style="color: red;">Fehler: ${daten.error}</p>`;
                    }
                }
            });
    }
});
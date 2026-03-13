
-- Categories: DRAW (Zeichnen), ACT (Pantomime), DESCRIBE (Erklären)
-- Points: 1-6 basierend auf Schwierigkeit

-- Activity 18+ Edition Import
-- Einfache Wörter, Punkte 4-6

ALTER SEQUENCE Word_SEQ RESTART WITH 1;

-- DESCRIBE Kategorie
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Kuss', 'Lippen auf Lippen', 4, 'DESCRIBE');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Nackt', 'Ohne jegliche Kleidung', 4, 'DESCRIBE');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Flirten', 'Jemanden mit Blicken und Worten anschwärmen', 4, 'DESCRIBE');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Kater', 'Schlechtes Gefühl nach zu viel Alkohol', 4, 'DESCRIBE');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Tanga', 'Sehr kleines Unterwäschestück', 4, 'DESCRIBE');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Stripper', 'Person die sich vor Publikum auszieht', 4, 'DESCRIBE');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Verhütung', 'Schutz vor ungewollter Schwangerschaft', 4, 'DESCRIBE');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Brust', 'Weiblicher Oberkörper', 4, 'DESCRIBE');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Scheidung', 'Das Ende einer Ehe', 4, 'DESCRIBE');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Kondom', 'Gummischutz beim Sex', 4, 'DESCRIBE');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Affäre', 'Heimliche Liebesbeziehung', 4, 'DESCRIBE');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Erotik', 'Alles was sexuell anregend ist', 4, 'DESCRIBE');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Orgasmus', 'Höhepunkt beim Sex', 4, 'DESCRIBE');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Dessous', 'Verführerische Unterwäsche', 4, 'DESCRIBE');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Po', 'Hinterteil des menschlichen Körpers', 4, 'DESCRIBE');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Porno', 'Film mit explizitem sexuellem Inhalt', 5, 'DESCRIBE');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Vibrator', 'Elektrisches Gerät für sexuelle Stimulation', 5, 'DESCRIBE');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Fremdgehen', 'Seinem Partner untreu sein', 5, 'DESCRIBE');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Erektion', 'Steifes männliches Glied', 5, 'DESCRIBE');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Fetisch', 'Sexuelle Fixierung auf einen bestimmten Gegenstand', 5, 'DESCRIBE');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Handschellen', 'Fesselwerkzeug aus Metall', 5, 'DESCRIBE');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Prostitution', 'Sex gegen Bezahlung', 5, 'DESCRIBE');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Libido', 'Sexuelles Verlangen', 5, 'DESCRIBE');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Peitsche', 'Hilfsmittel aus dem BDSM-Bereich', 5, 'DESCRIBE');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Sexstellung', 'Position beim Geschlechtsverkehr', 5, 'DESCRIBE');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Voyeur', 'Person die andere heimlich beim Ausziehen beobachtet', 6, 'DESCRIBE');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Exhibitionist', 'Person die sich gerne öffentlich nackt zeigt', 6, 'DESCRIBE');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Sado', 'Lust am Schmerz eines anderen', 6, 'DESCRIBE');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Polygamie', 'Gleichzeitig mit mehreren Partnern verheiratet', 6, 'DESCRIBE');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Pheromone', 'Duftstoffe die sexuelle Anziehung auslösen', 6, 'DESCRIBE');

-- DRAW Kategorie
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Kuss', 'Lippen auf Lippen', 4, 'DRAW');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Kondom', 'Gummischutz beim Sex', 4, 'DRAW');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Tanga', 'Sehr kleines Unterwäschestück', 4, 'DRAW');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Brust', 'Weiblicher Oberkörper', 4, 'DRAW');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Po', 'Hinterteil des menschlichen Körpers', 4, 'DRAW');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Dessous', 'Verführerische Unterwäsche', 4, 'DRAW');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Whirlpool', 'Badewanne mit Wasserdüsen', 4, 'DRAW');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Stripperstange', 'Stange für erotische Tanzshows', 4, 'DRAW');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Erotikshop', 'Laden für Sexspielzeug', 4, 'DRAW');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Nacktfoto', 'Foto ohne jegliche Kleidung', 4, 'DRAW');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Ehebett', 'Das gemeinsame Bett eines Ehepaares', 4, 'DRAW');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Porno', 'Film mit explizitem sexuellem Inhalt', 4, 'DRAW');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Liebeshotel', 'Stundenhotel für romantische Begegnungen', 4, 'DRAW');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Playboy', 'Bekanntes Erotikmagazin', 4, 'DRAW');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Handschellen', 'Fesselwerkzeug aus Metall', 5, 'DRAW');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Vibrator', 'Elektrisches Gerät für sexuelle Stimulation', 5, 'DRAW');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Peitsche', 'Hilfsmittel aus dem BDSM-Bereich', 5, 'DRAW');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Peepshow', 'Fenster für erotische Darbietungen', 5, 'DRAW');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Domina', 'Dominante Frau mit Kostüm und Peitsche', 5, 'DRAW');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Rotlichtviertel', 'Stadtgebiet mit Prostitution', 5, 'DRAW');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Latex-Outfit', 'Enges Gummi-Kostüm für erotische Zwecke', 5, 'DRAW');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Erektion', 'Steifes männliches Glied', 5, 'DRAW');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Tabledance', 'Erotischer Tanz auf dem Tisch', 5, 'DRAW');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Swinger-Club', 'Treffpunkt für Partnertausch', 5, 'DRAW');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Keuschheitsgürtel', 'Historisches Gerät gegen Sex', 6, 'DRAW');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Fesselspiele', 'Erotisches Spiel mit Fesselung', 6, 'DRAW');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Aphrodite', 'Griechische Göttin der Liebe und Sexualität', 6, 'DRAW');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Tantra', 'Spirituelle Praxis mit Fokus auf Sexualenergie', 6, 'DRAW');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Kamasutra', 'Buch über Sexstellungen aus Indien', 6, 'DRAW');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Orgasmusgesicht', 'Gesichtsausdruck beim Höhepunkt', 6, 'DRAW');

-- ACT Kategorie
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Flirten', 'Jemanden mit Blicken anschwärmen', 4, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Betrunken sein', 'Stark alkoholisiert spielen', 4, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Kater haben', 'Elenden Morgen nach Alkohol spielen', 4, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Weinen nach Trennung', 'Herzschmerz nach Beziehungsende spielen', 4, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Schnarchen', 'Lautes Schlafgeräusch nachmachen', 4, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Eifersüchtig sein', 'Übertriebene Eifersucht zeigen', 4, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Heiratsantrag', 'Auf die Knie fallen und fragen', 4, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Ghosten', 'Jemanden mitten im Gespräch ignorieren', 4, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Beziehungsstreit', 'Typischen Paarkrach spielen', 4, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Nackt duschen', 'Person beim Duschen ohne Kleidung darstellen', 4, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Verführen', 'Jemanden mit Blicken und Gesten anlocken', 4, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Striptease', 'Sich langsam und verführerisch ausziehen', 4, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Strip Poker verlieren', 'Verlierenden Spieler beim Ausziehen spielen', 4, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Sex vortäuschen', 'Übertriebene Geräusche und Bewegungen machen', 5, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Orgasmus vortäuschen', 'Überzeugend einen Höhepunkt vorspielen', 5, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Domina', 'Dominante Frau mit Befehlston spielen', 5, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Exhibitionist', 'Person die sich zeigen will darstellen', 5, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Voyeur', 'Jemanden beim heimlichen Beobachten spielen', 5, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Pole Dance', 'An imaginärer Stange erotisch tanzen', 5, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Gigolo', 'Charmanten männlichen Escort spielen', 5, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Fremdgehen gestehen', 'Schuldbewusstes Geständnis vorspielen', 5, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Masochist', 'Person die Schmerz genießt darstellen', 6, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Nymphomanin', 'Frau mit extremem Sexualtrieb spielen', 6, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'BDSM-Master', 'Dominante Person aus der BDSM-Szene spielen', 6, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Femme Fatale', 'Gefährliche Verführerin verkörpern', 6, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Don Juan', 'Legendären Frauenheld spielen', 6, 'ACT');

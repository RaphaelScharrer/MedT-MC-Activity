
-- Categories: DRAW (Zeichnen), ACT (Pantomime), DESCRIBE (Erklären)
-- Points: 1-6 basierend auf Schwierigkeit

-- Activity 18+ Edition Import
-- Einfache Wörter, Punkte 4-6

ALTER SEQUENCE Word_SEQ RESTART WITH 1;

-- DESCRIBE Kategorie
-- ACT Kategorie
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Zähne putzen', 'Morgendliche Mundhygiene darstellen', 4, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Schlafen', 'Ruhig im Bett liegen und träumen', 4, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Radfahren', 'Auf einem unsichtbaren Fahrrad treten', 4, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Schwimmen', 'Bewegungen im Wasser nachmachen', 4, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Kochen', 'Am Herd rühren und schneiden', 4, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Schnarchen', 'Lautes Schlafgeräusch und Körperhaltung zeigen', 4, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Telefonieren', 'Mit imaginärem Handy am Ohr reden', 4, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Foto machen', 'Mit imaginärer Kamera jemanden ablichten', 4, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Flirten', 'Jemanden mit Blicken und Gesten anschwärmen', 4, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Weinen', 'Tränen und Schluchzen vorspielen', 4, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Lachen', 'Unkontrollierbares Lachen darstellen', 4, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Tanzen', 'Sich zur Musik bewegen', 4, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Eifersüchtig sein', 'Misstrauische Blicke und verschränkte Arme zeigen', 4, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Heiratsantrag', 'Auf die Knie fallen und imaginären Ring überreichen', 4, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Autofahren', 'Am imaginären Lenkrad sitzen und lenken', 4, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Einschlafen', 'Langsam die Augen schließen und vornüber fallen', 4, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Haare föhnen', 'Mit imaginärem Föhn die Haare trocknen', 4, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Einkaufen', 'Mit imaginärem Einkaufswagen durch Reihen schieben', 4, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Kater haben', 'Verkatert mit Kopfschmerzen aufwachen', 4, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Betrunken sein', 'Schwankend und lallend gehen', 4, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Striptease', 'Sich langsam und verführerisch ausziehen', 5, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Verführen', 'Jemanden mit Blicken und Gesten anlocken', 5, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Streit schlichten', 'Zwischen zwei streitenden Personen vermitteln', 5, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Prüfungsangst', 'Schwitzen und zittern vor einem leeren Blatt', 5, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Ghosten', 'Jemanden mitten im Gespräch einfach ignorieren', 5, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Beziehungsstreit', 'Typischen Paarkrach mit Gesten spielen', 5, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Schminken', 'Sorgfältig Make-up vor imaginärem Spiegel auftragen', 5, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Selfie machen', 'Arm ausstrecken und übertrieben posieren', 5, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Fremdgehen gestehen', 'Schuldbewusstes Geständnis mit Gesten vorspielen', 5, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Pole Dance', 'An imaginärer Stange erotisch tanzen', 5, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Sex vortäuschen', 'Übertriebene Geräusche und Bewegungen machen', 6, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Orgasmus vortäuschen', 'Überzeugend einen Höhepunkt vorspielen', 6, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Domina', 'Dominante Frau mit Befehlston und Gesten spielen', 6, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Voyeur', 'Jemanden beim heimlichen Beobachten darstellen', 6, 'ACT');
INSERT INTO word (id, word, definition, points, category) VALUES (nextval('Word_SEQ'), 'Femme Fatale', 'Gefährliche Verführerin mit Blicken und Gang verkörpern', 6, 'ACT');
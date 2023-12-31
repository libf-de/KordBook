def durchschnittliche_zeilenlaenge(text):
    # Teile den Text in Zeilen auf
    zeilen = text.split('\n')
    
    # Initialisiere eine Variable für die Summe der Zeilenlängen
    gesamtlaenge = 0
    
    # Durchlaufe die Zeilen und summiere die Längen
    for zeile in zeilen:
        gesamtlaenge += len(zeile)
    
    # Berechne den Durchschnitt der Zeilenlängen
    durchschnitt = gesamtlaenge / len(zeilen)
    
    return durchschnitt

text = """Es war schon dunkel
Als ich durch Vorstadtstraßen heimwärts ging
Da war ein Wirtshaus
Aus dem das Licht noch auf den Gehsteig schien
Ich hatte Zeit und mir war kalt, drum trat ich ein
Da saßen Männer mit braunen
Augen und mit schwarzem Haar
Und aus der Jukebox erklang Musik
Die fremd und südlich war
Als man mich sah
Stand einer auf und lud mich ein
Griechischer Wein ist
So wie das Blut der Erde
Komm', schenk dir ein
Und wenn ich dann traurig werde
Liegt es daran
Dass ich immer träume von daheim
Du musst verzeihen
Griechischer Wein
Und die altvertrauten Lieder
Schenk' nochmal ein
Denn ich fühl' die Sehnsucht
Wieder, in dieser Stadt
Werd' ich immer nur ein Fremder sein, und allein
Und dann erzählten sie mir von grünen Hügeln, Meer und Wind
Von alten Häusern und jungen Frauen, die alleine sind
Und von dem Kind das seinen Vater noch nie sah
Sie sagten sich immer wieder
Irgendwann geht es zurück
Und das Ersparte genügt zu
Hause für ein kleines Glück
Und bald denkt keiner mehr daran
Wie es hier war
Griechischer Wein ist
So wie das Blut der Erde
Komm', schenk dir ein
Und wenn ich dann traurig werde
Liegt es daran
Dass ich immer träume von daheim
Du musst verzeihen
Griechischer Wein
Und die altvertrauten Lieder
Schenk' nochmal ein,
Denn ich fühl' die Sehnsucht
Wieder, in dieser Stadt
Werd' ich immer nur ein Fremder sein, und allein"""

durchschnitt = durchschnittliche_zeilenlaenge(text)
print(f'Durchschnittliche Zeilenlänge: {durchschnitt}')
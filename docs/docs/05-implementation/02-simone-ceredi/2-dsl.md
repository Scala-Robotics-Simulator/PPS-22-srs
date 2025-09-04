---
sidebar_position: 2
---

# Implementazione dei DSL

Ogni entità configurabile dall'utente attraverso la _view_ o il _file_ di configurazione può essere creata e gestita utilizzando un **Domain Specific Language** (DSL) che semplifica la definizione delle proprietà dell'entità e delle relazioni tra di esse.

Oltre all'[ambiente](./1-environment.md), sono stati creati DSL per la definizione di:

- robot;
- ostacoli;
- luci;
- sensori;
- attuatori.

Tutti i **DSL** hanno caratteristiche simili, mimano la sintassi del linguaggio naturale per rendere la definizione delle entità più intuitiva e leggibile.
In aggiunta, sono stati implementati metodi dedicati ad una più semplice gestione della configurazione.

I **DSL** riguardanti le entità dispongono inoltre di un metodo `validate` che consente di verificare la correttezza dell'entità configurata, assicurando che tutte le proprietà richieste siano presenti e che i valori forniti siano validi.

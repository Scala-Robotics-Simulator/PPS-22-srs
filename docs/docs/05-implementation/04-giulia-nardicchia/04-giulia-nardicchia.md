---
sidebar_position: 4
---

# Giulia Nardicchia

L’implementazione realizzata si concentra principalmente sulle seguenti funzionalità:

- [Model-View-Controller (MVC) con Cake Pattern](mvc-implementation.md)
  - Model
    - Stato della simulazione
    - Logica di aggiornamento dello stato della simulazione (evoluzione temporale, gestione dei comandi di
      controllo, velocità e collisioni)
  - Controller
    - Gestione del ciclo di simulazione
    - Gestione degli eventi
  - View
    - Interfaccia comune
- Entità dinamiche (riguardanti principalmente gli attuatori)
    - Robot
- [Attuatori](./actuators.md)
    - Motori differenziali (con velocità indipendenti per ruota sinistra e destra)
    - Cinematica differenziale per il calcolo della nuova posizione e orientamento
- [Azioni e Algebra delle azioni](./action.md)
    - Movimento delle ruote dei motori differenziali delle entità dinamiche
    - Azioni di movimento predefinite: avanti, indietro, rotazione e stop
    - Azioni di movimento custom con validazione dei parametri
- [Generatore di numeri casuali](../../04-detailed-design/10-random-number-generator.md)
- [DSL per la creazione di ambienti grid-based per la simulazione](./dsl-environment-grid-based.md)
- [Command Line Interface (CLI)](./cli.md)
    - Parsing degli argomenti da linea di comando
    - View della CLI di configurazione e simulazione

Patterns e concetti funzionali utilizzati:
- Pattern **MVC** con **Cake Pattern** per separare chiaramente le responsabilità tra `Model`, `View` e `Controller`;
- Uso del pattern **Tagless Final** per definire azioni in modo astratto e composabile;
- Uso di `given` e `using` per l’iniezione delle dipendenze e la gestione implicita dei contesti;
- Uso delle monadi per la gestione degli effetti collaterali, degli errori e delle operazioni asincrone;
- Uso di funzioni di ordine superiore per creare funzioni più generiche e riutilizzabili;
- Uso di tipi algebrici (ADT) per modellare in modo chiaro e sicuro i dati e le operazioni;
- Uso di extension methods per estendere le funzionalità delle classi esistenti senza modificarle direttamente;
- Uso di operatori overloading per rendere il codice più leggibile e conciso;
- Uso di pattern matching per gestire in modo elegante i casi specifici dei dati;
- Uso di collezioni immutabili per garantire la sicurezza e la prevedibilità del codice.
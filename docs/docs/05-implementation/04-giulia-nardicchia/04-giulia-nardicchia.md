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
  - View della CLI di configurazione e simulazione.

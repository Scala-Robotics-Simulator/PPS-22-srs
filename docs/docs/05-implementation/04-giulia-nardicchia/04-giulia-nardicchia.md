---
sidebar_position: 4
---

# Giulia Nardicchia

L’implementazione realizzata si concentra principalmente sulle seguenti funzionalità:

- Architettura MVC: Model
    - Stato della simulazione
    - Logica di aggiornamento dello stato della simulazione (evoluzione temporale, gestione dei comandi di
      controllo, velocità e collisioni)
- Architettura MVC: Controller
    - Gestione del ciclo di simulazione
    - Gestione degli eventi
- Architettura MVC: View
    - Interfaccia comune
- Entità dinamiche
    - Robot
- Attuatori
    - Motori differenziali (con velocità indipendenti per ruota sinistra e destra)
- Algebra delle azioni
    - Movimento delle ruote dei motori differenziali delle entità dinamiche
    - Azioni di movimento predefinite: avanti, indietro, rotazione e stop
    - Azioni di movimento custom con validazione dei parametri
- Generatore di numeri casuali
- DSL
    - Creazione di ambienti grid-based per la simulazione
- Command Line Interface (CLI)
    - Parsing degli argomenti da linea di comando
    - View della CLI di simulazione e configurazione
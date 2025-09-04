---
sidebar_position: 2
---

# Entry Point

## Avvio dell’applicazione

L’applicazione si avvia dal punto di ingresso `Main`, che interpreta gli argomenti da linea di comando e determina la
modalità di esecuzione:

- **CLI**: testuale senza interfaccia grafica;
- **GUI**: con interfaccia grafica.

In base alla scelta, il `Main` inizializza la configurazione della simulazione, e delega l'avvio al `Launcher` fornendo
la modalità selezionata e la configurazione.

Il `Launcher` rappresenta il ponte tra il `Main` e l’architettura MVC:

- fornisce la vista corrispondente alla modalità selezionata;
- collega `Model`, `View` e `Controller`;
- crea lo stato iniziale della simulazione;
- avvia la simulazione passando al `Controller` lo stato iniziale.

In questo modo, il `Main` si limita a selezionare e configurare, mentre il `Launcher` predispone i componenti
dell’architettura e ne avvia l’esecuzione.

### Gestione degli argomenti

All’avvio, gli argomenti della riga di comando vengono analizzati per impostare i parametri della simulazione, tra cui:

- scelta della modalità (CLI o GUI);
- percorso di un file di configurazione;
- durata complessiva della simulazione;
- seed per la riproducibilità;
- informazioni di help.

:::info

I dettagli di implementazione della modalità CLI e degli argomenti della linea di comando sono descritti
in [Command Line Interface](../05-implementation/04-giulia-nardicchia/cli.md).

:::

## Configuration View

L’interfaccia `ConfigurationView` definisce il comportamento comune delle viste di configurazione:

![Configuration View UML](../../static/img/04-detailed-design/configuration-view.png)

Chiamando il metodo `init()` si apre l'interfaccia di configurazione e restituisce la configurazione scelta dall'
utente. L’uso dell’effetto `IO` consente di gestire in modo sicuro le operazioni di I/O, garantendo che il risultato
sia una configurazione valida con cui avviare la simulazione.

Il metodo `close()` è separato da `init()`  per consentire a chi utilizza la vista di gestire la chiusura
dell’interfaccia indipendentemente dall’inizializzazione.

:::info

Per i dettagli di implementazione vedere
sezioni [ConfigurationCLI](../05-implementation/04-giulia-nardicchia/cli.md#configurationcli)
e [ConfigurationGUI](../05-implementation/02-simone-ceredi/5-config-gui.md).

:::

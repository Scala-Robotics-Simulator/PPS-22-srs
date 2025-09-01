---
sidebar_position: 2
---

# Entry Point

## Avvio dell’applicazione

L’applicazione si avvia dal punto di ingresso `main`, che interpreta gli argomenti da linea di comando e determina la
modalità di esecuzione:

- **CLI**: modalità testuale senza interfaccia grafica;
- **GUI**: modalità con interfaccia grafica.

In base alla scelta, il `main` inizializza la configurazione della simulazione, costruisce lo stato iniziale e
seleziona il `launcher` appropriato (CLI o GUI) a cui delegare l’avvio del sistema.

Il `launcher` rappresenta il ponte tra il `main` e l’architettura MVC:

- fornisce la vista corrispondente alla modalità selezionata;
- collega `Model`, `View` e `Controller`;
- avvia la simulazione passando al `Controller` lo stato iniziale.

In questo modo, il `main` si limita a scegliere e configurare, mentre il `launcher` si occupa di predisporre i componenti
dell’architettura e avviarne l’esecuzione.

### Gestione degli argomenti

All’avvio, gli argomenti della linea di comando vengono analizzati per determinare i parametri di configurazione della
simulazione.
Questi includono, ad esempio:

- scelta della modalità (CLI o GUI);
- percorso di un file di configurazione;
- durata complessiva della simulazione;
- seed per la riproducibilità;
- informazioni di help o di versione.

:::info

I dettagli di implementazione della modalità CLI e degli argomenti della linea di comando sono descritti nella sezione [Command Line Interface](../05-implementation/04-giulia-nardicchia/cli.md).

:::

## Configuration View

L’interfaccia `ConfigurationView` definisce il comportamento comune delle viste di configurazione:

```scala
trait ConfigurationView:

  def init(): IO[SimulationConfig]

  def close(): IO[Unit]
```

Chiamando il metodo `init()` si apre l'interfaccia di configurazione e si restituisce la configurazione scelta dall'
utente.
Questo processo viene facilitato dall'utilizzo dell'effetto `IO`, in quanto consente di gestire in modo semplice e
sicuro le operazioni di input/output, garantendo la corretta esecuzione delle azioni richieste dall'utente.

### ConfigurationGUI

Eseguendo il simulatore senza indicare `--headless`, viene mostrata inizialmente l'interfaccia grafica di configurazione.

Il simulatore può essere configurato in due modalità, tramite file `YAML`, come descritto nella sezione
[Configuration](08-configuration.md), oppure tramite interfaccia grafica.

La **configuration gui** è l’interfaccia grafica che permette di modificare i parametri del simulatore in modo
interattivo, senza dover modificare manualmente il file `YAML`.

Tramite l'interfaccia grafica, è possibile:

- Visualizzare i parametri correnti del simulatore;
- Modificare i valori dei parametri;
- Salvare la configurazione in un file `YAML`;
- Caricare una configurazione da un file `YAML`;
- Caricare una delle configurazioni predefinite.

#### Componenti

Ogni elemento dell'interfaccia è gestito da una componente, ovvero un `JPanel`, per permettere maggiore modularità e
riutilizzo del codice.
I componenti utilizzati sono i seguenti:

- `ConfigurationControlsPanel`: gestisce i controlli per salvare e caricare le configurazioni personalizzate e quelle 
predefinite.
- `SimulationSettingsPanel`: gestisce i parametri specifici della simulazione, come la durata e il seed.
- `EnvironmentSettingsPanel`: gestisce i parametri dell'ambiente, come la dimensione della mappa.
- `EntitiesPanel`: gestisce le entità presenti nella simulazione, ovvero i robot, le luci e gli ostacoli.
- `SimulationCanvas`: per ottenere una preview dell'ambiente di simulazione.

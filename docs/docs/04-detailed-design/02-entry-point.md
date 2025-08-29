---
sidebar_position: 2
---

# Entry Point

## Main

L’applicazione si avvia dal metodo `main`, che rappresenta il punto d’ingresso del programma.
Gli argomenti della riga di comando vengono interpretati da `ArgParser`, in modo da determinare la modalità di
esecuzione:

- **CLI** (`CLILauncher`) se è attivata l’opzione `--headless`;
- **GUI** (`GUILauncher`) altrimenti.

Una volta scelto il launcher, viene inizializzata la vista corrispondente (`ConfigurationCLI` o `ConfigurationGUI`),
costruito lo stato iniziale della simulazione con `mkInitialState`, e avviata l’architettura MVC attraverso `runMVC`.

## Launcher

I due launcher, `CLILauncher` e `GUILauncher`, condividono la stessa struttura di base definita da `BaseLauncher`.
Questo trait integra i tre moduli fondamentali — `Model`, `View` e `Controller` — e fornisce il meccanismo per avviare
la
simulazione.

Il ciclo principale è gestito dal `Controller`, richiamato dal metodo `controller.start(state)`, mentre il launcher si
limita a collegare i componenti dell’architettura MVC e ad avviarne l’esecuzione nella modalità scelta (CLI o GUI).

## ArgParser e AppArgs

`ArgParser` gestisce l’analisi degli argomenti passati da linea di comando all'avvio dell’applicazione.
Viene utilizzata la libreria [**scopt**](https://github.com/scopt/scopt) per definire le opzioni disponibili.

Scopt fornisce una DSL dichiarativa che permette di definire con semplicità:

- opzioni obbligatorie o facoltative, con relativi tipi;
- valori di default;
- messaggi di aiuto e documentazione;
- versioning e validazione degli argomenti.

In questa applicazione, le principali opzioni supportate sono:

- `--headless`: avvia la simulazione in modalità CLI senza interfaccia grafica;
- `--path <file>`: specifica il percorso del file di configurazione YAML;
- `--duration <milliseconds>`: imposta la durata totale della simulazione;
- `--seed <number>`: definisce il seme casuale per garantire riproducibilità;
- `--help`: mostra le istruzioni disponibili;
- `--version`: mostra la versione dell’applicazione.

Il risultato del parsing è la struttura `AppArgs` che raccoglie in modo tipizzato tutti i parametri forniti dall’utente.
Se la lettura degli argomenti fallisce, il metodo `parse(args: Seq[String])` restituisce `None`, stampando un messaggio
di errore.

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

### ConfigurationCLI

La versione CLI implementa `ConfigurationView` tramite la console.
Permette di leggere la configurazione senza interfaccia grafica e restituisce i valori scelti dall’utente.
Se alcuni parametri non sono specificati, `ConfigurationCLI` chiede all’utente di inserirli interattivamente tramite
console (`askSimulationTime` e `askSeed`).
La configurazione viene validata, se il file di configurazione non esiste o non può essere letto, viene generato un
errore.
Se la validazione dell’ambiente fallisce, viene segnalato un errore con il messaggio specifico.

### ConfigurationGUI

Eseguendo il simulatore senza indicare `--headless`, viene mostrata inizialmente l'interfaccia grafica di configurazione.

Il simulatore può essere configurato in due modalità, tramite file `YAML`, come descritto nella sezione
[Configuration](06-configuration.md), oppure tramite interfaccia grafica.

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

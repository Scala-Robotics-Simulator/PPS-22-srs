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

### ConfigurationCLI

### ConfigurationGUI
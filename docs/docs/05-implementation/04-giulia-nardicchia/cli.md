# Command Line Interface

La modalità CLI permette di avviare la simulazione senza interfaccia grafica, interagendo esclusivamente tramite la
console. Tutti i parametri necessari per l’esecuzione della simulazione possono essere forniti da linea di comando o
inseriti interattivamente dall’utente.

In fase di avvio, il launcher selezionato determina le componenti della simulazione in base alla modalità scelta.
In base al flag `--headless`, il launcher seleziona i componenti appropriati: in modalità CLI vengono utilizzati
`ConfigurationCLI`, per la lettura dei parametri iniziali e la validazione dei parametri di configurazione, e
`CLIComponent`, che gestisce la vista testuale della simulazione.
Questa distinzione mantiene invariata la logica della simulazione rispetto alla modalità GUI, differenziando solo i
meccanismi di input/output e di rendering dello stato.

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

:::tip Esempio di avvio

Esempio di avvio del simulatore in modalità CLI con parametri specifici:

```bash
  java -jar PPS-22-srs.jar --headless --path config.yaml --duration 60000 --seed 42
```
:::

## ConfigurationCLI

`ConfigurationCLI` implementa `ConfigurationView` per la modalità CLI.
Il suo compito principale è leggere e validare la configurazione iniziale della simulazione, fornendo un oggetto
`SimulationConfig[ValidEnvironment]` pronto per il modello.

I valori mancanti, come durata della simulazione o il seed, vengono richiesti interattivamente all’utente tramite
console (`askSimulationTime` e `askSeed`).

La lettura del file `YAML` di configurazione è affidata a `YamlConfigManager`, con gestione degli errori per file
inesistenti o non validi.

Anche la validazione dell’ambiente avviene prima dell’avvio della simulazione, generando un’eccezione in caso di errori.

## CLIComponent

Il componente `CLIComponent` rappresenta la vista testuale della simulazione nell’architettura MVC.
La classe interna `CLIViewImpl` implementa i metodi principali di `View`:

- `init(queue: Queue[IO, Event])`: stampa un messaggio di benvenuto;
- `render(state: S)`: invocato a ogni aggiornamento dello stato, ma lasciato volutamente vuoto per evitare output
  continuo
  in console;
- `close()`: stampa un messaggio di chiusura al termine della simulazione;
- `timeElapsed(state: S)`: mostra il risultato finale stampando l’ambiente in formato tabellare, tramite `prettyPrint`.

In questo modo, `CLIComponent` fornisce un’interfaccia minimale ma sufficiente per monitorare la simulazione,
concentrandosi sui messaggi chiave e sull’output finale, senza appesantire l’esecuzione con rendering continui.
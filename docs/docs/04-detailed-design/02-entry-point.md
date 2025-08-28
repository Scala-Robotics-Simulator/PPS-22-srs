---
sidebar_position: 2
---

# Entry Point

## Main

L’applicazione è avviata dal metodo `main`. Questo metodo funge da entry point, leggendo gli argomenti della riga di
comando tramite `ArgParser` e selezionando il launcher appropriato:

- `CLILauncher` per la modalità a linea di comando (`--headless`);
- `GUILauncher` per la modalità grafica.

Il `Launcher` scelto inizializza la view corrispondente (`ConfigurationCLI` o `ConfigurationGUI`), crea lo stato
iniziale della simulazione tramite `mkInitialState`, chiude la configurazione e avvia l’architettura MVC chiamando
`runMVC`.

## Launcher

Sia `CLILauncher` che `GUILauncher` estendono `BaseLauncher`, che combina i moduli `Model`, `View` e `Controller`,
costruendo il cuore della simulazione e gestendo il ciclo principale di esecuzione.

## ArgParser e AppArgs

`ArgParser` gestisce l’analisi degli argomenti passati da linea di comando all'avvio dell’applicazione. 
Utilizza la libreria **scopt** per definire le opzioni disponibili e generare automaticamente messaggi di help e versione.

Le principali opzioni supportate includono:

- `--headless`: avvia la simulazione in modalità CLI senza interfaccia grafica;
- `--path <file>`: specifica il percorso del file di configurazione YAML;
- `--duration <milliseconds>`: imposta la durata totale della simulazione;
- `--seed <number>`: definisce il seme casuale per garantire riproducibilità;
- `--help`: mostra le istruzioni disponibili;
- `--version`: mostra la versione dell’applicazione.

Il metodo `parse(args: Seq[String])` restituisce un oggetto `AppArgs` con i valori forniti dall’utente, oppure `None` se la lettura degli argomenti fallisce.
---
sidebar_position: 3
---

# MVC e Simulation Core

## ModelModule

### Definizione dello stato

Il trait `State` definisce la struttura dello stato della simulazione, che include i seguenti campi:

- `simulationTime`: tempo totale previsto per la simulazione (opzionale per consentire simulazioni infinite);
- `elapsedTime`: tempo già trascorso dall’inizio della simulazione;
- `dt`: delta time della simulazione, cioè l’intervallo temporale usato per ogni step;
- `simulationSpeed`: velocità corrente della simulazione;
- `simulationRNG`: generatore di numeri casuali (`RNG`) usato per introdurre elementi stocastici nei comportamenti dei 
robot. Vedere la sezione [Generatore di numeri casuali](./09-random-number-generator.md) per maggiori dettagli;
- `simulationStatus`: stato corrente della simulazione;
- `environment`: rappresenta l’ambiente della simulazione, contenente le entità validate (`ValidEnvironment`). Vedere la sezione [Environment](./04-environment.md) per maggiori dettagli.

#### SimulationSpeed

`SimulationSpeed` è una enum che definisce le possibili velocità della simulazione:

- `SLOW`: velocità ridotta (200 ms per tick);
- `NORMAL`: velocità standard (100 ms per tick);
- `FAST`: velocità aumentata (10 ms per tick);
- `SUPERFAST`: velocità massima (0 ms per tick). Questa modalità viene utilizzata in esecuzione `headless` per far 
girare la simulazione il più rapidamente possibile.

#### SimulationStatus
`SimulationStatus` è una enum che rappresenta i possibili stati della simulazione:
- `RUNNING`: la simulazione è in esecuzione;
- `PAUSED`: la simulazione è in pausa;
- `STOPPED`: la simulazione è stata fermata manualmente;
- `ELAPSED_TIME`: la simulazione ha raggiunto il tempo massimo previsto.


### Logica di aggiornamento dello stato

Il trait `Model[S]` è parametrizzato sul tipo di stato `S`, che deve estendere `State`.
Esso definisce l’interfaccia per aggiornare lo stato della simulazione in modo funzionale e sicuro.

Il metodo `update(s: S)(using f: S => IO[S]): IO[S]` accetta:

- lo stato corrente `s`;
- una funzione di aggiornamento `f` (chiamata updateLogic), che trasforma lo stato in modo asincrono restituendo un
  nuovo stato incapsulato in `IO`.

L’uso della keyword `using` permette di passare la logica di aggiornamento implicitamente, in questo modo, il `Model`
non ha bisogno di sapere quali eventi o logiche hanno causato l’aggiornamento; si limita ad applicare la funzione
ricevuta.

Si noti che lo stato è immutabile: ogni aggiornamento produce una nuova istanza di `State`, mantenendo
l’integrità e la coerenza dei dati.

Il trait `Provider[S]` espone un’istanza concreta del `Model` agli altri moduli, permettendo l’iniezione delle
dipendenze secondo il **Cake Pattern**.

Il trait `Component[S]` fornisce l’implementazione concreta del `Model`.

`ModelImpl` implementa `update` semplicemente delegando l’aggiornamento alla funzione passata tramite `using`, rendendo l’applicazione della logica di trasformazione completamente modulare e riutilizzabile.

L’`Interface[S]` combina `Provider` e `Component`, fungendo da interfaccia unificata del modulo.

## ControllerModule

Il trait `Controller[S]` è parametrizzata sul tipo di stato `S`, che deve estendere `ModelModule.State`.
Esso espone due metodi:

- `start(initialState: S): IO[S]`: avvia la simulazione;
- `simulationLoop(s: S, queue: Queue[IO, Event]): IO[S]`: esegue il ciclo principale della simulazione.

Il `Controller` è responsabile dell’avvio della simulazione, della gestione del ciclo di esecuzione, del trattamento
degli eventi e della comunicazione tra il `Model` e la `View`.
L’implementazione segue un approccio modulare e funzionale, sfruttando `Cats Effect` per la gestione della concorrenza
ed effetti asincroni.

Il trait `Provider[S]` espone un’istanza concreta di `Controller[S]` e permette l’iniezione del controller nei moduli
che ne hanno bisogno.

Il trait `Component[S]` fornisce l’implementazione concreta del `Controller` e richiede i moduli `ModelModule.Provider`
e `ViewModule.Provider`.
Contiene l’oggetto `Controller`, che fornisce un’implementazione concreta dell'interfaccia Controller.

Il trait `Interface[S]` combina `Provider` e `Component`, fungendo da interfaccia unificata del modulo.

### Avvio della simulazione

Il metodo `start` inizializza la simulazione creando una coda di eventi e avviando il ciclo di simulazione:

- utilizza `Queue.unbounded[IO, Event]` per creare una coda di eventi asincrona e non bloccante;
- avvia la view chiamando `context.view.init`, che prepara l’interfaccia utente;
- esegue i comportamenti dei robot con `runBehavior`, che raccoglie in parallelo le proposte di azione dei robot;
- infine, avvia il ciclo principale chiamando `simulationLoop` passando lo stato iniziale e la coda degli eventi.

### Ciclo di simulazione

Il metodo `simulationLoop` implementa una funzione ricorsiva che:

- esegue i comportamenti dei robot se lo stato è `RUNNING`
- recupera ed elabora gli eventi dalla coda (`handleEvents`)
- aggiorna la vista con lo stato corrente (`context.view.render`)
- verifica la condizione di stop tramite `handleStopCondition`, che gestisce lo stato di terminazione della simulazione:
  - `STOPPED`: chiusura della view;
  - `ELAPSED_TIME`: passa alla view lo stato finale;
- se la simulazione non è terminata, esegue `nextStep` in base allo stato:
  - `RUNNING`: esegue `tickEvents`, calcolando il tempo trascorso e regolando il tick in modo preciso;
  - `PAUSED`: sospende il ciclo per un breve intervallo (`50 ms`);
  - altri stati: restituisce lo stato corrente senza modifiche;
- ripete ricorsivamente il loop.

### Gestione degli eventi

La gestione degli eventi è stata resa più modulare:

- `handleEvents`: processa una sequenza di eventi in ordine, applicando ciascun evento allo stato corrente;
- `handleEvent`: gestisce un singolo evento, aggiornando lo stato tramite le logiche definite nel `LogicsBundle`.

Gli eventi (`Event`) comprendono:

- `Tick`: avanzamento temporale della simulazione;
- `TickSpeed`: modifica della velocità dei tick;
- `Random`: aggiornamento del generatore casuale;
- `Pause` / `Resume` / `Stop`: controllo dello stato della simulazione;
- `RobotActionProposals`: gestione delle proposte di azione (`RobotProposal`) dei robot a ogni tick.

Ogni evento nel `Controller` viene trasformato in un aggiornamento dello stato tramite le logiche definite nel
`LogicsBundle`.
Il `LogicsBundle` viene passato implicitamente al controller come `given` e utilizzato con la keyword `using` quando il
`Controller` chiama il metodo `update` del model.

In questo modo:

- il `Controller` non modifica direttamente lo stato;
- ma delega tutte le trasformazioni al `Model`, specificando quale logica applicare (`tick`, `pause`, `stop`, `resume`,
  ecc.);
- il `Model` applica la logica appropriata e restituisce il nuovo stato aggiornato.

Questo consente al `Controller` di continuare il ciclo della simulazione con lo stato corretto, senza occuparsi
direttamente delle regole di aggiornamento o dei dettagli della business logic.

#### LogicsBundle

Il `LogicsBundle` raccoglie le funzioni che definiscono come lo stato della simulazione viene aggiornato in risposta a
diversi eventi.
Ogni funzione prende lo stato corrente e, se necessario, parametri aggiuntivi, restituendo un nuovo stato aggiornato.
Le funzioni incluse sono:

- `tick`: aggiorna lo stato della simulazione avanzando il tempo trascorso e, se necessario, modificando lo stato in
  base al tempo massimo raggiunto;
- `tickSpeed`: modifica la velocità della simulazione;
- `random`: aggiorna il generatore di numeri casuali nello stato;
- `pause`: mette la simulazione in pausa aggiornando lo stato;
- `resume`: riprende la simulazione aggiornando lo stato;
- `stop`: ferma la simulazione aggiornando lo stato;
- `robotActions`: gestisce le proposte di azione dei robot (`RobotProposal`) e aggiorna lo stato della simulazione
  (`SimulationState`) di conseguenza.

  Per ciascuna proposta:
  - viene applicata l’azione del robot all’ambiente usando un approccio di **ricerca binaria** per
    trovare la massima durata di movimento sicura, evitando collisioni con altri oggetti o robot;
  - i movimenti di tutti i robot vengono calcolati in parallelo usando `parTraverse`;
  - i robot aggiornati sostituiscono quelli originali nell’ambiente simulato, mantenendo la
    validità dell’ambiente tramite la funzione di `validate`;
  - se la validazione fallisce, lo stato dell’ambiente non viene modificato.

### Esecuzione dei comportamenti dei robot

Il metodo `runBehavior` seleziona tutte le entità di tipo `Robot` presenti nell’ambiente.

Per ciascun robot:

- legge i sensori (`senseAll`);
- costruisce un `BehaviorContext` e calcola l’azione da compiere tramite la logica del robot (`robot.behavior.run`);
- aggiorna il generatore casuale della simulazione (`Event.Random`);
- crea una proposta di azione (`RobotProposal`);
- alla fine, inserisce in coda un evento `RobotActionProposals` contenente tutte le proposte di azione raccolte.

Questo approccio permette di calcolare i comportamenti in parallelo, riducendo i tempi di elaborazione e mantenendo
l’aggiornamento dello stato coerente.

## ViewModule

Il trait `View[S]` definisce l’interfaccia della view, parametrizzata sul tipo di stato `S` che estende
`ModelModule.State`. La view espone quattro operazioni principali:

- `init(queue: Queue[IO, Event]): IO[Unit]`: inizializza la view e collega la coda degli eventi del controller, in modo
  che la view possa ricevere e reagire agli eventi;
- `render(state: S): IO[Unit]`: aggiorna la visualizzazione in base allo stato corrente della simulazione, mostrando i
  cambiamenti dell'ambiente e delle entità;
- `close(): IO[Unit]`: chiude la view;
- `timeElapsed(state: S): IO[Unit]`: gestisce le azioni da eseguire quando il tempo massimo della simulazione è
  raggiunto.

Queste operazioni sono tutte implementate come effetti `IO`, consentendo di gestire in modo sicuro e non bloccante
l’aggiornamento della `UI` e la sincronizzazione con il ciclo di simulazione.

Il trait `Provider[S]` espone un’istanza concreta della `View` agli altri moduli, permettendo l’iniezione delle
dipendenze secondo il **Cake Pattern**.

Il trait `Component[S]` definisce l’implementazione concreta della view tramite il metodo `makeView()`, che viene
richiamato
dall’object `View` per creare nuove istanze.

Il trait `Interface[S]` combina `Provider` e `Component`, fornendo un’interfaccia unica per l’uso del modulo `View`
all’interno
della simulazione.

### CLIComponent

Il trait `CLIComponent[S]` estende `ViewModule.Component[S]` e fornisce un’implementazione concreta della view
utilizzando un’interfaccia a linea di comando (CLI).

Per scelta progettuale, il metodo `render` non stampa lo stato della simulazione ad ogni step. La visualizzazione avviene
invece solo quando il tempo massimo della simulazione è raggiunto, tramite il metodo `timeElapsed`.

In questa fase finale, la view mostra in console l’ultimo stato della simulazione, rappresentando l’ambiente in modo
testuale con le entità e le loro posizioni in un formato semplificato.


:::info Esempio di ambiente testuale
La simulazione mostra le entità presenti in una griglia testuale.

Ogni simbolo rappresenta un tipo di entità (robot: `R`, ostacolo: `X`, luce: `**`, cella vuota: `--`).

```text
-- | -- | -- | -- | -- | -- | -- | -- | -- | -- ||
-- | -- | -- | R  | -- | -- | -- | -- | -- | -- ||
-- | -- | ** | -- | -- | -- | -- | -- | -- | -- ||
-- | -- | -- | -- | -- | -- | -- | -- | -- | -- ||
-- | -- | -- | -- | -- | -- | -- | -- | X  | -- ||
-- | -- | -- | -- | -- | X  | -- | -- | -- | -- ||
-- | R  | -- | -- | -- | -- | -- | -- | -- | -- ||
-- | -- | -- | -- | -- | -- | -- | -- | -- | -- ||
-- | -- | -- | -- | -- | -- | -- | -- | ** | -- ||
-- | -- | -- | -- | -- | -- | -- | -- | -- | --
```
:::

### GUIComponent

Il trait `GUIComponent[S]` estende `ViewModule.Component[S]` e fornisce un’implementazione concreta della view
utilizzando un’interfaccia grafica (GUI) basata su Swing.

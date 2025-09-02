---
sidebar_position: 3
---

# MVC e Simulation Core

## Model

![ModelModule](../../static/img/04-detailed-design/model-module.png)

### Definizione dello stato

![State](../../static/img/04-detailed-design/state.png)

Il trait `State` definisce la struttura dello stato della simulazione, che include i seguenti campi:

- `simulationTime`: durata totale prevista (opzionale, per supportare simulazioni infinite);
- `elapsedTime`: tempo trascorso dall’inizio della simulazione;
- `dt`: delta time della simulazione (intervallo temporale di ciascuno step);
- `simulationSpeed`: velocità corrente della simulazione;
- `simulationRNG`: generatore di numeri casuali (`RNG`) usato per introdurre elementi stocastici nei comportamenti dei
  robot;
- `simulationStatus`: stato corrente della simulazione;
- `environment`: rappresenta l’ambiente della simulazione, contenente le entità validate (di tipo `ValidEnvironment`).

:::info note
Vedere la sezione [Generatore di numeri casuali](10-random-number-generator.md), [Environment](./04-environment.md) per
maggiori dettagli.
:::

#### SimulationSpeed

`SimulationSpeed` è una enum che definisce le possibili velocità della simulazione:

- `SLOW`: velocità ridotta (200 ms per tick);
- `NORMAL`: velocità standard (100 ms per tick);
- `FAST`: velocità aumentata (10 ms per tick);
- `SUPERFAST`: velocità massima (0 ms per tick, usata in esecuzione `headless` per massimizzare la velocità).

#### SimulationStatus

`SimulationStatus` è una enum che rappresenta i possibili stati della simulazione:

- `RUNNING`: simulazione in esecuzione;
- `PAUSED`: simulazione in pausa;
- `STOPPED`: simulazione fermata manualmente;
- `ELAPSED_TIME`: raggiunto il tempo massimo previsto della simulazione.

### Logica di aggiornamento dello stato

Il trait `Model[S]` è parametrizzato sul tipo di stato `S`, che deve estendere `State`.
Esso definisce l’interfaccia per aggiornare lo stato della simulazione in modo funzionale e sicuro.

Il metodo `update(s: S)(using f: S => IO[S]): IO[S]` accetta:

- lo stato corrente `s`;
- una funzione di aggiornamento `f` denominata updateLogic, che produce (in modo asincrono( un nuovo stato incapsulato
  in `IO`.

Grazie al parametro di contesto `using`, la logica di aggiornamento viene passata implicitamente: il `Model` non deve
conoscere quali eventi o regole hanno causato l’aggiornamento; si limita ad applicare la funzione ricevuta.

> Si noti che lo stato è immutabile: ogni aggiornamento produce una nuova istanza di `State`, mantenendo
> l’integrità e la coerenza dei dati.

- `Provider[S]`: espone un’istanza concreta di `Model`, agli altri moduli, permettendo l’iniezione delle
  dipendenze secondo il **Cake Pattern**;
- `Component[S]`: fornisce l’implementazione concreta del `Model`;
- `ModelImpl`: implementa `update` delegando l’aggiornamento alla funzione passata tramite `using`, rendendo
  l’applicazione della logica di trasformazione completamente modulare e riutilizzabile;
- `Interface[S]`: combina `Provider` e `Component` come interfaccia unificata del modulo.

## Controller

![ControllerModule](../../static/img/04-detailed-design/controller-module.png)

Il trait `Controller[S]` è parametrizzata sul tipo di stato `S`, che estende `ModelModule.State`.
Esso espone due metodi:

- `start(initialState: S): IO[S]`: avvia la simulazione;
- `simulationLoop(s: S, queue: Queue[IO, Event]): IO[S]`: esegue il ciclo principale della simulazione.

Il `Controller` è responsabile dell’avvio della simulazione, della gestione del ciclo di esecuzione, del trattamento
degli eventi e della comunicazione tra il `Model` e la `View`.
L’implementazione segue un approccio modulare e funzionale, sfruttando **Cats Effect** per la gestione della concorrenza
ed effetti asincroni.

- `Provider[S]`: espone un’istanza concreta di `Controller[S]`, permettendo  l’iniezione del controller nei moduli
  che ne hanno bisogno.
- `Component[S]`: implementazione concreta del `Controller` (richiede `ModelModule.Provider` e `ViewModule.Provider`);
- `Interface[S]`: combina `Provider` e `Component`, operando da interfaccia unificata del modulo.

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
- se la simulazione non termina, esegue `nextStep` in base allo stato:
    - `RUNNING`: esegue `tickEvents`, calcolando il tempo trascorso e regolando il tick in modo preciso;
    - `PAUSED`: sospende il ciclo per un breve intervallo (`50 ms`);
    - altri stati: restituisce lo stato corrente senza modifiche;
- ripete ricorsivamente il loop.

### Gestione degli eventi

La gestione degli eventi è stata resa più modulare:

- `handleEvents`: processa una sequenza di eventi in ordine, applicando ciascun evento allo stato corrente;
- `handleEvent`: gestisce un singolo evento, aggiornando lo stato tramite le logiche definite nel `LogicsBundle`.

[Protocollo Event e Message RobotProposal](../../static/img/04-detailed-design/protocol-message.png)

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

![LogicsBundle UML](../../static/img/04-detailed-design/logic.png)

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
    - si applica l’azione del robot all’ambiente usando una  **ricerca binaria** per
      calcolare la massima durata di movimento sicura ( evitando collisioni con altri oggetti o robot);
    - i movimenti di tutti i robot vengono calcolati in parallelo usando `parTraverse`;
    - i robot aggiornati sostituiscono quelli originali nell’ambiente simulato, mantenendo la
      validità dell’ambiente tramite la funzione di `validate`;
    - se la validazione fallisce, lo stato dell’ambiente non viene modificato.

### Esecuzione dei comportamenti dei robot

Il metodo `runBehavior` seleziona tutte le entità di tipo `Robot` presenti nell’ambiente.

Per ciascun robot:

- legge i sensori (`senseAll`);
- costruisce un `BehaviorContext` (letture sensori + RNG) e calcola l’azione con `robot.behavior.run`;
- aggiorna il generatore casuale della simulazione (`Event.Random`) con quello restituito dal comportamento;
- crea una proposta di azione (`RobotProposal`);
- inserisce in coda un evento `RobotActionProposals` contenente tutte le proposte di azione raccolte.

Questo approccio permette di calcolare i comportamenti in parallelo, riducendo i tempi di elaborazione e mantenendo
l’aggiornamento dello stato coerente.
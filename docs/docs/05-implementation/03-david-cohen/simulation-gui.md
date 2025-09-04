---
sidebar_position: 4
---

# SimulationView

La `SimulationView` è l'interfaccia grafica (GUI) della simulazione, implementata in **Java Swing**. Le sue
responsabilità principali sono il **rendering** dello stato dell'ambiente, la gestione dell'**interazione utente**
tramite controlli di esecuzione e la **visualizzazione di dati** specifici, come lo stato di un robot e il tempo di simulazione.

L'implementazione di riferimento si trova in `GUIComponent.scala`.

## Architettura e design pattern

### Componenti

L'interfaccia è strutturata tramite la composizione di pannelli, ognuno dedicato a una funzione specifica.

- **Layout Principale**: un `JFrame` contiene un `JSplitPane` che separa il `SimulationCanvas` (a sinistra) dai
  pannelli informativi e di controllo (a destra);
- **Pannelli Funzionali**:
  - `SimulationCanvas`: si occupa del rendering dell'ambiente;
  - `RobotPanel`: mostra la lista dei robot, gestisce la selezione e la visualizzazione dei dati del robot selezionato;
  - `TimePanel`: mostra il tempo di simulazione;
  - `ControlsPanel`: raggruppa i controlli di interazione (start/stop, pausa, velocità).

### Stato centralizzato

Per evitare l’accoppiamento diretto tra i pannelli e garantire coerenza dei dati, la vista adotta un _stato
centralizzato_.
Un singolo oggetto `SimulationViewState`, gestito in modo thread-safe tramite un `AtomicReference`, contiene i dati
necessari alla UI.

```scala
// Lo stato centralizzato contiene tutti i dati necessari per il rendering
final case class SimulationViewState
(
  environment: Option[Environment] = None,
  selectedRobotId: Option[String] = None,
  robots: List[Robot] = Nil,
  staticLayer: Option[BufferedImage] = None,
  // ... altri campi per la gestione della cache
)
```

> Nota: il flusso è _unidirezionale_: il controller aggiorna lo stato via `render`, i componenti leggono e si
> aggiornano.

## Ciclo di vita e rendering

- **`init(queue)`**: costruisce la UI, registra gli handler e mostra la finestra.
- **`render(state)`**: a ogni tick aggiorna `SimulationViewState` e programma il ridisegno sull’EDT.
- **`timeElapsed(state)`**: disabilita i controlli al termine della simulazione.
- **`close()`**: rilascia le risorse della finestra Swing.

### Ottimizzazione del rendering

Per garantire fluidità, `SimulationCanvas` usa una cache (`BufferedImage`). Gli elementi _statici_ (griglia, ostacoli,
luci) sono disegnati una sola volta; ad ogni tick si ridisegnano solo quelli _dinamici_ (robot e sensori). La cache si
rigenera solo quando cambiano dimensioni del canvas o dell’ambiente.

## Interazione e flusso degli eventi

### Controlli di esecuzione

- **Start/Stop**: invia `Event.Resume`/`Event.Stop`. Per prevenire interruzioni accidentali, _Stop_ prima mette in pausa
  (`Event.Pause`) e chiede conferma tramite un modale.
- **Pausa/Riprendi**: invia `Event.Pause` o `Event.Resume`.
- **Velocità**: i radio button inviano `Event.TickSpeed` con la velocità selezionata.

### Selezione del robot e visualizzazione dati

Un robot può essere selezionato sia dalla lista nel `RobotPanel` sia cliccando direttamente sulla sua rappresentazione
nel `SimulationCanvas`. Una volta selezionato, il `RobotPanel` mostra le sue informazioni dettagliate, comprese le
letture dei sensori in tempo reale, ottenute tramite la funzione `robot.senseAll(environment)`.

### Dettagli grafici

- **Entità**: gradienti e ombre per migliorare leggibilità.
- **Selezione**: il robot selezionato è evidenziato con bordo e colore distintivi.
- **Sensori**: i raggi di prossimità sono linee con lunghezza proporzionale alla distanza misurata.

:::info
Per un'overview delle funzionalità offerte dall'interfaccia grafica, si rimanda alla sezione [Simulation GUI](../../08-user-guide/02-simulation.md) della User Guide.
:::

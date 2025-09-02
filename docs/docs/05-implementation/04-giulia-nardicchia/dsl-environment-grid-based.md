# DSL per la creazione dell'ambiente grid-based

In questa pagina viene descritto il **Domain-Specific Language** (DSL) sviluppato in Scala per la definizione di
ambienti
bidimensionali a griglia, popolati da **entità statiche** (ostacoli, luci) e **dinamiche** (robot).
Il DSL fornisce un linguaggio dichiarativo e leggibile, che permette di costruire e visualizzare gli ambienti in maniera
compatta, senza dover gestire manualmente coordinate o parametri delle entità.

Nota: il DSL rappresenta un'approssimazione dell'ambiente reale; alcune funzionalità sono state semplificate e per altre
vengono utilizzati parametri di default.

Il modulo si compone di tre parti principali:

- `Cell`: rappresenta una singola cella della griglia e ne definisce il contenuto (vuota, ostacolo, luce, robot);
- `EnvironmentBuilder` e `GridDSL`: forniscono metodi per costruire l'ambiente come una griglia di celle, usando
  operatori infissi per separare le celle e le righe;
- `EnvironmentToGridDSL`: consente di convertire un ambiente esistente in una rappresentazione a griglia e di stamparlo.

## Cell

L'_enum_ `Cell` modella i possibili contenuti di una cella della griglia:

- `Empty`: cella vuota;
- `Obstacle`: cella contenente un ostacolo;
- `Light`: cella contenente una fonte di luce;
- `Robot(policy: Policy)`: cella occupata da un robot dotato di una determinata politica comportamentale (
  `AlwaysForward`,
  `RandomWalk`, `ObstacleAvoidance`, ecc.).

Ogni cella può essere convertita in una entità concreta (Entity) posizionata nello spazio bidimensionale tramite il
metodo
`toEntity`. Questo passaggio traduce la rappresentazione astratta in oggetti del modello della simulazione.

Il _companion object_ di `Cell` fornisce operatori simbolici che rendono più leggibile la definizione di un ambiente:

- `--`: cella vuota;
- `X`: ostacolo;
- `**`: luce;
- `A`, `R`, `O`, `P`, `M`: robot con diverse politiche di movimento.

Il metodo `symbolFor` serve invece per convertire una politica (`Policy`) nel corrispondente simbolo testuale, utile
nella fase di stampa.

## EnvironmentBuilder e GridDSL

La _case class_ `EnvironmentBuilder` rappresenta una griglia di celle e permette di costruire un oggetto `Environment`
completo tramite il metodo `build()`.
La costruzione segue due passaggi:

1. ogni cella viene convertita in entità con `toEntity`;
2. le entità sono aggregate per ottenere un `Environment` con le dimensioni coerenti (larghezza, altezza)
   e contenente tutti gli oggetti.

L'_object_ `GridDSL` fornisce un'interfaccia per definire la griglia in modo dichiarativo, usando operatori infissi:

- `|`: separa le celle all'interno di una riga;
- `||`: separa le righe della griglia.

Il codice sfrutta le _extension methods_ per estendere la funzionalità delle classi `Cell` e `EnvironmentBuilder` senza
modificarle direttamente.

### Inizio di una nuova riga con due celle

```scala
extension (first: Cell)

  infix def |(next: Cell): EnvironmentBuilder =
    EnvironmentBuilder(Vector(Vector(first, next)))
```

L’operatore `|` estende `Cell` e permette di iniziare una nuova riga della griglia a partire da due celle (`first` e
`next`).
Restituisce un `EnvironmentBuilder` contenente una riga iniziale con le due celle specificate.

### Aggiunta di celle a una riga esistente

```scala
extension (env: EnvironmentBuilder)

  infix def |(next: Cell): EnvironmentBuilder =
    EnvironmentBuilder(
      env.cells.dropRight(1) :+ (env.cells.lastOption.getOrElse(Vector.empty: Vector[Cell]) :+ next)
    )
```

L’operatore `|` che estende `EnvironmentBuilder` consente di aggiungere una cella alla riga corrente:

- `env.cells` contiene tutte le righe definite fino a quel momento;
- `dropRight(1)` rimuove l’ultima riga, che verrà aggiornata;
- `lastOption.getOrElse(Vector.empty: Vector[Cell])` recupera l’ultima riga, o una riga vuota se non esiste;
- `:+ next` aggiunge la nuova cella `next` alla fine dell’ultima riga;

### Inizio di una nuova riga

```scala
extension (env: EnvironmentBuilder)

  infix def ||(next: Cell): EnvironmentBuilder =
    EnvironmentBuilder(env.cells :+ Vector(next))
```

L’operatore `||` serve per iniziare una nuova riga nella griglia.
Prende la griglia corrente (`env.cells`) e aggiunge una nuova riga contenente solo la cella `next`.

Tramite la conversione implicita, un `EnvironmentBuilder` può essere usato direttamente come `Environment`.

## EnvironmentToGridDSL

Questa componente si occupa di fare il processo inverso: ovvero, dato un `Environment` già popolato di entità, produce la griglia di `Cell` corrispondente.

Per ogni posizione `(x, y)` della griglia, si determina quale cella è presente:
- `Robot`: priorità massima;
- `Light`: priorità intermedia.
- `Obstacle`: priorità bassa;
- `Empty`: se non ci sono entità.

Si ricostruisce un `EnvironmentBuilder` a partire dall’ambiente reale.
Il metodo `prettyPrint` genera una stringa formattata che mostra la griglia.


:::tip Esempio di stampa della griglia

```text
  -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- ||
  -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- ||
  -- | -- | M  | -- | -- | X  | X  | -- | -- | -- | -- | -- ||
  -- | -- | -- | -- | -- | X  | X  | -- | -- | -- | -- | -- ||
  -- | -- | -- | -- | -- | X  | X  | -- | -- | -- | -- | -- ||
  -- | -- | -- | -- | -- | X  | X  | -- | -- | ** | -- | -- ||
  -- | -- | -- | -- | -- | X  | X  | -- | -- | -- | -- | -- ||
  -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- ||
  -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- ||
  -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | --
```
:::
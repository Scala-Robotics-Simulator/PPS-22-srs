---
sidebar_position: 3
---

# Behavior

Il modulo `behavior` implementa il sistema decisionale per le entità dinamiche, fornendo un framework puro e componibile
per la selezione delle azioni basata su input sensoriali.

## Tipi fondamentali

### BehaviorContext

Incapsula l'input immutabile per la decisione:

```scala
final case class BehaviorContext
(
  sensorReadings: SensorReadings,
  rng: RNG
)
```

### Kleisli Arrow

Il modulo utilizza `Kleisli` per modellare funzioni pure con effetti:

```scala
// in BehaviorTypes.scala
type Behavior[I, A] = Kleisli[Id, I, A] // I => A (totale)
type PartialBehavior[I, A] = Kleisli[Option, I, A] // I => Option[A] (parziale),
type Condition[I] = I => Boolean // Predicato

// in BehaviorCommon.scala
type Decision[F[_]] = Behavior[BehaviorContext, (Action[F], RNG)]
```

> Nota: le motivazioni sono descitte in [**Design → Behavior**](../../04-detailed-design/06-behavior.md#pattern-reader--kleisli-razionale).

Una decisione è una funzione totale che, dato un `BehaviorContext`, restituisce una tupla con l’`Action[F]` da eseguire
e il nuovo stato del generatore di numeri casuali `RNG`.

```scala
// Esempio di Composizione con Kleisli
val decision: Kleisli[Id, BehaviorContext, (Action[F], RNG)] =
  Kleisli.ask[Id, BehaviorContext].map { ctx =>
    // abbiamo a disposizione ctx.readings e ctx.rng
    // calcoliamo l'azione da restituire
    (moveForward[F], ctx.rng)
  }
```

:::info
Per ulteriori informazioni su `Action[F]` e `RNG`, vedere:

- [Azioni](../../04-detailed-design/07-action.md)
- [RNG](../../04-detailed-design/10-random-number-generator.md)

:::

## DSL di composizione

Il **DSL** fornisce combinatori per costruire regole parziali e comporle in behavior totali:

### Operatori principali

```scala
// Crea regola parziale da condizione
infix def ==>(cond: Condition[I], act: => A): PartialBehavior[I, A]

// Composizione left-biased
infix def |(r1: PartialBehavior[I, A], r2: PartialBehavior[I, A]): PartialBehavior[I, A]

// Chiusura in behavior totale
def default(fallback: => A): Behavior[I, A]

// Combinatori logici
def and(c1: Condition[I], c2: Condition[I]): Condition[I]
def or(c1: Condition[I], c2: Condition[I]): Condition[I]
def not(c: Condition[I]): Condition[I]
```

:::tip Esempio di creazione di un comportamento semplice

```scala
/** Definiamo un comportamento che:
 * - se un ostacolo è vicino (proximity < 0.3), gira a destra;
 * - se la luce è forte (light > 0.5), si muove verso;
 * - se l'energia è bassa (energy < 0.2), si ferma;
 * - altrimenti, avanza.
 */
val behavior = {
  ((proximity < 0.3) ==> turnRight) |
    ((light > 0.5) ==> moveToLight) |
    ((energy < 0.2) ==> stop)
      .default(moveForward)
}
```

:::

## Utility comuni (estratto)

Il modulo fornisce funzioni helper in `BehaviorCommon`:

```scala
// Clamping e normalizzazione
def clamp01(v: Double): Double
def clamp(v: Double, lo: Double, hi: Double): Double
def toSignedDegrees(deg: Double): Double // [-180, 180]
def normalize360(deg: Double): Double

// Azioni predefinite
def forward[F[_]]: Action[F]
def wheels[F[_] : Monad](l: Double, r: Double): Action[F]
def moveOrNo[F[_] : Monad](l: Double, r: Double): Action[F]
```

> Nota: a tutte le velocità viene fatto il clamp tra `MinSpeed` e `MaxSpeed`. I parametri/tuning vivono in
> `io.github.srs.utils.SimulationDefaults`.

## Implementazioni delle Policy

### AlwaysForward

Comportamento minimale deterministico: ritorna sempre `forward[F]` e l’`rng` intatto. Utile come baseline/fallback.

```scala
def decision[F[_]]: Decision[F] =
  Kleisli(ctx => (forward[F], ctx.rng))
```

Procedura:

1. ignora le letture sensoriali;
2. seleziona sempre l’azione “avanti” a velocità di crociera;
3. clamp delle velocità in `[MinSpeed, MaxSpeed]`;
4. RNG restituito **invariato**.

### RandomWalk

Esplorazione stocastica con bias in avanti e sterzata variabile.

```scala
def decision[F[_] : Monad]: Decision[F] = Kleisli { ctx =>
  val (uF, r1) = ctx.rng.generate(range)
  val (uT, r2) = r1.generate(range)
  val (uM, r3) = r2.generate(range)

  val base = calculateBaseSpeed(uF)
  .
..
  val turn = calculateTurn(uT, uM)
  val (left, right) = applyTurnToWheels(base, turn)

  (moveOrNo[F](left, right), r3)
}
```

Procedura:

1. estrae tre valori uniformi e indipendenti in `[MinSpeed, MaxSpeed]`:
   `uF` (base-speed), `uT` (turn-magnitude), `uM` (pivot-mix); RNG avanza `r → r1 → r2 → r3`.
2. calcola la base: $$MinForwardFactor + MaxForwardExtra * |uF|$$, poi clamp in `[MinSpeed, MaxSpeed]`;
3. deriva l’ampiezza di sterzo da `|uT|` con curva `TurnExponent`, scalata tra `MinTurnOfBase` e `MaxTurnOfBase` (in
   funzione della base);
4. applica un **pivot-boost** con bassa probabilità (`PivotBoostProb`);
5. converte in velocità differenziali (sx/dx), clamp in `[MinSpeed, MaxSpeed]`;
6. restituisce azione + **RNG successivo** a `r3`.

### ObstacleAvoidance

Sistema a tre fasi basato su distanze:

```scala
private enum Phase:
  case Free, Warn, Blocked

def decision[F[_] : Monad]: Decision[F] =
  Kleisli.ask[Id, BehaviorContext].map { ctx =>
    val readings = ctx.sensorReadings.proximityReadings

    val front = minIn(readings)(deg => math.abs(deg) <= 10)
    val frontLeft = minIn(readings)(deg => deg > 0.0 && deg <= 100.0)
    val frontRight = minIn(readings)(deg => deg < 0.0 && deg >= -100.0)

    val phase = pickPhase(frontLeft, frontRight)
    val sign = turnSign(readings)

    val (l, r) =
      if front < CriticalDist then (-1.0, -0.1)
      else
        phase match
          case Phase.Free => (CruiseSpeed, CruiseSpeed)
          case Phase.Warn =>
            if sign > 0.0 then (WarnSpeed - WarnTurnSpeed, WarnSpeed + WarnTurnSpeed)
            else (WarnSpeed + WarnTurnSpeed, WarnSpeed - WarnTurnSpeed)
          case Phase.Blocked =>
            if sign > 0.0 then (-BackBoost, BackBoost)
            else (BackBoost, -BackBoost)

    (wheels[F](l, r), ctx.rng)
  }
```

Procedura:

1. aggrega letture dei sensori di prossimità in zone frontali (front, front-left, front-right); valori normalizzati in `[0,1]`;
2. determina la **fase** tramite soglie: `SafeDist`, `CriticalDist` → `Free | Warn | Blocked`;
3. stima direzione di sterzo via medie emisferiche (sinistra vs destra) e segno conseguente;
4. imposta velocità:
   - `Free` → `CruiseSpeed, CruiseSpeed`;
   - `Warn` → `WarnSpeed ± WarnTurnSpeed` (sterza **lontano** dal lato più occupato);
   - `Blocked` → pivot/retromarcia `±BackBoost`.
5. clamp finale in `[MinSpeed, MaxSpeed]`;
6. RNG restituito **invariato**.

### Phototaxis

Orientamento verso sorgenti luminose:

```scala
def decision[F[_] : Monad]: Decision[F] =
  Kleisli.ask[Id, BehaviorContext].map { ctx =>
    val action =
      bestLight(ctx.sensorReadings.lightReadings) match
        case None => moveForward[F]
        case Some((s, off)) =>
          val (l, r) = wheelsTowards(s, off)
          moveOrNo[F](l, r)

    (action, ctx.rng)
  }
```

Procedura:

1. seleziona la luce “migliore” per intensità; tie-break su offset più frontale con tolleranza `Epsilon`;
2. se non è presente nessuna luce ricade in fallback **forward**; altrimenti continua;
3. la velocità di base è proporzionale all’intensità: $$MinForwardBias + (1 − MinForwardBias) * strength$$, clamp in `[MinSpeed, MaxSpeed]`;
4. lo sterzo dipende dall'errore angolare normalizzato ($$|offset|/180$$), guadagno `TurnGain`; segno verso la luce;
5. converte in velocità differenziali e clamp in `[MinSpeed, MaxSpeed]`;
6. RNG restituito **invariato**.

### Prioritized

Composizione gerarchica di comportamenti in modo dichiarativo. Non duplica logiche ma le **orchestra**.

```scala
// behaviors/PrioritizedBehavior.scala (cuore)
def decision[F[_] : Monad]: Decision[F] =
  val danger: Condition[BehaviorContext] =
    ctx => ctx.sensorReadings.proximityReadings.exists(_.value < Behaviors.Prioritized.DangerDist)

  val hasLight: Condition[BehaviorContext] =
    ctx => ctx.sensorReadings.lightReadings.exists(_.value >= Behaviors.Prioritized.LightThreshold)

  val chooser: Behavior[BehaviorContext, BehaviorContext => (Action[F], RNG)] =
    (
      (danger ==> ObstacleAvoidanceBehavior.decision[F].run) |
        (hasLight ==> PhototaxisBehavior.decision[F].run)
      ).default(RandomWalkBehavior.decision[F].run)

  Kleisli.ask[Id, BehaviorContext].flatMap { ctx =>
    Kleisli.liftF(chooser.run(ctx)(ctx))
  }
```

Procedura:

1. valuta condizioni globali:
   - `danger` se prossimità < `DangerDist`;
   - `hasLight` se luce ≥ `LightThreshold`.
2. seleziona **in ordine**: `ObstacleAvoidance` → `Phototaxis` → `RandomWalk` (fallback);
3. esegue la policy scelta sul `BehaviorContext` corrente;
4. restituisce l’azione (totalità garantita) + RNG della policy selezionata (immutato per Obstacle Avoidance/Phototaxis, avanzato per
   RandomWalk).

## Come estendere il sistema di behavior

Il sistema è progettato per facilitare l'aggiunta di nuovi comportamenti.

1. _nuove condizioni_: estendere i predicati per nuovi tipi di sensori.
2. _nuove azioni_: aggiungere azioni al catalogo disponibile.
3. _nuove policy_: comporre comportamenti esistenti o crearne di completamente nuovi.
4. _nuovi contesti_: se necessario, estendere `BehaviorContext` per informazioni aggiuntive (es. memoria).

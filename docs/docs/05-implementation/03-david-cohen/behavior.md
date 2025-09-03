---
sidebar_position: 1
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

L'utilizzo di `Kleisli` fornisce:

- Combinatori pronti (`map`, `flatMap`)
- Forma uniforme per il DSL (alias di tipo) su cui definire i combinatori del DSL (es. `|`, `default`).
- incapsulare la funzione in un **wrapper** con combinatori già pronti (es. `map`, `flatMap`).
- Accesso esplicito al contesto con `Kleisli { ctx => }` e `Kleisli.ask`

```scala
// Esempio di Composizione con Kleisli
val decision: Kleisli[Id, BehaviorContext, (Action[F], RNG)] =
  Kleisli.ask[Id, BehaviorContext].map { ctx =>
    // abbiamo a disposizione ctx.readings e ctx.rng
    // calcoliamo l'azione da restituire
    (moveForward[F], ctx.rng)
  }
```

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

### Esempio di uso

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

> A tutte le velocità viene fatto il clamp tra MinSpeed e MaxSpeed. I parametri/tuning vivono in
> io.github.srs.utils.SimulationDefaults.

## Implementazioni delle Policy

### AlwaysForward

Comportamento minimale deterministico: ritorna sempre `forward[F]` e l’`rng` intatto. Utile come baseline/fallback.

```scala
def decision[F[_]]: Decision[F] =
  Kleisli(ctx => (forward[F], ctx.rng))
```

Procedura:

1. Ignora le letture sensoriali.
2. Seleziona sempre l’azione “avanti” a velocità di crociera.
3. Clamp delle velocità in `[MinSpeed, MaxSpeed]`.
4. RNG restituito **invariato**.

### RandomWalk

Esplorazione stocastica con bias in avanti e sterzata variabile.

```scala
def decision[F[_] : Monad]: Decision[F] = Kleisli { ctx =>
  val (uF, r1) = ctx.rng.generate(range)
  val (uT, r2) = r1.generate(range)
  val (uM, r3) = r2.generate(range)

  val base = calculateBaseSpeed(uF)
  ...
  val turn = calculateTurn(uT, uM)
  val (left, right) = applyTurnToWheels(base, turn)

  (moveOrNo[F](left, right), r3)
}
```

Procedura:

1. Estrae tre valori uniformi e indipendenti in `[MinSpeed, MaxSpeed]`:
   `uF` (base-speed), `uT` (turn-magnitude), `uM` (pivot-mix); RNG avanza `r → r1 → r2 → r3`.
2. Calcola la base: `MinForwardFactor` + `MaxForwardExtra * |uF|`, poi clamp in `[MinSpeed, MaxSpeed]`.
3. Deriva l’ampiezza di sterzo da `|uT|` con curva `TurnExponent`, scalata tra `MinTurnOfBase` e `MaxTurnOfBase` (in
   funzione della base).
4. Applica un **pivot-boost** con bassa probabilità (`PivotBoostProb`), entità `PivotBoostAbs`.
5. Converte in velocità differenziali (sx/dx), clamp in `[MinSpeed, MaxSpeed]`.
6. Restituisce azione + **RNG avanzato** a `r3`.

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

1. Aggrega prossimità in zone frontali (front, front-left, front-right); valori normalizzati in `[0,1]`.
2. Determina la **fase** tramite soglie: `SafeDist`, `CriticalDist` → `Free | Warn | Blocked`.
3. Stima direzione di sterzo via medie emisferiche (sinistra vs destra) e segno conseguente.
4. Imposta velocità:
    * `Free` → `CruiseSpeed, CruiseSpeed`
    * `Warn` → `WarnSpeed ± WarnTurnSpeed` (sterza **lontano** dal lato più occupato)
    * `Blocked` → pivot/retromarcia `±BackBoost`.
5. Clamp finale in `[MinSpeed, MaxSpeed]`.
6. RNG restituito **invariato**.

### Phototaxis

Orientamento verso sorgenti luminose:

```scala
def decision[F[_] : Monad]: Decision[F] =
  Kleisli.ask[Id, BehaviorContext].map { ctx =>
    val action =
      bestLight(ctx.sensorReadings.lightReadings) match
        case None => MovementActionFactory.moveForward[F]
        case Some((s, off)) =>
          val (l, r) = wheelsTowards(s, off)
          moveOrNo[F](l, r)

    (action, ctx.rng)
  }
```

Procedura:

1. Seleziona la luce “migliore” per intensità; tie-break su offset più frontale con tolleranza `Epsilon`.
2. Se nessuna luce: fallback **forward**; altrimenti continua.
3. Base ∝ intensità: `MinForwardBias + (1 − MinForwardBias) * strength`, clamp in `[MinSpeed, MaxSpeed]`.
4. Sterzo ∝ errore angolare normalizzato (`|offset|/180`), guadagno `TurnGain`; segno verso la luce.
5. Converte in velocità differenziali e clamp in `[MinSpeed, MaxSpeed]`.
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

1. Valuta condizioni globali: `danger` se prossimità < `Behaviors.Prioritized.DangerDist`; `hasLight` se luce ≥
   `Behaviors.Prioritized.LightThreshold`.
2. Seleziona **in ordine**: `ObstacleAvoidance` → `Phototaxis` → `RandomWalk` (fallback).
3. Esegue la policy scelta sul `BehaviorContext` corrente.
4. Restituisce l’azione (totalità garantita) + RNG della policy selezionata (immutato per OA/Phototaxis, avanzato per
   RandomWalk).

## Estensione del modulo

Per aggiungere una nuova policy:

1. **Creare il behavior** in `behaviors/`:

```scala
object MyBehavior:
  def decision[F[_] : Monad]: Decision[F] = Kleisli { ctx =>
    // Logica decisionale
    (action, ctx.rng)
  }
```

2. **Registrare in Policy enum**:

```scala
enum Policy:
  case MyPolicy extends Policy("MyPolicy")

  def run[F[_] : Monad](input: BehaviorContext): (Action[F], RNG) =
    this match
      case MyPolicy => MyBehavior.decision.run(input)
```

[//]: # (## Limiti noti)

[//]: # ()

[//]: # (- **Stateless**: Le decisioni non mantengono memoria tra tick)

[//]: # (- **Myopic**: Non c'è pianificazione a lungo termine)

[//]: # (- **Locale**: Le euristiche non considerano lo stato globale)

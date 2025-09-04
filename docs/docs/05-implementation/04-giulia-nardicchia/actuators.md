# Actuators

In questa sezione sono descritti gli attuatori implementati nel progetto, che permettono di modificare lo stato delle
entità dinamiche, in particolare la posizione e l'orientazione in base alle velocità delle ruote.

L'interfaccia principale è `Actuator`, che definisce il metodo `act` per aggiornare lo stato di un'entità dinamica
in base al tempo trascorso e alle caratteristiche dell'attuatore:

```scala
trait Actuator[E <: DynamicEntity]:
  def act[F[_] : Monad](dt: FiniteDuration, entity: E): F[E]
```

Il metodo è parametrizzato su una monade `F[_]`, richiamando lo stile del **Tagless Final**, perché consente di astrarre
il contesto funzionale in cui l’attuatore opera.
Tuttavia, l’interfaccia non segue completamente il pattern **Tagless Final** “puro”, in quanto non incapsula tutte le
operazioni in un _trait_ parametrizzato su `F[_]`. Si può quindi considerare una versione ispirata al **Tagless Final**,
che combina flessibilità e semplicità, permettendo di definire diversi tipi di attuatori senza propagare `F[_]`
sull’intero
modello `Robot`.

È stato implementato solo un tipo di attuatore, `DifferentialWheelMotor`.
L'interfaccia `Actuator` è progettata per essere estesa facilmente con nuovi tipi di attuatori.

## DifferentialWheelMotor

`DifferentialWheelMotor` estende l'interfaccia `Actuator[Robot]` e rappresenta un motore differenziale con velocità
indipendenti per le ruote sinistra e destra. Le velocità sono espresse in unità al secondo (unit/s) e possono essere
positive (avanti), negative (indietro) o nulle (fermo). Il motore differenziale utilizza la cinematica differenziale per
calcolare la nuova posizione e orientamento del robot in base alle velocità delle ruote e al tempo trascorso.

Il motore differenziale utilizza l'_object_ `DifferentialKinematics` per eseguire i calcoli necessari.

## DifferentialKinematics

Nell'_object_ `DifferentialKinematics` sono presenti tre funzioni principali che servono a calcolare la cinematica
differenziale di un robot a due ruote.

Si trattano di **funzioni di ordine superiore** (_higher-order functions_) perché prendono parametri di configurazione e
restituiscono altre funzioni che eseguono i calcoli veri e propri.

Questo approccio permette di separare parametri:

- statici: configurazione del robot (es. distanza tra le ruote, orientamento iniziale, delta time);
- dinamici: valori che cambiano ad ogni tick della simulazione (es. velocità delle ruote, velocità lineare e
  angolare).

:::info
Le funzioni implementano le formule descritte nella
sezione [DifferentialKinematics](../../04-detailed-design/05-entity.md#attuatori-di-movimento)
per calcolare velocità, posizione e orientamento del robot.
:::

Le funzioni sono:

```scala
def computeWheelVelocities: DifferentialWheelMotor => (Double, Double)
def computeVelocities(wheelDistance: Double): ((Double, Double)) => (Double, Double)
def computePositionAndOrientation(theta: Double, dt: FiniteDuration): ((Double, Double)) => (Double, Double, Double)
```

- `computeWheelVelocities`: prende un motore differenziale e restituisce una tupla con le velocità delle ruote sinistra
  e destra;
- `computeVelocities`: prende il parametro `wheelDistance` (distanza tra le ruote) e restituisce una funzione
  `(vLeft: Double, vRight: Double) => (v: Double, omega: Double)` che calcola la velocità lineare `v` e la velocità
  angolare `omega` del robot basandosi sulle velocità delle ruote;
- `computePositionAndOrientation`: prende parametri di configurazione `theta` (orientamento iniziale) e `dt` (delta
  time) e restituisce una funzione `(v: Double, omega: Double) => (dx: Double, dy: Double, newOrientation: Double)`
  che calcola lo spostamento (dx, dy), cioè la nuova posizione e la nuova orientazione del robot basandosi sulle
  velocità lineare e angolare.

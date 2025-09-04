---
sidebar_position: 7
---

# Action

Per la modellazione della tipologia di azioni che è possibile svolgere dalle entità dinamiche è stato adottato un
approccio basato sul pattern **Tagless Final**, che consente di definire in maniera astratta e componibile i
comportamenti applicabili a un’entità senza vincolarsi a una specifica implementazione.

## Tagless final pattern

Il pattern **tagless final** consente di separare la definizione di un’azione dalla sua esecuzione concreta.
In particolare:

- l'interfaccia astratta `Action[F[_]]` descrive le operazioni disponibili e i comportamenti possibili,
  parametrizzandoli sul tipo di effetto `F[_]`;
- l'algebra concreta `ActionAlg[F, E]` specifica come tali operazioni vengono effettivamente eseguite su una
  determinata entità `E`.

Questo approccio favorisce la flessibilità e la riusabilità, consentendo di definire nuove azioni o contesti di
esecuzione senza modificare il modello di base delle entità dinamiche.

Di seguito viene riportata la definizione di `Action`:

```scala
trait Action[F[_]]:
  def run[E <: DynamicEntity](e: E)(using a: ActionAlg[F, E]): F[E]
```

L’esecuzione dell’azione è demandata a un’interfaccia separata,
`ActionAlg[F, E]`, che definisce l’algebra delle operazioni disponibili su un’entità dinamica `E` che deve
estendere `DynamicEntity`.
In questo modo si realizza una netta distinzione tra _cosa_ può essere fatto (la definizione dell’azione) e _come_ viene
fatto.

Al momento, `ActionAlg` definisce un singolo metodo `moveWheels`.

```scala
trait ActionAlg[F[_], E <: DynamicEntity]:
  def moveWheels(e: E, leftSpeed: Double, rightSpeed: Double): F[E]
```

Questo consente di applicare velocità diverse alle ruote dell’entità dinamica, permettendo così di controllarne il movimento.

> Nota: in futuro, questa interfaccia potrà essere estesa per includere ulteriori tipologie di azioni.

Sono state poi definite diverse implementazioni di `Action`:

- `MovementAction`: che rappresenta un movimento applicando velocità diverse alle ruote;
- `NoAction`: che lascia inalterata l’entità;
- `SequenceAction`: che permette di comporre più azioni in sequenza, garantendo un’esecuzione ordinata e monadica.

A supporto è stato introdotto anche l’oggetto `MovementActionFactory`, che fornisce un insieme di azioni predefinite
(avanti, indietro, svolta a sinistra/destra, stop) e un metodo per la creazione di movimenti personalizzati con
validazione sui parametri di velocità.

## Implementazione di `ActionAlg` per `Robot`

Nel _companion object_ `Robot` viene inoltre fornita l’implementazione di `ActionAlg[IO, Robot]`, ovvero
l’interprete dell’algebra delle azioni in un contesto di effetto `IO`.
In particolare, l’implementazione del metodo `moveWheels` aggiorna lo stato degli attuatori di tipo
`DifferentialWheelMotor`, applicando nuove velocità alle ruote sinistra e destra, e restituendo un nuovo stato del
robot incapsulato in `IO`.

Grazie a questa architettura e all’uso del pattern **Tagless Final** (introdotto nella modellazione delle azioni),
il robot può eseguire azioni in modo astratto e indipendente dal contesto, garantendo modularità ed estensibilità.

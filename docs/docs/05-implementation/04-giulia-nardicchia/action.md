---
sidebar_position: 7
---

# Action

Per la modellazione della tipologia di azioni che è possibile svolgere dalle entità dinamiche è stato adottato un
approccio basato sul pattern **Tagless Final**, che consente di definire in maniera astratta e composabile i
comportamenti applicabili a un’entità senza vincolarsi a una specifica implementazione.

L’interfaccia principale è `Action[F[_]]`, che rappresenta un’azione parametrizzata sul tipo di effetto `F[_]`, in modo
da poter essere eseguita in contesti differenti. Di seguito viene riportata la definizione di `Action`:
```scala
trait Action[F[_]]:
  def run[E <: DynamicEntity](e: E)(using a: ActionAlg[F, E]): F[E]
```
L’esecuzione dell’azione è demandata a un’interfaccia separata,
`ActionAlg[F, E]`, che definisce l’algebra delle operazioni disponibili su un’entità dinamica `E` che deve
estendere `DynamicEntity`.
In questo modo si realizza una netta distinzione tra _cosa_ può essere fatto (la definizione dell’azione) e _come_ viene
fatto.

Al momento, `ActionAlg` definisce un singolo metodo:
```scala
trait ActionAlg[F[_], E <: DynamicEntity]:
  def moveWheels(e: E, leftSpeed: Double, rightSpeed: Double): F[E]
```
- `moveWheels`, che consente di applicare velocità diverse alle ruote dell’entità dinamica, permettendo così di
  controllarne il movimento.

In futuro, questa interfaccia potrà essere estesa per includere ulteriori tipologie di azioni.

Sono state poi definite diverse implementazioni di `Action`:

- `MovementAction`: che rappresenta un movimento applicando velocità diverse alle ruote;
- `NoAction`: che lascia inalterata l’entità;
- `SequenceAction`: che permette di comporre più azioni in sequenza, garantendo un’esecuzione ordinata e monadica.

A supporto è stato introdotto anche l’oggetto `MovementActionFactory`, che fornisce un insieme di azioni predefinite
(avanti, indietro, svolta a sinistra/destra, stop) e un metodo per la creazione di movimenti personalizzati con
validazione sui parametri di velocità.
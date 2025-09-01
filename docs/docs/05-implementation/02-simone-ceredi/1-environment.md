---
sidebar_position: 1
---

# Implementazione dell'ambiente di simulazione

L'ambiente di simulazione è implementato a partire da un _trait_ `EnvironmentParameters` che definisce i parametri dell'ambiente, quali:

- Dimensioni dell'ambiente
- Entità presenti nell'ambiente
- LightField per ottenere informazioni sulla luce nell'ambiente

Una case class `Environment` implementa il trait `EnvironmentParameters` e fornisce i dettagli specifici dell'ambiente di simulazione, come le dimensioni, le entità presenti e le informazioni sulla luce.
Le informazioni sulla luce sono calcolate solo se necessario, grazie a un sistema di lazy evaluation che evita calcoli inutili.

## DSL

Per la creazione dell'ambiente è possibile utilizzare un Domain Specific Language (DSL) che semplifica la definizione delle proprietà dell'ambiente e delle entità in esso contenute.

```scala
infix def withWidth(width: Int): Environment

infix def withHeight(height: Int): Environment

def withHighPrecisionLighting: Environment

def withFastLighting: Environment

def withDefaultLighting: Environment

infix def containing(entities: Set[Entity]): Environment

infix def containing(entity: Entity): Environment

infix def and(entity: Entity): Environment

infix def validate: Validation[ValidEnvironment]
```

:::info

In questo modo è possibile creare un ambiente tramite:

```scala
environment withWidth 10 withHeight 10 containing robot and obstacle and light
```

:::

Allo stesso modo, il DSL rende più semplice la gestione della configurazione dell'ambiente, come mostrato nella sezione [Gestione della configurazione](./4-configuration.md).

## Gestione della validazione

La validazione dell'ambiente e delle entità al suo interno sfrutta il package `io.github.srs.model.validation` che fornisce strumenti per la definizione di regole di validazione e per l'applicazione di queste regole alle entità presenti nell'ambiente.
All'interno di environment le regole sono applicate tramite la funzione `validate`:

```scala
infix def validate: Validation[ValidEnvironment] =
  val entities = env.entities.filterNot:
    case _: Boundary => true
    case _ => false
  val boundaries = Boundary.createBoundaries(env.width, env.height)
  for
    width <- bounded(s"$Self width", env.width, MinWidth, MaxWidth, includeMax = true)
    height <- bounded(s"$Self height", env.height, MinHeight, MaxHeight, includeMax = true)
    _ <- bounded(s"$Self entities", env.entities.size, 0, MaxEntities, includeMax = true)
    entities <- withinBounds(s"$Self entities", entities, width, height)
    entities <- noCollisions(s"$Self entities", entities ++ boundaries)
    _ <- entities.toList.traverse_(validateEntity)
  yield ValidEnvironment.from(env.copy(entities = entities))
```

In questo modo si vanno a controllare le dimensioni dell'ambiente, oltre alla posizione delle entità al suo interno e alla presenza di eventuali collisioni tra di esse.
Si garantisce poi la validità di tutte le entità presenti nell'ambiente, assicurando che rispettino le regole di dominio definite.

Se le regole sono rispettate viene quindi creato un valore `ValidEnvironment` che rappresenta l'ambiente di simulazione valido.
Questo tipo viene utilizzato all'interno del motore di simulazione per garantire che solo ambienti validi vengano utilizzati durante la simulazione.
Inoltre , `ValidEnvironment` è utilizzato durante la gestione della configurazione realizzata dall'utente, per garantire che le configurazioni siano sempre valide e coerenti con le regole di dominio.

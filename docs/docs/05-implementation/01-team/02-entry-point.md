# Entry Point

Il punto di ingresso del simulatore è il file `Main.scala`, che si occupa di:

- effettuare il parsing degli argomenti da linea di comando;
- ottenere la configurazione iniziale del simulatore;
- avviare il ciclo di simulazione.

## Avvio del simulatore

L'avvio del simulatore avviene tramite la funzione `run` definita nell'oggetto `Launcher`.

Questa funzione si occupa di avviare il simulatore o in modalità grafica o in modalità testuale.

```scala
object Launcher:
  def run(headless: Boolean = true)(simulationConfig: SimulationConfig[ValidEnvironment]): IO[Option[SimulationState]]
```

Quando viene richiamata dal `Main` viene inizialmente effettuata una _partial application_ della funzione `run`, passando come argomento il valore booleano `headless` che indica se il simulatore deve essere eseguito in modalità testuale (true) o grafica (false).
Successivamente viene passata la configurazione iniziale del simulatore.

```scala
val run = Launcher.run(parsed.headless)

val runner = for
cfg <- configurationView.init()
_ <- configurationView.close()
_ <- run(cfg)
yield ()
```

Il valore di ritorno della funzione `run` è un effetto `IO` che restituisce opzionalmente lo stato finale (`IO[Option[SimulationState]]`).

### Domain Specific Language (DSL)

È stato definito un **Domain Specific Language** (DSL) per la creazione della configurazione iniziale del simulatore.
Questo DSL, oltre a configurare il simulatore, permette inoltre di eseguire la simulazione, tramite il metodo `>>>`.

```scala
extension (simulationConfig: SimulationConfig[ValidEnvironment])
  @targetName("run")
  infix def >>> : IO[Option[SimulationState]] =
    Launcher.run()(simulationConfig)
```

:::tip Esecuzione della simulazione tramite il DSL

```scala
simulation withDuration 100000 withSeed 42 in environment >>>
```

:::

Questo permetterà di ottenere lo stato finale della simulazione, senza dover passare per l'entry point `Main`, utile per eseguire test automatici e validare il comportamento del simulatore.

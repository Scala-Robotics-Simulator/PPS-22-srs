---
sidebar_position: 5
---

# Implementazione della GUI di configurazione

La GUI di configurazione è stata implementata sulla base del trait `ConfigurationView`.

```scala
trait ConfigurationView:
  def init(): IO[SimulationConfig[ValidEnvironment]]
  def close(): IO[Unit]
```

L'utilizzo di `cats.effect.IO` nel metodo `init`, wrappando `SimulationConfig[ValidEnvironment]`, consente di gestire in modo efficace le interazioni dell'utente con l'interfaccia. Ciò che interessa al chiamante della funzione è che il risultato dell'IO sarà una configurazione valida del simulatore, con la quale potrà andare ad avviare la simulazione.

Il metodo `close` è separato da `init` per lasciare libertà all'utilizzatore di gestire la chiusura dell'interfaccia utente in modo indipendente dall'inizializzazione, e non obbligatoriamente quando la configurazione è stata scelta.

## ConfigurationGUI

`ConfigurationGUI` estende da `ConfigurationView` e fornisce un'implementazione concreta dell'interfaccia utente, sfruttando la libreria `Swing`.

Per modularizzare la gestione dell'interfaccia sono stati creati diversi componenti che vanno a costituire la GUI, e comunicano tramite _callback_.

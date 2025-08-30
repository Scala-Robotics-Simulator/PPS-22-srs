---
sidebar_position: 4
---

# Configurazione

Come descritto nella sezione di [design di dettaglio](../../04-detailed-design/08-configuration.md) dedicata, la gestione della configurazione è stata implementata sfruttando il pattern **Tagless Final**.
Per la gestione della configurazione è stato creato il trait `ConfigManager` che definisce i metodi per il caricamento e il salvataggio della configurazione.

```scala
trait ConfigManager[F[_]]:
  def load: F[ConfigResult[SimulationConfig[Environment]]]
  def save(config: SimulationConfig[Environment]): F[Unit]
```

Questa implementazione consente di gestire in modo flessibile la configurazione del simulatore, grazie alla definizione di multipli **interpreti** dell'**algebra** di configurazione (ad esempio `YamlConfigManager` per la gestione della configurazione in formato YAML).

## YAML

### Struttura

Dato che la configurazione del simulatore risulterebbe molto complessa se trasformata in un singolo file YAML, la struttura è stata semplificata introducendo parametri che fungono da scorciatoie per configurare diverse porzioni del simulatore in modo più intuitivo.

Nel caso dei robot, ad esempio, invece che specificare ogni sensore disponibile al robot, è possibile utilizzare un parametro che indica se il robot dispone dei sensori di prossimità o di luce standard.

Anche per la gestione degli attuatori è possibile specificare la velocità di movimento del robot, invece di dover configurare ogni singolo attuatore.

```yaml
- robot:
    position: [3, 1]
    orientation: 90.0
    radius: 0.5
    speed: 1.0
    withProximitySensors: true
    withLightSensors: true
    behavior: CollisionAvoidance
```

## Parsing della configurazione YAML

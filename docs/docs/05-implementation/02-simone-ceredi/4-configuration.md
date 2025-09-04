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

Questa implementazione consente di gestire in modo flessibile la configurazione del simulatore, grazie alla definizione di _interpreti_ multipli dell'_algebra_ di configurazione (ad esempio `YamlConfigManager` per la gestione della configurazione in formato _YAML_).

## YAML

### Struttura

Dato che la configurazione del simulatore risulterebbe molto complessa se trasformata in un singolo file _YAML_, la struttura è stata semplificata introducendo parametri che fungono da scorciatoie per configurare diverse porzioni del simulatore in modo più intuitivo.

Nel caso dei robot, ad esempio, invece che specificare ogni sensore disponibile al robot, è possibile utilizzare un parametro che indica se il robot dispone dei sensori di prossimità o di luce standard (8 sensori per tipo, orientati a 45 gradi l'uno dall'altro).

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

## Parsing della configurazione

Per il parsing della configurazione _YAML_ è stata utilizzata la libreria [scala-yaml](https://github.com/VirtusLab/scala-yaml), che permette di effettuare il parsing di un file _YAML_ trattandolo come una `Map[String, Any]`. Questo approccio consente di gestire in modo flessibile la configurazione, permettendo di utilizzare parametri di alto livello per configurare il simulatore senza dover specificare ogni dettaglio.

Il codice sotto mostra come viene effettuato il parsing della configurazione _YAML_ per un robot, estraendo i vari parametri dalla mappa e creando un'istanza di `Robot` con le opzioni specificate.

```scala
  private def parseRobot(map: Map[String, Any]): ConfigResult[Entity] =
    for
      id <- getOptional[UUID](EntityFields.Id, map)
      pos <- get[List[Double]](EntityFields.Position, map)
      position <- parsePosition(pos)
      orient <- getOptional[Double](EntityFields.Orientation, map)
      radius <- getOptional[Double](RobotFields.Radius, map)
      speed <- getOptional[Double](RobotFields.Speed, map)
      prox <- getOptional[Boolean](RobotFields.WithProximitySensors, map)
      light <- getOptional[Boolean](RobotFields.WithLightSensors, map)
      behavior <- getOptional[Policy](RobotFields.Behavior, map)
    yield Robot().at(position)
      |> (r => id.fold(r)(r.withId))
      |> (r => orient.fold(r)(o => r.withOrientation(Orientation(o))))
      |> (r => radius.fold(r)(radius => r.withShape(ShapeType.Circle(radius))))
      |> (r => speed.fold(r)(s => r.withSpeed(s)))
      |> (r => if prox.getOrElse(false) then r.withProximitySensors else r)
      |> (r => if light.getOrElse(false) then r.withLightSensors else r)
      |> (r => behavior.fold(r)(b => r.withBehavior(b)))
```

I metodi `get` e `getOptional` hanno le seguenti signature:

```scala
def get[A](field: String, map: Map[String, Any])(using decoder: Decoder[A]): ConfigResult[A]
def getOptional[A](field: String, map: Map[String, Any])(using decoder: Decoder[A]): ConfigResult[Option[A]]
```

Il `decoder` è responsabile della conversione dei valori estratti dalla mappa nel tipo desiderato, gestendo eventuali errori di parsing in modo appropriato.
L'utilizzo di `decoder` tramite `given` consente di semplificare il processo di decoding, rendendo il codice più pulito e leggibile.

I `decoder` sono definiti come istanze di `Decoder[A]`:

```scala
  given Decoder[Int] with

    def decode(field: String, value: Any): ConfigResult[Int] =
      value match
        case n: Number =>
          if n.intValue() == n.longValue() then Right[Seq[ConfigError], Int](n.intValue())
          else Left[Seq[ConfigError], Int](Seq(ConfigError.InvalidType(field, "Int - value out of range")))
        case s: String =>
          try Right[Seq[ConfigError], Int](s.toInt)
          catch
            case _: NumberFormatException =>
              Left[Seq[ConfigError], Int](Seq(ConfigError.InvalidType(field, "Int - invalid string format")))
        case _ => Left[Seq[ConfigError], Int](Seq(ConfigError.InvalidType(field, "Int")))
```

Il metodo `|>` è un operatore di piping che consente di passare il risultato di un'espressione come input alla successiva. Questo rende il codice più leggibile e consente di concatenare più operazioni in modo fluido. Ad esempio, nel parsing della configurazione _YAML_, l'operatore `|>` viene utilizzato per applicare una serie di trasformazioni all'istanza di `Robot` in modo chiaro e conciso.

## Serializzazione della configurazione

La libreria `scala-yaml` non dispone di documentazione sufficiente per la definizione di serializzatori _YAML_ customizzati, pertanto per la serializzazione della configurazione è stata utilizzata una soluzione alternativa basata su [circe-yaml](https://github.com/circe/circe-yaml).
La libreria nasce per la serializzazione e deserializzazione di dati in formato JSON, e fornisce un'estensione che consente di lavorare con il formato _YAML_.

Per la serializzazione della configurazione, sono stati definiti `Encoder` specifici per ciascun tipo di dato utilizzato nella configurazione.

```scala
given Encoder[Robot] = (robot: Robot) =>
  val dwt = robot.actuators.collectFirst { case dwt: DifferentialWheelMotor =>
    dwt
  }
  val speeds = dwt.map(d => (d.left.speed, d.right.speed))
  val withProximitySensors = StdProximitySensors.forall(robot.sensors.contains)
  val withLightSensors = StdLightSensors.forall(robot.sensors.contains)
  Json
    .obj(
      EntityFields.Id -> robot.id.asJson,
      EntityFields.Position -> robot.position.asJson,
      RobotFields.Radius -> robot.shape.radius.asJson,
      EntityFields.Orientation -> robot.orientation.degrees.asJson,
      RobotFields.WithProximitySensors -> withProximitySensors.asJson,
      RobotFields.WithLightSensors -> withLightSensors.asJson,
      RobotFields.Behavior -> robot.behavior.toString().asJson,
    )
    .deepMerge(
      speeds.map(RobotFields.Speed -> _._1.asJson).toList.toMap.asJson,
    )

```

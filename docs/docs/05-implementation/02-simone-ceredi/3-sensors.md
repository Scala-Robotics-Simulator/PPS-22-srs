---
sidebar_position: 3
---

# Sensori

L'idea di implementazione iniziale per i sensori era quella di utilizzare il pattern **Tagless Final** per definire i vari tipi di sensori in modo modulare e riutilizzabile.

Tuttavia l'utilizzo di tale pattern avrebbe portato il trait `Sensor` ad avere la seguente forma:

```scala
trait Sensor[F[_], Entity, Environment]:
  type Data
  def sense: F[Data]
```

L'**effect type** `F`, rappresentante il contesto in cui il sensore opera, si sarebbe però riversato su **Robot**, in quanto robot contiene al suo interno i vari sensori.
Una possibile soluzione era quella di definire un trait `Robot[F[_]]` che incapsula l'effetto e fornisce metodi per interagire con i sensori.

```scala
trait Robot[F[_]]:
  def sensors: List[Sensor[F, Entity, Environment]]
```

Ma questo avrebbe "sporcato" la modellazione di **Robot**.

Altra possibilità era quella di definire già il tipo `F` all'interno di `Robot`, ad esempio come `cats.effect.IO`.

```scala
trait Robot:
  def sensors: List[Sensor[IO, Entity, Environment]]
```

Soluzione che sarebbe però restrittiva e limiterebbe la flessibilità nell'uso di altri effetti.

La soluzione implementata è quindi un ibrido, che permette di mantenere la flessibilità nell'uso di diversi effetti, senza "sporcare" la modellazione di **Robot**.

```scala
trait Sensor[Entity, Environment]:
  type Data
  def sense[F[_]]: F[Data]
```

In questo modo, il tipo di effetto può essere specificato al momento della chiamata al metodo `sense`, consentendo una maggiore flessibilità nell'uso di diversi contesti di effetto.

## Sensori di prossimità

I sensori di prossimità sono in grado di rilevare la presenza di entità all'interno dell'ambiente circostante il robot. L'implementazione della logica di rilevamento si basa sull'uso di un algoritmo di **ray-casting**, che simula raggi di luce e determina se un oggetto si trova lungo il percorso di un raggio entro la distanza di rilevamento specificata.

## Sensori di luce

I sensori di luce sono in grado di rilevare la presenza e l'intensità della luce nell'ambiente circostante il robot. L'implementazione della logica di rilevamento si basa sull'uso di un `lightField`, presente all'interno dell'ambiente, che fornisce informazioni sulla quantità di luce nelle diverse aree dell'ambiente.

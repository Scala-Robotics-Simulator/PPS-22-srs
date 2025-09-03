---
sidebar_position: 2
---

# Illumination

## Pipeline di Calcolo

La funzione `computeLightField` orchestra la pipeline:

1. **Dimensionamento**: traduce lo spazio continuo in una griglia discreta (in funzione di `ScaleFactor`);
2. **Occlusione**: costruisce la mappa di occlusione a partire dall’ambiente (ostacoli sempre, dinamici opzionali).
3. **Per-luce** + **combina**: calcola il contributo di ogni luce e li somma in un unico campo luminoso (`LightField`).

```scala
def computeLightField(scale: ScaleFactor)(fov: FovEngine)
                     (includeDynamic: Boolean)(env: Environment): LightField = {
  given ScaleFactor = scale

  // 1) dimensionamento
  val dims = GridDims.from(env)(scale)
  val lights = env.lights.toVector

  // 2) occlusione
  val occlusion = computeOcclusion(env, dims, includeDynamic)

  // 3) Calcola e combina i contributi di tutte le luci
  computeField(dims, lights, occlusion, fov)
}
```

### Rasterizzazione delle Occlusioni

La conversione delle geometrie in occlusioni è ottimizzata per ogni forma:

- **Cerchi**: Algoritmo scan-line fill.
- **Rettangoli** allineati: Fast path che riempie direttamente l'area.
- **Rettangoli ruotati**: Test inverse-rotate sul centro di ogni cella all'interno di un'area di interesse ristretta.

```scala
// OcclusionRaster.scala (estratto)
def rasterizeStatics(env, dims)(using ScaleFactor) =
  rasterizeEntities(env.entities.collect { case ob: StaticEntity.Obstacle => ob }, dims)

def rasterizeDynamics(env, dims)(using ScaleFactor) =
  rasterizeEntities(env.entities.collect { case d: DynamicEntity => d }, dims)

private def rasterizeCircle(center: Point2D, radius: Double, dims: GridDims)
                           (using s: ScaleFactor): Iterator[Int] = {
  // Scanline: per ogni riga che interseca il cerchio, riempi lo span [minX,maxX]
  // Indice lineare: dims.toIndex(x,y)
...
}

private def rasterizeAxisAlignedRect(center, w, h, orientation, dims)
                                    (using s: ScaleFactor): Iterator[Int] = {
  // Rettangoli allineati: riempi AABB (con fast-path per 0/90/180/270)
...
}

private def rasterizeRotatedRect(center, w, h, orientation, dims)
                                (using s: ScaleFactor): Iterator[Int] = {
  // AABB stretta in world-space → inverse-rotate il centro cella → inside test
...
}

def combine(base, overlay) = Grid.overlayMax(base, overlay) // max per cella (0/1)
```

### Calcolo Per-Luce e Combinazione

Per ogni campo luminoso, calcoliamo un campo locale con `FovEngine` e li riduciamo. La combinazione può essere
parallelizzata in modo adattivo in base alla dimensione della griglia e al numero di luci.
Per ottimizzare, usiamo validazioni economiche (bounds, raggio, intensità) per evitare computazione inutile.
Infine, la somma dei contributi usa una somma saturata (`zipSat`) per mantenere i valori in `[0,1]`.


```scala

// IlluminationLogic.scala (estratto)

private def computeField(dims, lights, occlusion, fov)(using scale: ScaleFactor): LightField = {
  val shouldPar = dims.totalCells >= Illumination.GridThreshold ||

  lights.sizeIs >= Illumination.LightThreshold

  val perLight = computeAllLightContributions(lights, dims, occlusion, fov, shouldPar)

  val combined = perLight match
    case Vector() => ArraySeq.fill(dims.totalCells)(0.0)
    case Vector(single) => single
    case many =>
      if (!shouldPar && many.sizeIs >= Illumination.LightThreshold)
        combineParallel(many)
      else
        combineSequential(dims.totalCells, many)
  LightField(dims, combined)

}


private def computeSingleLightContribution(dims, occlusion, fov, light)(using scale: ScaleFactor): Field = {
...
  val (cx, cy) = Cell.toCellFloor(light.position)

  val radiusInCells = Cell.radiusCells(light.illuminationRadius).toDouble

  val intensity = clampTo01(light.intensity)

  if (!dims.inBounds(cx, cy) || intensity == 0.0 || radiusInCells <= 0.0)
    ArraySeq.fill(dims.totalCells)(0.0)
  else {
    val raw = fov.compute(occlusion)(cx, cy, radiusInCells)

    applyIntensity(intensity, raw)

  }
}

```

### Engine FOV Pluggable

L'interfaccia `FovEngine` astrae l'algoritmo di propagazione della luce. L'implementazione può essere sostituita senza
modificare il resto della pipeline, permettendo di sperimentare con diversi profili di decadimento e/o ottimizzazioni.

```scala
// engine/FovEngine.scala
trait FovEngine {
  def compute(occlusion: Grid[Double])
             (startX: Int, startY: Int, radius: Double): ArraySeq[Double]
}

// engine/SquidLibFovEngine.scala
object SquidLibFovEngine extends FovEngine {
  def compute(occ)(sx, sy, r) = {
    val buffer = Array.ofDim[Double](occ.width, occ.height)
    FOV.reuseFOV(occ, buffer, sx, sy, r)
    buffer.flattenRowMajor // row-major, x-fast
  }
}
```

### LightField: Query dai Sensori

Il `LightField` è il dato finale interrogato dai sensori: prende coordinate continue e restituisce un valore _smooth_
grazie all’interpolazione bilineare sulle quattro celle adiacenti. Fuori dai confini, il valore è 0.0 per un
comportamento prevedibile.

```scala
// model/LightField.scala (estratto)
final case class LightField(dims: GridDims, data: ArraySeq[Double]) {
  def illuminationAt(pos: Point2D)(using scale: ScaleFactor): Double = {
    val gridX = pos.x * scale
    val gridY = pos.y * scale
    if (pos.x < 0 || pos.y < 0) 0.0
    else bilinearInterpolate(gridX, gridY)
  }

  private def bilinearInterpolate(gx: Double, gy: Double): Double = {
    // Interpolazione bilineare tra le 4 celle circostanti
  ...
  }
}
```

### Type-Safe Scale e Dimensioni

`ScaleFactor` è un _tipo opaco_ (`opaque type`) che incapsula un intero con validazione (1..1000). Questo previene errori
di
configurazione e rende esplicita la sua semantica di "celle per metro".

```scala
// model/ScaleFactor.scala
opaque type ScaleFactor = Int

object ScaleFactor:
  def validate(n: Int): Validation[ScaleFactor] =
    bounded("ScaleFactor", n, 1, 1000, includeMax = true).map(v => v: ScaleFactor)

...
```

### Integrazione con Environment

Il campo luce è un `lazy val`, quindi viene calcolato on-demand al primo accesso e poi riusato in modo da fare caching.
In
caso le entità o luci cambiano o semplicemente si vuole aggiornare il campo, bisogna ricomputare via LightMap o ricrea
l’Environment.

```scala
// Environment.scala
final case class Environment(

...,
private[environment] val _lightMap: Option[LightMap[IO]] = None
):

private val lightMap: LightMap[IO] =
  _lightMap.getOrElse(LightMapConfigs.BaseLightMap)

lazy val lightField: LightField =
  lightMap.computeField(this, includeDynamic = true).unsafeRunSync()
```

### Facciata LightMap (Effect-Aware)

La facade `LightMap` isola il codice di calcolo dai side-effect (come l'esecuzione asincrona gestita da IO di Cats
Effect).

```scala
// LightMap.scala
trait LightMap[F[_]]:
  def computeField(env: Environment, includeDynamic: Boolean): F[LightField]
```

### Configurazioni Disponibili

La scelta del preset si fa via DSL senza toccare il core. Ci sono tre preset nel `LightMapConfigs`:

1. **High Precision** (scale = 100):
    - Alta qualità visiva;
    - Adatto per ambienti piccoli/medi.
2. **Fast** (scale = 5):
    - Performance ottimale;
    - Adatto per ambienti grandi.
3. **Default** (scale = 10):
    - Bilanciamento qualità/performance;
    - Configurazione standard.

## Motivazioni per la Scelta di SquidLib

La libreria **SquidLib** è stata scelta per il calcolo del FOV per diverse ragioni:

* **Basata su Griglia**: opera nativamente su griglie 2D.
* **Fisica Semplificata**: fornisce un modello di visibilità binario (visibile/non visibile) e un'attenuazione lineare.
* **Performance**: è ottimizzata per calcoli rapidi su griglie.
* **API Minimale**: l'API è semplice e diretta (`FOV.reuseFOV(...)`), facilitando l'integrazione.
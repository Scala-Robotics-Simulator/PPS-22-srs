---
sidebar_position: 2
---

# Illumination

## Pipeline di calcolo

La funzione `computeLightField` orchestra la pipeline:

1. **dimensionamento**: traduce lo spazio continuo in una griglia discreta (in funzione di `ScaleFactor`);
2. **occlusione**: costruisce la mappa di occlusione a partire dall’ambiente (ostacoli e dinamici opzionali);
3. **combinazione**: calcola il contributo di ogni luce e li somma in un unico campo luminoso (`LightField`).

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

### Rasterizzazione delle occlusioni

La conversione delle geometrie in occlusioni è ottimizzata per ogni forma:

- **cerchi**: algoritmo scan-line fill;
- **rettangoli** allineati: fast path che riempie direttamente l'area;
- **rettangoli ruotati**: test inverse-rotate sul centro di ogni cella all'interno di un'area di interesse ristretta.

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

### Calcolo e combinazione del campo luminoso

Per ogni campo luminoso, si calcola un campo locale con `FovEngine` e lo si riduce. La combinazione può essere
parallelizzata in modo adattivo in base alla dimensione della griglia e al numero di luci.
Per ottimizzare, si usano guardie (bounds, raggio, intensità) per evitare computazione inutile.
Infine, per ottenere la somma dei contributi si usa una somma saturata (`zipSat`) per mantenere i valori in `[0,1]`.

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

### Engine FOV pluggable

L'interfaccia `FovEngine` astrae l'algoritmo di propagazione della luce. L'implementazione può essere sostituita senza
modificare il resto della pipeline, permettendo di sperimentare con diversi profili di decadimento.

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

### LightField: query dai sensori

Il `LightField` è il dato finale utilizzato dai sensori: prende coordinate continue e restituisce un valore _smooth_
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

### Type-safe scale e dimensioni

`ScaleFactor` è un _tipo opaco_ (`opaque type`) che incapsula un intero con validazione (1..1000). Questo previene
errori di configurazione e rende esplicita la sua semantica di "celle per metro".

```scala
// model/ScaleFactor.scala
opaque type ScaleFactor = Int

object ScaleFactor:
  def validate(n: Int): Validation[ScaleFactor] =
    bounded("ScaleFactor", n, 1, 1000, includeMax = true).map(v => v: ScaleFactor)

...
```

### Integrazione con environment

Il campo luce è un `lazy val`, quindi viene calcolato on-demand al primo accesso e poi riusato in modo da fare caching.
In caso le entità o luci cambiano o semplicemente si vuole aggiornare il campo, bisogna ri-elaborare attraverso la
`LightMap` o ricreare l’`Environment`.

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

### Facade LightMap (Effect-Aware)

La facade `LightMap` isola il codice di calcolo dai side-effect (come l'esecuzione asincrona gestita da `IO` di `cats.effect`).

```scala
// LightMap.scala
trait LightMap[F[_]]:
  def computeField(env: Environment, includeDynamic: Boolean): F[LightField]
```

#### Tagless final pattern

`LightMap` segue il **tagless final pattern**: le operazioni sono parametrizzate su un tipo di effetto `F[_]` e vincolate solo alle capacità necessarie, ad esempio `Sync` nel caso di `LightMapImpl`.
Il vincolo di tipo (`cats.effect.Sync`) definisce le capacità richieste - sospendere side-effect - senza imporre implementazioni concrete.

In questo contesto si distinguono quindi:

- **algebra**: il trait `LightMap`, che definisce le operazioni disponibili senza specificare come debbano essere implementate;
- **interpreti**: le varie implementazioni concrete dell’algebra. Ad esempio `LightMapImpl[F]` interpreta le operazioni eseguendo i calcoli richiesti, mentre in futuro potrebbero esserci interpreti diversi.

Questo approccio consente:

- indipendenza dal tipo di effetto specifico utilizzato per l'esecuzione;
- migliore testabilità tramite interpreti fittizi o mock;
- separazione netta tra la definizione dell'algebra (`LightMap`) e le implementazioni concrete (_interpreti_ come `LightMapImpl`).

### Configurazioni disponibili

Il preset viene scelto tramite DSL senza toccare il core. Sono presenti tre preset nel `LightMapConfigs`:

1. `HighPrecision` (scale = 100):
   - alta qualità visiva;
   - adatto per ambienti piccoli/medi.
2. `Fast` (scale = 5):
   - performance ottimale;
   - adatto per ambienti grandi.
3. `Default` (scale = 10):
   - bilanciamento qualità/performance;
   - configurazione standard.

## Motivazioni per la scelta di SquidLib

La libreria **SquidLib** è stata scelta per il calcolo del FOV per diverse ragioni:

- **basata su griglia**: opera nativamente su griglie 2D;
- **fisica semplificata**: fornisce un modello di visibilità binario (visibile/non visibile) e un'attenuazione lineare;
- **performance**: è ottimizzata per calcoli rapidi su griglie;
- **API minimale**: l'API è semplice e diretta (`FOV.reuseFOV(...)`), facilitando l'integrazione.

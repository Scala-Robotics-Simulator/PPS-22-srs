---
sidebar_position: 5
---

# Entity

In questa sezione, viene descritta la struttura e le funzionalità delle entità nel sistema di simulazione.

## Entità

![Entity](../../static/img/04-detailed-design/entity.png)

Il `trait` `Entity` modella un oggetto spaziale 2D con:

- `id: UUID` - ID univoco dell'entità;
- `position: Point2D` — posizione cartesiana;
- `shape: ShapeType` — forma geometrica (vedi sotto);
- `orientation: Orientation` — direzione espressa in gradi.

Costituisce l’interfaccia base comune per oggetti **statici** e **dinamici**.

### Posizione

`Point2D` rappresenta un punto nel piano cartesiano (`x: Double` e `y: Double`) e fornisce primitive geometriche:

- somma/sottrazione vettoriale, moltiplicazione per scalare;
- prodotto scalare, modulo, normalizzazione;
- distanza euclidea.

> Queste operazioni sono la base per spostamenti, direzioni e collisioni.

### Forma

La forma è un’`enum` `ShapeType` con due varianti:

- `Circle(radius: Double)` — cerchio di raggio `radius`;
- `Rectangle(width: Double, height: Double)` — rettangolo con larghezza/altezza.

> Questo approccio consente una modellazione semplice ma estensibile delle dimensioni fisiche degli oggetti nello spazio
> simulato.

### Orientamento

`Orientation` rappresenta l’angolo di rotazione rispetto a un riferimento fisso:

- `degrees: Double` — gradi;
- `toRadians: Double` — conversione in radianti per la trigonometria.

> L’orientamento permette di rappresentare la direzione verso cui è rivolto un oggetto nello spazio, supportando il
> movimento direzionale e la rotazione.

## Entità statiche e dinamiche

![Entities](../../static/img/04-detailed-design/entities.png)

`StaticEntity` e `DynamicEntity` estendono `Entity` separando responsabilità e capacità.

### DynamicEntity

`DynamicEntity` rappresenta un’entità che **percepisce** e **agisce** nel ciclo **sense → decide → act**:

- `sensors: Vector[Sensor[? <: DynamicEntity, ? <: Environment, ?]]`;
- `actuators: Seq[Actuator[? <: DynamicEntity]]`;
- `behavior: Policy `.

I **sensori** raccolgono dati dall’ambiente; il **behavior** elabora le letture in base alla *policy* e seleziona
un’azione; gli **attuatori** applicano l’azione all’entità. Questa struttura consente di modellare agenti robotici con
ciclo **sense–decision–act**.

:::info

Approfondimento: progettazione del motore decisionale (**Behavior**) — regole, comportamenti, policy e DSL — nella
pagina di design [Behavior](./06-behavior.md).

:::

### StaticEntity

`StaticEntity` modella entità **non mobili** che possono interagire passivamente con l'ambiente. Possono essere:

- `Obstacle`/ `Boundary` — rettangoli (orientabili) che occupano spazio e collidono;
- `Light` — cerchi che emettono illuminazione (isotropica).

> I **boundary** sono creati automaticamente durante la **validazione** dell’ambiente: sono rettangoli sottili posti sui
> bordi, partecipano a collisioni e alla resistenza luminosa, e sono percepibili dai sensori di prossimità.

## Ostacoli

Gli **ostacoli** (`StaticEntity.Obstacle`) sono “muri” rettangolari che bloccano il movimento e la luce, creando
corridoi, stanze e scenari di navigazione.

**Attributi principali**:

- `id: UUID` — identificativo univoco;
- `position: Point2D` — posizione del centro dell’ostacolo;
- `orientation: Orientation` — angolo in gradi;
- `width: Double`, `height: Double` — dimensioni;
- `shape = Rectangle(width, height)` — forma rettangolare.

**Validazione**:

- Dimensioni **> 0**;
- Inclusione nei limiti dell’ambiente;
- **Assenza di sovrapposizioni** con altre entità;

> Servono a costruire scenari realistici — dal corridoio stretto al piccolo labirinto — in cui i robot devono
> pianificare il movimento ed evitare collisioni.

## Luce

Le **luci** (`StaticEntity.Light`) sono sorgenti **radiali** che forniscono un **campo percettivo** per i
`LightSensor` e rendono osservabile lo stato luminoso (`Environment.lightField`).

**Attributi**:

- `id: UUID` - identificativo univoco;
- `position: Point2D` — posizione del centro della luce;
- `orientation: Orientation` — presente per uniformità di modello; impostato a `Orientation(0.0)`; l’emissione è *
  *isotropica** (non direzionale);
- `radius: Double` — raggio **geometrico** usato per la `shape` (`ShapeType.Circle(radius)`);
- `illuminationRadius: Double` — **portata luminosa** efficace (raggio del contributo nel campo luce);
- `intensity: Double`, `attenuation: Double` — livello e **decadimento** con la distanza;
- `shape = Circle(radius)` — forma circolare.

**Validazione**:

- parametri strettamente `> 0`;
- inclusione nei limiti dell’ambiente;
- **assenza di sovrapposizioni** con altre entità.

> Servono a creare zone buie/illuminate utili per testare policy di navigazione e priorità tra comportamenti.

## Boundary

I **boundary** (`StaticEntity.Boundary`) definiscono i **confini** dell’ambiente. Sono creati **automaticamente** in
validazione, in modo tale che l'utente non debba preoccuparsi di definirli esplicitamente. Simili agli **ostacoli**,
ma con la differenza che hanno larghezza o altezza pari a zero.

**Attributi principali**:

- `id: UUID` — identificativo univoco;
- `position: Point2D` — centro del lato;
- `orientation: Orientation` — in factory impostato a `Orientation(0.0)`;
- `width: Double`, `height: Double` — **uno dei due è 0.0** (spessore zero);
- `shape = Rectangle(width, height)` *(derivata)*;
- **Factory**: `StaticEntity.Boundary.createBoundaries(width, height)` crea i 4 boundary ai bordi.

**Validazione**:

- Presenza su **tutti i lati** dell’ambiente;
- Dimensioni coerenti con i bordi (spessore zero consentito per i boundary);
- Nessuna sovrapposizione anomala con entità interne.

> Questi elementi sono fondamentali per definire i limiti entro cui le entità possono muoversi e interagire.

## Robot

![Robot](../../static/img/04-detailed-design/robot.png)

Il _case class_ `Robot` estende `DynamicEntity` e rappresenta un’entità autonoma in grado di muoversi e interagire con
l’ambiente circostante nello spazio bidimensionale. Ogni robot è caratterizzato da un identificativo univoco (`UUID`),
una posizione e un’orientazione nello spazio, nonché da una forma geometrica circolare (`ShapeType.Circle`).

Il robot è dotato di un insieme di attuatori (`Seq[Actuator[Robot]]`) e di una collezione di sensori
(`Vector[Sensor[Robot, Environment]]`), che gli permettono di percepire e raccogliere informazioni sull’ambiente.
Inoltre, possiede una strategia comportamentale (`Policy`) che definisce la logica decisionale del
robot in base ai dati forniti dai sensori.

Nel _companion object_ `Robot` viene inoltre fornita l’implementazione del _given_ `ActionAlg[IO, Robot]`, ovvero
l’interprete dell’algebra delle azioni in un contesto di effetto `IO`.

In particolare, l’implementazione del metodo `moveWheels` aggiorna lo stato degli attuatori di tipo
`DifferentialWheelMotor`, applicando nuove velocità alle ruote sinistra e destra, e restituendo un nuovo stato del
robot incapsulato in `IO`.

Grazie a questa architettura e all’uso del pattern **Tagless Final** (introdotto nella modellazione delle azioni),
il robot può eseguire azioni in modo astratto e indipendente dal contesto, garantendo modularità ed estensibilità.

:::info note
Vedere la sezione [Action](./07-action.md) per i dettagli sull’algebra delle azioni e il pattern **Tagless Final**.
:::

## Attuatori

![Actuator](../../static/img/04-detailed-design/actuator.png)

Un attuatore è un componente in grado di modificare lo stato di un’entità dinamica (`DynamicEntity`). Il _trait_
`Actuator[E]`
definisce l’interfaccia generica, tramite il metodo `act(dt, entity)`, che aggiorna l’entità dopo un intervallo
temporale
`dt`, restituendone una nuova istanza in un contesto monadico `F[_]`.

### Attuatori di movimento

Gli attuatori di movimento sono modellati tramite i motori differenziali (`DifferentialWheelMotor`), costituiti da due
ruote (`Wheel`) – sinistra e destra – dotate di velocità lineare (`speed`) e una forma circolare (`ShapeType.Circle`).
Il movimento del robot viene calcolato con un modello cinematico differenziale (`DifferentialKinematics`), in cui:

- **Velocità lineare** (media delle velocità delle due ruote; ottenute moltiplicando la velocità (`speed`) per il raggio
  della ruota (`radius`)):

$$
v = \frac{v_{\text{left}} + v_{\text{right}}}{2}
$$

- **Velocità angolare** (proporzionale alla differenza tra le velocità delle ruote divisa per la distanza tra le ruote;
  si assume che la distanza tra le ruote sia pari al diametro del robot):

$$
\omega = \frac{v_{\text{right}} - v_{\text{left}}}{d_{\text{wheel}}}
$$

- **Nuova posizione e orientazione del robot** integrando le equazioni del moto su un intervallo di tempo `dt`:

$$
x' = x + v \cdot \cos(\theta) \cdot dt
$$

$$
y' = y + v \cdot \sin(\theta) \cdot dt
$$

$$
\theta' = \theta + \omega \cdot dt
$$

Questa logica, incapsulata nel metodo `act`, consente di aggiornare lo stato del robot in modo funzionale e validato,
rendendo il comportamento dell’attuatore modulare ed estendibile.

## Sensori

![Sensor](../../static/img/04-detailed-design/sensor.png)

I sensori sono componenti che permettono a un'entità dinamica di percepire l'ambiente circostante. Il _trait_
`Sensor[Entity, Environment]` definisce un'interfaccia generica per i sensori.
I sensori sono parametrizzati su tre tipi:

- `Entity`: il tipo di entità che il sensore può percepire, sottotipo di `DynamicEntity` (ad esempio, `Robot`).
- `Environment`: il tipo di ambiente in cui il sensore opera, sottotipo di `Environment` (ad esempio, `Environment`
  stesso).
- `Data`: il tipo di dato restituito dal sensore.

Inoltre i sensori contengono un campo `offset` che rappresenta la posizione del sensore rispetto all'entità che lo
possiede.

Infine un metodo `sense[F[_]](entity: Entity, env: Environment): F[Data]` che permette di ottenere i dati di rilevamento
dal sensore.
Il tipo `F[_]` è un tipo di effetto generico (come `IO`, `Id`, `Task`, etc.) che permette:

- Astrazione rispetto al tipo di effetto utilizzato per l'esecuzione.
- Composizione funzionale con altre operazioni monadiche.
- Testabilità tramite interpreti fittizi o mock.

Il tipo `SensorReading` è un tipo di utilità che aiuta a rappresentare i dati letti da un sensore.
Si tratta di un _case class_ che contiene:

- `sensor: Sensor[?, ?]`: il sensore che ha effettuato la lettura.
- `value: A`: il valore letto dal sensore, parametrizzato su un tipo `A`.

Questo tipo consente di incapsulare le letture dei sensori in un formato coerente, facilitando la gestione e
l'elaborazione dei dati raccolti.

`SensorReadings` è un tipo di utilità che rappresenta una raccolta di letture dei sensori.

:::info

I dettagli implementativi riguardanti i sensori sono disponibili nella
sezione [Implementazione dei sensori](../05-implementation/02-simone-ceredi/3-sensors.md).

:::

### Sensori di prossimità

La _case class_ `ProximitySensor` estende `Sensor[Robot, Environment]` e rappresenta un sensore di prossimità che rileva
la presenza di altre entità nell'ambiente.
Questo sensore dispone di un campo `range` che rappresenta il raggio di azione del sensore.
I valori ritornati da questo sensore sono di tipo `Double`, che rappresenta la distanza alla quale si trova l'entità più
vicina, normalizzata tra 0 e 1, dove 0 indica che l'entità è molto vicina e 1 che è molto lontana.
Il metodo `sense` implementa la logica di rilevamento, tramite _Ray Casting_, che calcola la distanza tra il sensore e
le entità nell'ambiente, restituendo il valore normalizzato.

### Sensori di luce

La _case class_ `LightSensor` estende `Sensor[Robot, Environment]` e rappresenta un sensore di luce, in grado di
rilevare l'intensità luminosa in una determinata area.
Anche in questo caso i valori restituiti dal sensore sono `Double`, e rappresentano l'intensità luminosa normalizzata
tra 0 e 1, dove 0 indica assenza di luce e 1 indica luce massima.
La distribuzione della luce nell'ambiente è rappresentata da un campo `lightField` all'interno di `Environment`, il
metodo `sense` implementa la logica di rilevamento, utilizzando il `lightField` per ottenere i dati di intensità
luminosa.

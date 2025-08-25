---
sidebar_position: 4
---

# Entity

In questa sezione, viene descritta la struttura e le funzionalità delle entità nel sistema di simulazione.

## Entità

![Entity](../../static/img/04-detailed-design/entity.png)

Il _trait_ `Entity` descrive un’entità spaziale dotata di una posizione bidimensionale (`position: Point2D`), una forma
geometrica (`shape: ShapeType`), rappresentata dal tipo enumerato `ShapeType`, e un orientamento (
`orientation: Orientation`) espresso in gradi.  
Questo trait costituisce l’interfaccia di base per ogni oggetto collocato nello spazio simulato, fornendo una struttura
comune per modellare oggetti dinamici o statici.

### Posizione

La classe `Point2D` rappresenta un punto nel piano cartesiano bidimensionale. Oltre ai campi `x: Double` e `y: Double`,
essa fornisce un insieme di operazioni geometriche fondamentali:

- Somma e sottrazione vettoriale
- Moltiplicazione per scalare
- Prodotto scalare
- Modulo del vettore
- Normalizzazione del vettore
- Calcolo della distanza tra punti

Questa classe costituisce la base per il calcolo di spostamenti, direzioni e interazioni spaziali tra entità.

### Forma

La forma geometrica delle entità è definita dal tipo _enum_ `ShapeType`, che può assumere due varianti:

- `Circle(radius: Double)`: rappresenta un cerchio con raggio specificato.
- `Rectangle(width: Double, height: Double)`: rappresenta un rettangolo con larghezza e altezza definite.

Questo approccio consente una modellazione semplice ma estensibile delle dimensioni fisiche degli oggetti nello spazio
simulato.

### Orientamento

Il _trait_ `Orientation` descrive l’angolo di rotazione di un’entità rispetto a un sistema di riferimento fisso.
Contiene:

- `degrees: Double`: l’orientamento espresso in gradi.
- `toRadians: Double`: la conversione in radianti, utile per calcoli trigonometrici e trasformazioni geometriche.

L’orientamento permette di rappresentare la direzione verso cui è rivolto un oggetto nello spazio, supportando il
movimento direzionale e la rotazione.

## Entità statiche e dinamiche

![Entities](../../static/img/04-detailed-design/entities.png)

`DynamicEntity` e `StaticEntity` sono due _trait_ che estendono `Entity`.

### DynamicEntity

Il _trait_ `DynamicEntity` rappresenta un'entità in grado di muoversi e interagire con l’ambiente circostante.
Comprende:

- `sensors: SensorSuite`: un insieme di sensori che percepiscono l’ambiente
- `actuators: Seq[Actuator[? <: DynamicEntity]]`: una sequenza di attuatori che modificano lo stato dell’entità o
  dell’ambiente.

Questa struttura è pensata per simulare comportamenti robotici, in cui percezione e azione sono fortemente integrati.

<!-- TODO: behavior -->

### StaticEntity

Il _trait_ `StaticEntity` rappresenta un’entità fissa nello spazio, che non può muoversi né agire attivamente
sull’ambiente, come:

- `Obstacle`: ostacoli fissi che impediscono il movimento di entità dinamiche
- `Light`: fonti di luce che influenzano l’ambiente ma non interagiscono attivamente.

Ogni `StaticEntity` ha una forma geometrica (`shape: ShapeType`) e una posizione (`position: Point2D`) e
un orientamento (`orientation: Orientation`) coerente:

- `Obstacle`/`Boundary` sono rappresentati da un rettangolo, che può essere orientato;
- `Light` è rappresentato da un cerchio, che non ha orientamento.

> i `boundary` vengono creati da `CreationDSL.validate(insertBoundaries = true)`. Sono rettangoli sottili posizionati
> sui bordi e partecipano a collisioni/resistenza come gli ostacoli.

## Ostacoli

Gli **ostacoli** (`StaticEntity.Obstacle`) sono i “muri” dell’ambiente di simulazione:
blocchi **rettangolari** che occupano spazio, fermano i robot e interrompono il passaggio della luce. Servono a
costruire scenari realistici — dal corridoio stretto al piccolo labirinto — in cui i robot devono pianificare il
movimento ed evitare collisioni.

Ogni ostacolo ha:

- una **posizione** (`position`) e un’**orientazione** (`orientation`) nello spazio;
- due **dimensioni** (`width`, `height`);
- una **forma** coerente, esposta come `ShapeType.Rectangle(width, height)`.

In fase di **validazione** verifichiamo che le dimensioni siano **> 0**, che l’ostacolo stia **dentro i limiti**
dell’ambiente e che **non si sovrapponga** ad altre entità.

## Luce

Le **luci** (`StaticEntity.Light`) sono sorgenti **radiali**: da un punto emettono illuminazione che **decresce con la
distanza** e viene **bloccata** dagli ostacoli e robot. Sono l’ingrediente che rende
l’ambiente leggibile per futuri foto-sensori e utile per esperimenti di percezione.

Ogni luce definisce:

- un **raggio fisico** (`radius`) usato per la sua forma (`ShapeType.Circle(radius)`);
- un **raggio di illuminazione** (`illuminationRadius`) che ne delimita la portata;
- **intensità** (`intensity`) e **attenuazione** (`attenuation`) per controllare quanto e come “decade” la luce;
- un’**orientazione** presente per uniformità del modello, ma l’emissione è **isotropica** (non direzionale).

In fase di **validazione** verifichiamo che raggio, intensità e attenuazione siano **> 0** e che la luce sia
posizionata **all'interno** dell'ambiente.

## Boundary

## Robot

![Robot](../../static/img/04-detailed-design/robot.png)

Il _trait_ `Robot` estende `DynamicEntity` e rappresenta un'entità mobile autonoma nello spazio bidimensionale, in grado
di interagire con l’ambiente circostante tramite sensori e attuatori. Ogni robot ha una forma circolare
(`ShapeType.Circle`) e possiede un insieme di attuatori (`Seq[Actuator[Robot]]`) e una _suite_ di sensori (
`SensorSuite`).

Il _companion object_ fornisce un metodo `apply` per la creazione sicura di istanze tramite un sistema di validazione (
`Validation`), assicurandosi che i parametri forniti siano coerenti e privi di valori non validi (ad esempio NaN o
infiniti).

## Attuatori

![Actuator](../../static/img/04-detailed-design/actuator.png)

Un Actuator è un componente in grado di modificare lo stato di un'entità dinamica (`DynamicEntity`). Il _trait_
`Actuator[E]`
definisce un’interfaccia generica per tutti gli attuatori, attraverso il metodo `act(entity: E): Validation[E]`, che
applica un cambiamento all'entità specificata, restituendo una nuova istanza validata.

### Attuatori di movimento

Gli attuatori di movimento `WheelMotor` sono un tipo specifico di attuatori progettati per modificare la posizione e
l'orientamento
di un'entità dinamica nello spazio simulato. Questi attuatori sono implementati come sottotipi di `Actuator[Robot]`,
consentendo loro di agire specificamente su istanze del trait `Robot`.

Un `WheelMotor` è costituito da due ruote (`Wheel`) – sinistra e destra – ognuna dotata
di una velocità lineare (`speed`) e una forma circolare (`ShapeType.Circle`).

L'implementazione `DifferentialWheelMotor` utilizza un modello fisico di tipo differenziale, in cui il movimento viene
calcolato in base alla velocità delle due ruote e alla distanza tra esse (assunta pari al diametro del robot). In
particolare:

- la velocità lineare del robot è la media delle velocità delle due ruote
- la velocità angolare è proporzionale alla differenza di velocità tra le ruote
- la nuova posizione e orientazione vengono calcolate utilizzando le equazioni cinematiche del moto in un piano.

Questa logica è incapsulata nel metodo `act(robot: Robot): Validation[Robot]`, che restituisce una nuova istanza del
robot
con posizione e orientamento aggiornati.

### Azioni

L’enumerazione `Action` definisce un insieme predefinito di comandi che il robot può eseguire, come:

- `MoveForward`: muove il robot in avanti
- `MoveBackward`: muove il robot all'indietro
- `TurnLeft`: ruota il robot verso sinistra
- `TurnRight`: ruota il robot verso destra
- `Stop`: arresta il movimento del robot.

Ogni `Action` è caratterizzata da una coppia di velocità da applicare rispettivamente alla ruota sinistra e destra.
Un’estensione dell’enumerazione consente di applicare direttamente un’azione al robot (`applyTo(robot: Robot): Robot`),
modificando la configurazione dei `WheelMotor` e aggiornando così lo stato del robot.

L’estensione `move` disponibile su `Robot` permette poi di calcolare l'effetto dell’attuatore aggiornato, producendo il
movimento vero e proprio del robot nello spazio simulato.

## Sensori

![Sensor](../../static/img/04-detailed-design/sensor.png)

I sensori sono componenti che permettono a un'entità dinamica di percepire l'ambiente circostante. Il _trait_
`Sensor[Entity, Environment]` definisce un'interfaccia generica per i sensori.
I sensori sono parametrizzati su due tipi:

- `Entity`: il tipo di entità che il sensore può percepire, sottotipo di `DynamicEntity` (ad esempio, `Robot`).
- `Environment`: il tipo di ambiente in cui il sensore opera, sottotipo di `Environment` (ad esempio, `Environment`
  stesso).
- `Data`: il tipo di dato restituito dal sensore.

Inoltre i sensori contengono un campo `offset` che rappresenta la posizione del sensore rispetto all'entità che lo possiede.

Infine un metodo `sense[F[_]](entity: Entity, env: Environment): F[Data]` che permette di ottenere i dati di rilevamento
dal sensore.
Il tipo `F[_]` è un tipo di effetto generico (come `IO`, `Task`, etc.) che permette:

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

### Sensori di prossimità

La _case class_ `ProximitySensor` estende `Sensor[Robot, Environment]` e rappresenta un sensore di prossimità che rileva
la presenza di altre entità nell'ambiente.
Questo sensore dispone di un campo `range` che rappresenta il raggio di azione del sensore.
I valori ritornati da questo sensore sono di tipo `Double`, che rappresenta la distanza alla quale si trova l'entità più
vicina, normalizzata tra 0 e 1, dove 0 indica che l'entità è molto vicina e 1 che è molto lontana.
Il metodo `sense` implementa la logica di rilevamento, tramite _Ray Casting_, che calcola la distanza tra il sensore e
le entità nell'ambiente, restituendo il valore normalizzato.

### Sensori di luce

La _case class_ `LightSensor` estende `Sensor[Robot, Environment]` e rappresenta un sensore di luce, in grado di rilevare l'intensità luminosa in una determinata area.
Anche in questo caso i valori restituiti dal sensore sono `Double`, e rappresentano l'intensità luminosa normalizzata tra 0 e 1, dove 0 indica assenza di luce e 1 indica luce massima.
La distribuzione della luce nell'ambiente è rappresentata da un campo `lightField` all'interno di `Environment`, il metodo `sense` implementa la logica di rilevamento, utilizzando il `lightField` per ottenere i dati di intensità luminosa.

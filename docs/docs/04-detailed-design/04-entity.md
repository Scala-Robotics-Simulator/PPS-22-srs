---
sidebar_position: 4
---

# Entity

Il trait `Entity` descrive un’entità spaziale che possiede una posizione bidimensionale (`position: Position2D`), una
forma (`shape: ShapeType`), rappresentata dall’enumerazione ShapeType e un orientamento (`orientation: Orientation`)
espresso in gradi.
Questo trait costituisce l’interfaccia di base per ogni oggetto posizionato nello spazio, fornendo una struttura che
permette di modellare facilmente oggetti dinamici o statici in uno spazio bidimensionale.

![Entity](../../static/img/04-detailed-design/entity.png)

![Entities](../../static/img/04-detailed-design/entities.png)

## Sensori

Il trait `DynamicEntity` include un campo `sensors: SensorSuite`, che rappresenta un insieme di sensori associati all'entità.
`SensorSuite` è un trait che definisce un insieme di sensori, per facilità di utilizzo, ogni tipologia di sensore è specificata all'interno di un campo dedicato.
Il trait `SensorSuite` contiene un metodo `sense(entity)(environment): SensorReadings`, che permette di ottenere una lettura dei sensori per l'entità specificata nell'ambiente di simulazione.
`SensorReading` contiene il riferimento al sensore che ha effettuato la lettura, oltre al valore della lettura stessa.
I `Sensor` sono definiti come un trait che implementa il metodo `sense(entity)(environment): SensorReading`, permettendo di ottenere letture specifiche per ogni tipo di sensore, entity ed environment sono generici e sottotipi rispettivamente dei trait `DynamicEntity` e `Environment`.

![Sensor](../../static/img/04-detailed-design/sensor.png)

## Sensore di Prossimità

Il sensore di prossimità è un tipo di sensore che rileva la presenza di altre entità vicine. Si tratta del trait `ProximitySensor`, che estende il trait `Sensor`.
Il metodo `sense(entity)(environment): Double` restituisce la distanza tra il sensore e l'entità più vicina nell'ambiente di simulazione. Questa distanza viene calcolata tramite `rayCasting`, che verifica se, entro il `range` del sensore, è presente un'altra entità. Il valore restituito è normalizzato tra 0 e 1, dove 0 indica che l'entità è molto vicina e 1 indica che non ci sono entità nel raggio di azione del sensore.

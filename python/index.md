# Obiettivo del progetto

L'obiettivo del progetto è estendere il simulatore 2D di robot per dotarlo di comportamenti appresi tramite tecniche di Reinforcement Learning, in particolare Q-learning e Deep Q-learning (DQN).

Si è proposto di replicare i seguenti tre comportamenti:
- **phototaxis**: movimento verso una sorgente luminosa;
- **obstacle avoidance**: movimento con evitamento di ostacoli;
- **exploration**: massimizzare l’esplorazione dell’ambiente evitando le collisioni.

# Vincoli

I vincoli posti sono:

- agenti: da 1 a 30 robot (raggio 0.25 m);
- ambiente: griglie 5×5, 20×20 e 30×30 m;
- sensori: 8 prossimità + 8 luce + posizione + orientazione;
- luci con raggio 0.2 m e irradiazione 5 m;
- ostacoli rettangolari con dimensioni variabili.

# Starting point

Siamo partiti da un simulatore in grado di gestire un ambiente composto da N robot aventi un comportamento specificato da configurazione.

Nell'ambiente sono presenti entità statiche, come ostacoli e luci e entità dinamiche, come altri robot.
I robot sono in grado di percepire le altre entità attraverso i sensori di prossimità e di luce e possono interagire nell''ambiente attraverso gli attuatori tramite comportamenti pre-programmati. Il movimento dei robot segue un movimento differenziale in cui posono essere applicate delle velocità diverse alle due ruote.


## Configurazione del simulatore (yaml)

Per configurare il simulatore si possono utilizzare o file `yaml` oppure la GUI dedicata.

L'utilizzo di file `yaml` permette di configurare il simulatore in modo semplice ed efficace. Strutturato come segue:

```yaml
simulation:
  seed: 42
environment:
  width: 12
  height: 10
  entities:
    - light:
        position: [10, 5]
        illuminationRadius: 6.0
        intensity: 1.0
        radius: 0.2
        attenuation: 1.0
    - obstacle:
        position: [6, 5]
        orientation: 0.0
        width: 2.0
        height: 6.0
    - robot:
        position: [2, 2]
        orientation: 45.0
        radius: 0.25
        speed: 1.0
        withProximitySensors: true
        withLightSensors: true
        behavior: Phototaxis
```

I file di configurazione si utilizzano durante l'addestramento e la valutazione degli agenti per configurare il simulatore.

## Adattamento e modellazione del simulatore

L'idea è quella di estendere il simulatore integrando comportamenti basati su tecniche di Q-learning e Deep Q-learning per rendere i robot autonomi.

Il simulatore è stato implementato interamente in `Scala`, mentre la parte di Reinforcement Learning è stata implementata in `Python`.

### Comunicazione python-simulatore

Per il collegamento tra i due linguaggi è stato adottato `gRPC`. Inoltre, è stato definita un’interfaccia di interazione modellata sullo stile di `PettingZoo`, così da mantenere coerenza con le principali librerie Reinforcement Learning multi-agente.

---

### Simulatore lato Scala

Di seguito viene fornita una descrizione delle modifiche apportate implementativamente al simulatore.

#### Agente

Invece che apportare delle modifiche ai robot esistenti si è preferito creare delle entità ad-hoc: gli agenti.
Nel simulatore in Scala, l'agente ha le stesse caratteristiche del robot ad eccezione del `Behavior` in cui non associamo più un comportamento programmatico ma adesso è composto da `Reward`, `Termination` e `Truncation`.

#### Reward

La `Reward` è una funzione associata all'agente che permette di osservare lo stato dell'ambiente (precedente e corrente) e calcolare una ricompensa (bonus o penalità) adeguata al task che deve risolvere.

Signature per la reward:

```scala
def evaluate(prev: BaseState, current: BaseState, entity: Agent, action: Action[?]): Double
```

PS: nel task di Exploration si è reso necessario aggiungere della memorizzazione # TODO

#### Termination

La `Termination` permette di concludere in maniera naturale la finestra di addestramento sia come situazione positiva o negativa.
Serve ad avere una definition of done al di fuori del numero massimo di step, se definiti.
È stata modellata sulla base del task specifico da addestrare come:

- collisione dell'agente contro un ostacolo, che sia muro o oggetto.
- distanza minima da una fonte di luce.

La Signature per la termination viene definita come:

```scala
def evaluate(prev: BaseState, current: BaseState, entity: Agent, action: Action[?]): Boolean
```

#### Truncation

La `Truncation` è stata realizzata allo stesso modo della `Termination`, ma è stata meno utilizzata rispetto a quella descritta sopra in quanto la gestione del tempo è stata effettuata in python.

---

### Lato Python

Di seguito sono descritte tutte le aggiunte effettate in `python`.
Innanzitutto, è stato creato l'ambiente osservabile dall'agente, la modellazione dell'agente stesso con conseguente implementazione dell'algoritmo di training e valutazione.

#### Descrizione dell'ambiente

L'ambiente è stato modellato sulla base della libreria `gymnasium`, contiene quindi i metodi:

- step:

  ```python
  def step(self, actions: dict) -> tuple[dict, dict, dict, dict, dict]
  ```
  
  fa svolgere le azioni fornite agli agenti, effettuando un `tick` nel simulatore e restituisce quindi, per ogni agente:

  - osservazioni;
  - ricompense;
  - terminazioni;
  - troncamenti;
  - informazioni aggiuntive.
  
- render:
  ```python
  def render(self, width: int = 800, height: int = 600) -> np.ndarray
  ```

  ritorna un immagine in formato RGB delle dimensioni indicate, contenente una rappresentazione dello stato del simulatore al momento della chiamata.

- reset:

  ```python
  def reset(self, seed: int = 42) -> tuple[dict, dict]:
  ```

  riporta il simulatore alla configurazione iniziale e ritorna, per ogni agente:

  - osservazioni;
  - informazioni aggiuntive.

Sono presenti, inoltre, i metodi `init` e `close` utili a inizializzare l'ambiente con la configurazione e chiudere la connessione con `gRPC`. E' 

#### Agente
Q-agent
DQ-agent

In python invece, dell'agente sono presenti due varianti: il `QAgent` e il `DQAgent`.

---

# Task

I task da svolgere sono:

- **phototaxis**: generazione dell'agente in un punto casuale della mappa con l'obiettivo di raggiungere la prima luce disponibile cercando di evitare muri.In caso l'agente non rilevi valori con il sensore di luce deve entrare in uno stato di esplorazione.
- **obstacle avoidance**: generazione dell'agente in un punto casuale dell'ambiente. L'obiettivo è quello di muoversi nello spazio senza toccare ostacoli e muri in un certo tempo definito.

- **exploration**: mgenerazione dell'agente in un punto casualee deell'ambiente. L'obiettivo è quello di massimizzare la copertura visitata dell'ambiente cercando di evitare gli ostacoli entro un certo numero di steps.

## Modus operandi
### Env generation
è stata utile perchè
### Training
come viene svolta
### Evaluation

La valutazione delle performance dei `QAgent` e `DQAgent` viene effettuata tramite la funzione `evaluate`, la cui signature è la seguente:

```python
def evaluate(
    env: PhototaxisEnv | ObstacleAvoidanceEnv | ExplorationEnv,
    agents: dict[str, QAgent | DQAgent],
    configs: list[str],
    max_steps: int,
    did_succeed: Callable[[float, bool, bool], bool],
    window_size: int = 100,
) -> dict
```

Viene passato l'`environment` specifico per il task, oltre a un dizionario con gli agenti e i relativi id.
Questi vengono valutati su tutte le configurazioni fornite in `configs` per un numero di passi pari a `max_steps`.
La _lambda_ `did_succeed` permette di capire se l'agente ha terminato l'episodio con un successo o un fallimento.
Il parametro `window_size` permette di calcolare la `moving average reward`.

La funzione supporta nativamente scenari **multi-agente** (da 1 a $N$) utilizzando dizionari per gestire stati e azioni indipendenti, isolando correttamente gli agenti che terminano l'episodio prima degli altri. Durante la valutazione la policy è deterministica (*greedy*).

Le metriche restituite sono essenziali per diagnosticare la qualità dell'apprendimento:
*   **`success_rate`** e **`successes_idx`**: indicano la robustezza dell'agente e quali specifiche configurazioni riesce a risolvere.
*   **`median_steps_to_success`**: misura l'efficienza (velocità) nel raggiungere l'obiettivo.
*   **`total_rewards`** e **`moving_avg_reward`**: valutano la performance cumulativa e la stabilità del comportamento durante l'episodio.
*   **`td_losses`** (per DQAgent): monitora l'errore di stima dei valori $Q$, utile per rilevare incertezze o problemi di generalizzazione su stati non visti in training.


## Obstacle Avoidance
## Phototaxis
## Exploration

# Conclusioni

## Commenti finali




# Command Line Interface (CLI)

La modalità **CLI** permette di avviare la simulazione senza interfaccia grafica, interagendo esclusivamente tramite la console.  
Tutti i parametri necessari possono essere forniti da linea di comando o inseriti interattivamente dall’utente.

A differenza della **GUI**, in modalità **CLI**:

- la simulazione viene eseguita alla **massima velocità**;
- al termine dell’esecuzione, viene mostrato lo **stato finale dell’ambiente in formato testuale** e altre informazioni riguardanti la simulazione e i robot.

## Avvio della simulazione

La simulazione può essere avviata in modalità CLI aggiungendo il flag `--headless` al seguente comando:

```bash
  java -jar PPS-22-srs.jar --headless [options]
```

### Opzioni
Le opzioni disponibili sono:
- `--path <file>`: specifica il percorso del file di configurazione _YAML_;
- `--duration <milliseconds>`: imposta la durata totale della simulazione;
- `--seed <number>`: definisce il seme casuale per garantire riproducibilità;
- `--help`: mostra le istruzioni disponibili;
- `--version`: mostra la versione dell’applicazione.

Se non viene specificato il percorso, la durata o il seed, questi verranno richiesti interattivamente all’avvio della simulazione.

### Esempio di avvio
Esempio di avvio del simulatore in modalità CLI con parametri specifici:
```bash
  java -jar PPS-22-srs.jar --headless --path config.yaml --duration 60000 --seed 42
```

:::tip Esempio di risultato della simulazione
Al termine della simulazione, viene mostrato lo stato finale dell’ambiente e dei robot in console:
```plaintext
Simulation finished. Resulting state:
--- SimulationState ---
Simulation Time : 60000 ms
Elapsed Time    : 60100 ms
Δt              : 100 ms
Speed           : SUPERFAST
RNG Seed        : SimpleRNG(42)
Status          : ELAPSED_TIME

--- Environment ---
Width: 10
Height: 10

--- Robot ---
ID: 0775ac24-a8ef-458f-ac4a-1f893beda093
Position: (1.7868356190279204, 2.3963280715807955)
Orientation: 225.17660771972294°
Shape: Circle with radius 0.25
Actuators:
  DifferentialWheelMotor -> Left: 0.4726367489629125, Right: 1.0
Sensors:
  Proximity (offset: 0.0°, range: 1.0 m) -> 1.0
  Proximity (offset: 45.0°, range: 1.0 m) -> 1.0
  Proximity (offset: 90.0°, range: 1.0 m) -> 0.029314208030667692
  Proximity (offset: 135.0°, range: 1.0 m) -> 1.0
  Proximity (offset: 180.0°, range: 1.0 m) -> 1.0
  Proximity (offset: 225.0°, range: 1.0 m) -> 1.0
  Proximity (offset: 270.0°, range: 1.0 m) -> 1.0
  Proximity (offset: 315.0°, range: 1.0 m) -> 1.0
  Light (offset: 0.0°) -> 0.07969634096070385
  Light (offset: 45.0°) -> 0.9476652878063021
  Light (offset: 90.0°) -> 0.9543945816048541
  Light (offset: 135.0°) -> 0.919664988925314
  Light (offset: 180.0°) -> 0.8846272514441557
  Light (offset: 225.0°) -> 0.0
  Light (offset: 270.0°) -> 0.0
  Light (offset: 315.0°) -> 0.0

--- Robot ---
ID: 84b2f2ad-b7f7-4fb4-b8a8-a2d558b36bc9
Position: (2.270059873322035, 4.178978578605012)
Orientation: 268.11838177274535°
Shape: Circle with radius 0.25
Actuators:
  DifferentialWheelMotor -> Left: 1.0, Right: 0.5080782332544016
Sensors:
  Proximity (offset: 0.0°, range: 1.0 m) -> 1.0
  Proximity (offset: 45.0°, range: 1.0 m) -> 1.0
  Proximity (offset: 90.0°, range: 1.0 m) -> 1.0
  Proximity (offset: 135.0°, range: 1.0 m) -> 1.0
  Proximity (offset: 180.0°, range: 1.0 m) -> 1.0
  Proximity (offset: 225.0°, range: 1.0 m) -> 1.0
  Proximity (offset: 270.0°, range: 1.0 m) -> 1.0
  Proximity (offset: 315.0°, range: 1.0 m) -> 1.0
  Light (offset: 0.0°) -> 0.5032820427990032
  Light (offset: 45.0°) -> 0.590965695886743
  Light (offset: 90.0°) -> 0.553494533424852
  Light (offset: 135.0°) -> 0.27297622906554214
  Light (offset: 180.0°) -> 0.0
  Light (offset: 225.0°) -> 0.0
  Light (offset: 270.0°) -> 0.07430186024527663
  Light (offset: 315.0°) -> 0.5979437728212829
```
```plaintext
Resulting environment:
-- | -- | -- | -- | -- | -- | -- | X  | -- | -- ||
-- | -- | -- | -- | -- | -- | -- | X  | -- | -- ||
-- | P  | ** | -- | -- | -- | -- | X  | -- | -- ||
-- | -- | -- | -- | -- | -- | -- | X  | -- | -- ||
-- | -- | P  | X  | X  | X  | -- | X  | -- | -- ||
-- | -- | -- | -- | -- | -- | -- | X  | -- | -- ||
-- | -- | -- | -- | -- | -- | -- | X  | -- | -- ||
-- | -- | -- | -- | -- | -- | -- | -- | -- | -- ||
-- | -- | -- | -- | -- | -- | -- | -- | ** | -- ||
-- | -- | -- | -- | -- | -- | -- | -- | -- | --
```
:::
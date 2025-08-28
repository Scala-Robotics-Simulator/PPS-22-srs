---
sidebar_position: 6
---

# Configuration

Il package `io.github.srs.config` fornisce le classi necessarie per gestire la configurazione della simulazione.
Questa viene utilizzata per permettere di salvare e caricare le impostazioni della simulazione, come ad esempio il seed e la durata della simulazione, oltre a configurare le caratteristiche dell'ambiente di simulazione, ovvero la dimensione e il numero di entità presenti.
Le operazioni di salvataggio e caricamento sono gestite dal trait `ConfigManager`, un'interfaccia agnostica che permette di gestire la configurazione in modo indipendente dal formato di salvataggio, anche se al momento è possibile utilizzare solamente il formato YAML.
Le operazioni che vengono effettuate sono le seguenti:

- Caricamento (`load`) della configurazione da un file YAML.
- Salvataggio (`save`) della configurazione su un file YAML.

Durante l'esecuzione della funzione `load`, viene effettuato un controllo sulla validità della configurazione, per garantire che i parametri siano corretti e che non vi siano errori di formattazione o valori non validi.

![Configuration](../../static/img/04-detailed-design/configuration.png)

## Architettura

L'architettura di `ConfigManager` adotta un approccio _funzionale e parametrico_ basato su `F[_]`, rendendo il codice agnostico rispetto all'effetto utilizzato per l'esecuzione (es. `IO`, `SyncIO`, `Task`).
L'implementazione prevede:

- `ConfigManager[F[_]]`: un'interfaccia che definisce le operazioni di caricamento e salvataggio della configurazione.
- `YamlConfigManager[F[_]]`: un'implementazione di `ConfigManager` che utilizza il formato YAML per la configurazione.
- Utility di parsing e serializzazione (`YamlManager`, `YamlSimulationConfigParser`, `YamlSimulationConfigSerializer`) che gestiscono la conversione tra oggetti Scala e rappresentazioni YAML.
- `ConfigError` e `ConfigResult[A]`: tipi per gestire gli errori di configurazione e i risultati delle operazioni di caricamento e salvataggio.

### Tagless Final Pattern

Il `ConfigManager[F[_]]` segue il _Tagless Final Pattern_: le operazioni sono parametrizzate su un tipo di effetto `F[_]` e vincolate solo alle capacità necessarie, ad esempio `Sync` e `Files` per `YamlConfigManager[F[_]]`.
I vincoli di tipo (`Sync` di _cats-effect_ e `Files` di _fs2_) definiscono le capacità richieste — sospendere side-effect e interagire con il file system — senza imporre implementazioni concrete.
Questo approccio consente di:

- indipendenza dal tipo di effetto specifico utilizzato per l'esecuzione.
- migliore testabilità tramite interpreti fittizi o mock.
- separazione netta tra la definizione dell'algebra (`ConfigManager`) e le implementazioni concrete (`YamlConfigManager`).

## Gestione degli Errori

La gestione degli errori durante il caricamento della configurazione è stata gestita tramite il tipo `ConfigResult[A]`, che rappresenta il risultato di un'operazione di configurazione e può essere un successo (`Right`) o un fallimento (`Left`), in quanto non è altro che un alias per `Either[Seq[ConfigError], A]`.

I tipi di errore sono definiti in `ConfigError`, che include:

- `MissingField`: un campo obbligatorio mancante.
- `ParsingError`: un errore durante il parsing del file di configurazione, tipicamente causato dall'utilizzo di chiavi non valide.
- `InvalidType`: un tipo di dato non valido per un campo specifico.

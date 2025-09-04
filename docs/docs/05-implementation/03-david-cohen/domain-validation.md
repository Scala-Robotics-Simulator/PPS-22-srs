---
sidebar: 1
---

# Validation

Nel modello **Domain-Driven**, la **validazione** separa i dati **grezzi** dagli oggetti di **dominio validi**. Questo
previene che il sistema raggiunga stati incoerenti.

La convalida è modellata con `Either` (alias `Validation[A]`) per un comportamento **fail-fast**: a sinistra (`Left`) un
**`DomainError`** esplicito, a destra (`Right`) un valore già valido. Questo rende l'uso "sicuro" evidente a
_compile-time_.

## Architettura

### Tipi e alias

- **`Validation[A]`**: un alias per `Either[DomainError, A]`, che rende il contratto di convalida chiaro.
- **`DomainError`**: un ADT (`sealed trait`) che enumera le violazioni delle **invarianti** del modello, come:
  - `Infinite`: valori infiniti (es. `Double.PositiveInfinity`);
  - `OutOfBounds`: entità fuori dalla mappa;
  - `Collision`: collisioni tra entità.

Ogni errore espone un messaggio _human-friendly_ per la UI.

### Smart constructors

Gli _invarianti locali_ (es. `ScaleFactor`) sono gestiti da _smart constructors_ che restituiscono
`Validation[...]`, garantendo che non si possano creare valori non validi senza passare per la logica di convalida.

### ValidEnvironment

`Environment` è la rappresentazione _grezza_ dello stato, mentre `ValidEnvironment` è un _opaque type_ che può essere
ottenuto _solo_ dopo un'accurata validazione. Il core della simulazione opera esclusivamente su istanze di
`ValidEnvironment`.

:::info
Per maggiori dettagli sull'implementazione, vedere [Implementazione di Environment e ValidEnvironment](./../02-simone-ceredi/1-environment.md).
:::

## Politiche di gestione degli errori

- **Dominio**: vengono usate le monadi `Either` per un approccio _fail-fast_. La validazione si ferma al primo
  `DomainError` riscontrato, garantendo un comportamento semplice e deterministico.
- **Parsing/Configurazione**: gli errori di formato (es. YAML) sono accumulati usando `Either[Seq[ConfigError], A]`,
  permettendo alla UI di mostrare più problemi contemporaneamente.
- **UI Mapping**: ogni `DomainError` include un `message` pronto per l'uso nella GUI, fornendo un feedback
  immediato e utile.

## Integrazione nel flusso

Il flusso end-to-end della simulazione è una **pipeline**:

1. **parsing** del file _YAML_;
2. **validazione del dominio** tramite `env.validate`, che garantisce gli invarianti semantici;
3. **configurazione della simulazione**, dove il core lavora solo con un `ValidEnvironment` validato.

> Nota: questo raccorda la _Configuration_, che si occupa del formato dei dati, con il _dominio_, che ne garantisce la
> semantica, e il _core_, che opera solo su dati sicuri e consistenti.

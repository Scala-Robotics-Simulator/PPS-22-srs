---
sidebar: 1
---

# Validation

Nel modello **Domain-Driven**, la **validazione** separa i dati **grezzi** dagli oggetti di **dominio validi**. Questo
previene che il sistema raggiunga stati incoerenti.

La convalida è modellata con `Either` (alias `Validation[A]`) per un comportamento **fail-fast**: a sinistra (`Left`) un
**`DomainError`** esplicito, a destra (`Right`) un valore già valido. Questo rende l'uso "sicuro" evidente a
*compile-time*.

## Architettura

### Tipi e Alias

* **`Validation[A]`**: un alias per `Either[DomainError, A]`, che rende il contratto di convalida chiaro.
* **`DomainError`**: un ADT (`sealed trait`) che enumera le violazioni delle **invarianti** del modello, come:
    * `InvalidDimension`: dimensioni sotto i minimi.
    * `OutOfBounds`: entità fuori dalla mappa.
    * `Overlap`: collisioni tra entità.

Ogni errore espone un messaggio *human-friendly* per la UI.

### Smart Constructors

Gli _invarianti locali_ (es. `ScaleFactor`) sono gestiti da _smart constructors_ che restituiscono
`Validation[...]`, garantendo che non si possano creare valori non validi senza passare per la logica di convalida.

### ValidEnvironment

`Environment` è la rappresentazione *grezza* dello stato, mentre `ValidEnvironment` è un *opaque type* che può essere
_ottenuto _solo_ dopo un'accurata validazione. Il core della simulazione opera esclusivamente su istanze di_
`ValidEnvironment`.

## Politiche di Gestione degli Errori

* **Dominio**: Vengono usate le monadi `Either` per un approccio _fail-fast_. La validazione si ferma al primo
  `DomainError` riscontrato, garantendo un comportamento semplice e deterministico.
* **Parsing/Configurazione**: Gli errori di formato (es. YAML) sono accumulati usando `Either[Seq[ConfigError], A]`,
  permettendo alla UI di mostrare più problemi contemporaneamente.
* **UI Mapping**: Ogni `DomainError` include un `message` pronto per l'uso nella GUI o nella CLI, fornendo un feedback
  immediato e utile.

## Integrazione nel Flusso

Il flusso end-to-end della simulazione è una **pipeline** rigorosa:

1. **Parsing** del file YAML;
2. **Validazione del dominio** tramite `env.validate`, che garantisce gli invarianti semantici;
3. **Configurazione della simulazione**, dove il core lavora solo con un `ValidEnvironment` validato.

> Questo raccorda la *Configuration*, che si occupa del formato dei dati, con il *dominio*, che ne garantisce la
semantica, e il *core*, che opera solo su dati sicuri e consistenti.
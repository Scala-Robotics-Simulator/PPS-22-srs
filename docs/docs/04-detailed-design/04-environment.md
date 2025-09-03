---
sidebar_position: 4
---

# Environment

La case class `Environment`, estende il trait `EnvironmentParameters` e rappresenta l'ambiente di simulazione in cui le
entità interagiscono. Contiene:

- `entities: Set[Entity]`: l’insieme delle entità presenti;
- `width: Int` e `height: Int`: dimensioni dello spazio simulato in cui le entità possono muoversi;
  `lightField: LightField`: campo di illuminazione corrente dell’ambiente, ricalcolato a ogni tick dal motore di
  illuminazione.

Ai bordi dell’ambiente sono presenti `Boundary`, entità statiche che impediscono alle entità dinamiche di uscire dallo
spazio di simulazione.

![Environment](../../static/img/04-detailed-design/environment.png)

## ValidEnvironment

Tramite un metodo `validate` è possibile ottenere un `ValidEnvironment`, che rappresenta un ambiente di simulazione
valido.

<!-- TODO: aggiungere collegamento a validation -->

> È possibile creare un `ValidEnvironment` **solo** tramite `validate`, così da garantire la correttezza dell’ambiente.

I controlli di validazione includono:

- `width` e `height` in intervalli validi;
- posizionamento delle entità entro i limiti (`width` × `height`);
- assenza di sovrapposizioni tra entità;
- limite massimo al numero di entità;
- validità intrinseca di ciascuna entità.

:::info

I dettagli di implementazione dell'ambiente sono disponibili nella
sezione [Implementazione dell'ambiente di simulazione](../05-implementation/02-simone-ceredi/1-environment.md).

:::

## Illuminazione

Il `lightField` modella l’illuminazione dell’ambiente ed è ricalcolato a ogni tick dal motore di illuminazione, in
funzione delle entità statiche e dinamiche (ad es. robot e ostacoli).
Per maggiori dettagli consultare [la gestione dell’illuminazione](../04-detailed-design/09-illumination.md).
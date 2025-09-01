---
sidebar_position: 4
---

# Environment

La case class `Environment`, estende dal trait `EnvironmentParameters` e rappresenta l'ambiente di simulazione in cui le entità interagiscono. Esso contiene un campo `entities: Set[Entity]`, che rappresenta l'insieme delle entità presenti nell'ambiente.
L'ambiente ha una larghezza (`width: Int`) e un'altezza (`height: Int`), che definiscono le dimensioni dello spazio in cui le entità possono muoversi.
Ai bordi dell'ambiente si trovano dei `Boundary`, si tratta di entità statiche che impediscono alle entità dinamiche di uscire dallo spazio di simulazione.
`LightField` è utilizzato per ottenere l'illuminazione dell'ambiente.

![Environment](../../static/img/04-detailed-design/environment.png)

## ValidEnvironment

Tramite un metodo `validate` è possibile ottenere un `ValidEnvironment`, che rappresenta un ambiente di simulazione valido.

<!-- TODO: aggiungere collegamento a validation -->

È possibile creare un `ValidEnvironment` solamente tramite il metodo `validate`. Questo permette di garantire che l'ambiente sia valido e conforme ai requisiti definiti.

I controlli effettuati durante la validazione vanno a:

- controllare che `width` e `height` siano in un intervallo valido;
- controllare che le entità siano posizionate all'interno dei limiti definiti da `width` e `height`;
- verificare che le entità non si sovrappongano tra loro;
- verificare che non vi siano una quantità eccessiva di entità;
- verificare che tutte le entità siano valide.

:::info

I dettagli di implementazione dell'ambiente sono disponibili nella sezione [Implementazione dell'ambiente di simulazione](../05-implementation/02-simone-ceredi/1-environment.md).

:::

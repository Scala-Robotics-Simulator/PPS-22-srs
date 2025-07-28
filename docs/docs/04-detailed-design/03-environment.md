---
sidebar_position: 3
---

# Environment

Il trait `Environment` rappresenta l'ambiente di simulazione in cui le entità interagiscono. Esso contiene un campo `entities: Set[Entity]`, che rappresenta l'insieme delle entità presenti nell'ambiente.
L'ambiente ha una larghezza (`width: Int`) e un'altezza (`height: Int`), che definiscono le dimensioni dello spazio in cui le entità possono muoversi.
Ai bordi dell'ambiente si trovano dei `Boundary`, si tratta di entità statiche che impediscono alle entità dinamiche di uscire dallo spazio di simulazione.

![Environment](../../static/img/04-detailed-design/environment.png)

## Validazione dell'ambiente

Quando l'ambiente viene creato, viene eseguita una validazione per assicurarsi che le entità siano posizionate correttamente all'interno dei limiti definiti da `width` e `height`.
Inoltre viene verificato che le entità non si sovrappongano tra loro, per garantire che ogni entità abbia uno spazio unico e definito all'interno dell'ambiente.

---
sidebar_position: 2
---

# David Cohen

L’implementazione realizzata si concentra principalmente sulle seguenti funzionalità:

- [Implementazione della validazione di dominio](./validation.md):
  - definizione della validazione di domino.
  - implementazione di regole di validazione per le entità.
- [Implementazione delle entità statiche](../../04-detailed-design/05-entity.md#StaticEntity)
    - Luci
    - Ostacoli
- [Implementazione del motore di illuminazione](./light-engine.md)
    - Diffusione della luce
    - Calcolo del campo luminoso
- [Implementazione dei behavior](./behavior.md)
    - definizione di behavior
    - implementazione del dsl per la creazione di behavior singoli e composti
    - implementazione della politica di selezione dei behavior
    - implementazione di comportamenti predefiniti:
      - comportamento di evitamento ostacoli
      - comportamento di inseguimento luce
      - comportamento di esplorazione
      - comportamento combinato
- [Implementazione della GUI di simulazione](./simulation-gui.md)
    - visualizzazione dell'ambiente di simulazione
    - visualizzazione dello stato delle entità
    - controllo della simulazione (avvio, pausa, reset)
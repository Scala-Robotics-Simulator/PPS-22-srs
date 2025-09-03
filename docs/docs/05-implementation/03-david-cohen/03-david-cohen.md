---
sidebar_position: 2
---

# David Cohen

L’implementazione realizzata si concentra principalmente sulle seguenti aree:

- [Validazione di dominio](./domain-validation.md)
    - definizione del modello di *validazione*;
    - regole di validazione per entità e ambiente.
- [Entità statiche](../../04-detailed-design/05-entity.md#staticentity)
    - definizione di fonti di luce;
    - definizione di ostacoli.
- [Motore di illuminazione](./illumination.md)
    - diffusione della luce (pipeline FOV/occlusione, interpolazione);
    - calcolo e combinazione del campo luminoso.
- [Comportamenti delle entità dinamiche](./behaviors.md)
    - definizione dei behavior e DSL di composizione (parziali e totali);
    - politica di selezione dei behavior e comportamenti predefiniti:
        - _obstacle avoidance_;
        - _phototaxis_;
        - _exploration_ (random walk);
        - _composizione prioritaria_ (fallback gerarchico).
- [GUI di simulazione](./simulation-gui.md)
    - visualizzazione dell’ambiente di simulazione;
    - selezione del robot sia da lista sia da canvas con evidenziazione ed esposizione dettagli
    - controlli ciclo di vita e velocità della simulazione (avvio, pausa, arresto/velocità).
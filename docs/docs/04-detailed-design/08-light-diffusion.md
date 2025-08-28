---
sidebar_position: 8
---

# Light Diffusion

Il sottosistema di **light diffusion** calcola quanta luce (`Lux`) raggiunge ogni cella dell’ambiente, considerando
**ostacoli**, **boundary** e, opzionalmente, i **robot**. La pipeline è intenzionalmente semplice: usiamo un **FOV**
(campo visivo) basato su **SquidLib** per stimare la *visibilità* cella-per-cella, applichiamo **attenuazione** in
funzione della distanza e **sommiamo** i contributi delle sorgenti.

## SquidLib

I motivi principali per cui abbiamo scelto **SquidLib** per il calcolo del FOV sono:

* **Griglia**: **SquidLib** lavora su una griglia di celle 2D, adatta al nostro ambiente
  simulato.
* **Fisica semplice**: la fisica della luce è semplificata, con una visibilità binaria (cella visibile o no) e
  attenuazione lineare.
* **Performance**: è ottimizzata per calcoli rapidi su griglie.
* **Integrazione**: si integra con il nostro modello di entità e ambiente, senza richiedere complessi
  adattamenti.
* **API minimale**: l’API è semplice e diretta, permettendo di concentrarci sulla logica di diffusione senza
  complicazioni. Usiamo unicamente il metodo `FOV.compute(...)` per ottenere la visibilità cella-per-cella.

## Architettura

* **Resistance map**: matrice `Array[Array[Double]]` con 0.0 = trasparente, 1.0 = opaco.
  È derivata da `EnvironmentView`: ostacoli e boundary diventano celle solide; nella *vista dinamica* anche i robot
  possono bloccare la luce.
* **FovEngine**: dato `resistance`, **origine** (cella della luce) e **raggio**, produce un `ArraySeq[Double]` di *
  *visibilità** ∈ `[0,1]` per cella. Implementazione attuale: `SquidLibFov`.
* **Diffuser\[V, S]**: interfaccia per applicare un passo di diffusione (`step(view)(state)`) e interrogare
  l’intensità (`intensityAt(state)(cell)`).
* **ShadowFovDiffuser**: concretizza `Diffuser[EnvironmentView, LightState]` usando `FovEngine`; per ogni luce calcola
  visibilità, applica attenuazione e **somma** i contributi.
* **LightState**: stato finale (`width` + vettore piatto di **Lux**) con `intensity(cell)` e `render(...)` (anche ASCII)
  per debug.
* **Lux**: alias `Double`. Internamente **non normalizziamo**; la normalizzazione è solo per il `render`.

## Flusso di calcolo

1. **Resistenza**: da `EnvironmentView` otteniamo `resistance: Array[Array[Double]]` (celle solide = 1.0).
2. **FOV** (per ogni luce): `visibility = FovEngine.compute(resistance)(origin, radius)`.
3. **Attenuazione**: per cella, con distanza euclidea `d`:
   `atten = 1 / (1 + light.attenuation * (d² / radius²))`.
4. **Contributo cella**: `lux = visibility * light.intensity * atten`.
5. **Somma**: accumuliamo i contributi di tutte le luci → nuovo `LightState`.

> Nella **vista statica** i robot **non** bloccano la luce; nella **vista dinamica** sì.

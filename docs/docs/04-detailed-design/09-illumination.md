---
sidebar_position: 9
---

# Illumination

Il sottosistema di **illumination** è responsabile del calcolo di un **campo di luce** (`LightField`) discreto
all'interno dell'ambiente di simulazione. L'approccio fondamentale consiste nel:

- _discretizzare_ lo spazio in una griglia;
- stimare l'intensità luminosa in ogni cella tenendo conto di **ostacoli**, raggio delle sorgenti
  e, opzionalmente, i **robot**;
- combinare i contributi di tutte le luci in un unico campo luminoso.

Questo permette ai sensori e ad altri componenti della simulazione di interrogare l'intensità luminosa in
_qualsiasi punto_ dello spazio.

## Pipeline computazionale

Il calcolo segue una pipeline ben definita in tre fasi:

1. **rasterizzazione delle occlusioni**: le entità geometriche che proiettano ombre (ostacoli, e opzionalmente i robot)
   vengono convertite in una griglia discreta di valori di occlusione (`0.0` = trasparente, `1.0` = opaco);
2. **calcolo del Field-of-View (FOV)**: per ogni sorgente luminosa, viene calcolata la propagazione della luce. Un
   motore di Field-of-View (FOV) determina quali celle sono visibili dalla sorgente, tenendo conto della mappa di
   occlusione;
3. **combinazione dei campi luminosi**: i contributi di ogni sorgente luminosa vengono aggregati in un unico
   campo luminoso finale, utilizzando una tecnica di somma saturata per mantenere i valori nel range `[0, 1]`.

## Componenti principali

- **`LightMap`**: la _facade pubblica_ del sistema. Incapsula la configurazione (es.
  `ScaleFactor`) e offre preset per bilanciare qualità e performance.
- **`IlluminationLogic`**: l'_orchestratore_ della pipeline. Calcola l’occlusione, ricorre al FOV per
  ogni luce e combina i risultati decidendo se parallelizzare il calcolo.
- **`OcclusionRaster`**: converte le _geometrie_ (cerchi, rettangoli) in una mappa di occlusione su griglia.
- **`FovEngine`**: un'interfaccia _pluggable_ per gli algoritmi di propagazione della luce. L'implementazione attuale usa
  [SquidLib](https://github.com/yellowstonegames/SquidLib).
- **`LightField`**: la _struttura dati finale_ che rappresenta il campo luminoso, interrogabile in coordinate continue
  tramite _interpolazione bilineare_.

## Tagless final pattern

`LightMap` è modellato come un’algebra parametrica sull’effetto `F[_]`. Questo permette di avere:

- **astrazione dall’effetto**: attualmente usiamo `IO`, ma potremmo passare a `Either` o `Future` senza cambiare il core del calcolo;
- **composizione**: si integra con combinatori monadici per orchestrare eventuale parallelismo/caching;
- **testabilità**: nei test si può istanziare un interprete puro (`Id`) che restituisce campi predefiniti;
- **evoluzione futura**: se in futuro servissero effetti più complessi (es. logging, error handling), si può
  passare a `EitherT[IO, Error, A]` senza cambiare le API pubbliche.

> Nota: l'effetto `F[_]` incapsula solo le _politiche di esecuzione_ (quando e come eseguirle).

## Scelte di design

### Rappresentazione a griglia (grid-based)

Lo spazio continuo è campionato in una griglia di $$width*scale × height*scale$$ celle. La _risoluzione_ è controllata
da una scala `ScaleFactor` (celle per metro). Un valore più alto aumenta la qualità delle ombre e dei
dettagli, ma incrementa anche il costo computazionale.

> Nota: l'accuratezza dipende dalla risoluzione. A scale basse, i contorni delle ombre possono apparire "sgranati"
> (aliasing).

### Mappa di occlusione

Prima di calcolare la propagazione della luce, viene generata una mappa di occlusione (`occlusionGrid`). Vengono
rasterizzati:

- **ostacoli statici** (sempre inclusi);
- **entità dinamiche**, come robot (inclusione opzionale, utile quando devono proiettare ombre).

Le forme geometriche sono convertite in celle occluse tramite algoritmi ottimizzati e cache-friendly (es. Scan-line per
i cerchi). Questo approccio è veloce, ma la binarizzazione (0/1) non supporta oggetti semitrasparenti.

### Propagazione della luce indipendente (per-light FOV)

Ogni sorgente luminosa è trattata in modo _indipendente_. Un `FovEngine` calcola il contributo di luce per
una singola sorgente, rispettando la mappa di occlusione. Questo disaccoppia la logica di propagazione dalla libreria
di FOV, rendendo il motore sostituibile.

> Nota: il _profilo di decadimento_ della luce è determinato dal motore di FOV.
> `Light.intensity` scala il contributo, mentre `illuminationRadius` limita la portata.

### Combinazione dei contributi

I campi luminosi di ogni sorgente sono combinati cella-per-cella tramite una _somma saturata_ ($$min(a+b, 1.0)$$). Questa
operazione:

- _garantisce_ che l'intensità finale sia sempre nel range `[0, 1]`, ideale per sensori e rendering.
- è _associativa e commutativa_, rendendo il risultato deterministico anche con calcoli in parallelo.

### Parallelizzazione “adattiva”

Il calcolo viene eseguito in parallelo _solo quando è vantaggioso_, ovvero per griglie molto grandi o con molte luci. Le
soglie di attivazione sono configurabili.
Grazie alla somma saturata, la riduzione parallela resta deterministica.

### Interrogazione del campo con interpolazione

Il `LightField` finale memorizza valori discreti per cella. Questo permettere ai sensori di leggere un valore di luce
da una coordinata continua (`Point2D`), utilizzando l'_interpolazione bilineare_ (risultati fluidi e aliasing ridotto).

> Nota: fuori dai confini ritorna `0.0`.

### Integrazione con `Environment`

Nell'`Environment` il campo luminoso (`LightField`) è un `lazy val`.
Di conseguenza viene calcolato solo alla prima richiesta e il risultato viene messo in cache per tutta la vita dell'istanza.

> Nota: se l'ambiente o le luci cambiano, il `LightField` deve essere invalidato (non è gestito automaticamente).

:::info
Per i dettagli e gli snippet di codice, vedi [Illumination — Implementazione](../05-implementation/03-david-cohen/illumination.md).
:::

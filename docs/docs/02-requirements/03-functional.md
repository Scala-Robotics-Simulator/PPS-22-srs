---
sidebar_label: 2.3. Funzionali
sidebar_position: 3
description: "Requisiti funzionali e di sistema del progetto."
---

# Funzionali

## Utente

- **[UF-01](./03-functional.md)** – Scelta di uno scenario preconfigurato;
- Gestione parametri della simulazione:
  - **[UF-02](./03-functional.md)** - Dimensioni ambiente;
  - **[UF-03](./03-functional.md)** - Durata simulazione;
  - **[UF-04](./03-functional.md)** - Seed;
  - **[UF-05](./03-functional.md)** - Velocità massima dei robot;
  - **[UF-06](./03-functional.md)** - Gestione del rumore.
- Gestione entità:
  - **[UF-07](./03-functional.md)** – Robot (numero, posizione, velocità);
  - **[UF-08](./03-functional.md)** – Ostacoli (numero, posizione);
  - **[UF-09](./03-functional.md)** – Luci (numero, posizione, intensità).
- Gestione sensori:
  - **[UF-10](./03-functional.md)** – Posizione e numero dei sensori di prossimità;
  - **[UF-11](./03-functional.md)** – Posizione e numero dei foto sensori.
- Gestione configurazione di simulazione:
  - **[UF-12](./03-functional.md)** – Salvataggio;
  - **[UF-13](./03-functional.md)** – Caricamento di file personalizzati;
- **[UF-14](./03-functional.md)** – Avviare, mettere in pausa o fermare la simulazione;
- **[UF-15](./03-functional.md)** – Modificare velocità di simulazione per esperimenti lunghi.

## Di sistema

- Generazione entità:
  - **[SF-01](./03-functional.md)** – Robot circolari con raggio costante;
  - **[SF-02](./03-functional.md)** – Ostacoli rettangolari;
  - **[SF-03](./03-functional.md)** – Ambiente con dimensione definita dall’utente.
- Gestione fisica:
  - **[SF-04](./03-functional.md)** – Movimento robot tramite ruote;
  - **[SF-05](./03-functional.md)** – Rilevamento e gestione collisioni;
  - **[SF-06](./03-functional.md)** – Calcolo diffusione della luce;
  - **[SF-07](./03-functional.md)** – Limiti ambientali.
- Logica di sistema:
  - **[SF-08](./03-functional.md)** – Definire un set di regole logiche per gestire le decisioni;
  - **[SF-09](./03-functional.md)** – Esporre API di controllo headless;
  - **[SF-10](./03-functional.md)** – Invalidazione di entità sovrapposte;
  - **[SF-11](./03-functional.md)** – Logging;
  - **[SF-12](./03-functional.md)** – Ritorno posizioni finali dei robot a fine simulazione.
- **[SF-13](./03-functional.md)** – Eseguire simulazione CLI a massima velocità.

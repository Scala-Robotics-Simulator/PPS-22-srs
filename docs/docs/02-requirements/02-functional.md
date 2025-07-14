---
description: "Requisiti funzionali e di sistema del progetto."
---

# Funzionali

## Utente

- **[UF-01](/docs)** – Scelta di uno scenario preconfigurato;
- Gestione parametri della simulazione:
    - **[UF-02](/docs)** - Dimensioni ambiente;
    - **[UF-03](/docs)** - Durata simulazione;
    - **[UF-04](/docs)** - Seed;
    - **[UF-05](/docs)** - Velocità massima dei robot;
    - **[UF-06](/docs)** - Gestione del rumore.
- Gestione entità:
    - **[UF-07](/docs)** – Robot (numero, posizione, velocità);
    - **[UF-08](/docs)** – Ostacoli (numero, posizione);
    - **[UF-09](/docs)** – Luci (numero, posizione, intensità).
- Gestione sensori:
    - **[UF-10](/docs)** – Posizione e numero dei sensori di prossimità;
    - **[UF-11](/docs)** – Posizione e numero dei foto sensori.
- Gestione configurazione di simulazione:
    - **[UF-12](/docs)** – Salvataggio;
    - **[UF-13](/docs)** – Caricamento di file personalizzati;
- **[UF-14](/docs)** – Avviare, mettere in pausa o fermare la simulazione;
- **[UF-15](/docs)** – Modificare velocità di simulazione per esperimenti lunghi.

## Di sistema

- Generazione entità:
    - **[SF-01](/docs)** – Robot circolari con raggio costante;
    - **[SF-02](/docs)** – Ostacoli rettangolari;
    - **[SF-03](/docs)** – Ambiente con dimensione definita dall’utente.
- Gestione fisica:
    - **[SF-04](/docs)** – Movimento robot tramite ruote;
    - **[SF-05](/docs)** – Rilevamento e gestione collisioni;
    - **[SF-06](/docs)** – Calcolo diffusione della luce;
    - **[SF-07](/docs)** – Limiti ambientali.
- Logica di sistema:
    - **[SF-08](/docs)** – Definire un set di regole logiche per gestire le decisioni;
    - **[SF-09](/docs)** – Esporre API di controllo headless;
    - **[SF-10](/docs)** – Invalidazione di entità sovrapposte;
    - **[SF-11](/docs)** – Logging;
    - **[SF-12](/docs)** – Ritorno posizioni finali dei robot a fine simulazione.
- **[SF-13](/docs)** – Eseguire simulazione CLI a massima velocità.
---
id: requisiti
title: Requisiti
sidebar_position: 1
description: Requisiti di business, di dominio, funzionali, non funzionali e di implementazione.
---

# Requisiti

## Business

- **[B-01](/docs)** – Simulazione multi-robot in ambiente 2D statico.
- **[B-02](/docs)** – Osservare e monitorare le diverse tipologie di comportamento dei robot.
- **[B-03](/docs)** – Configurare ambiente ed elementi della simulazione per influenzarne l’andamento.

## Modello di dominio

### Funzionali

#### Utente

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

#### Di sistema

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


### Non funzionali

- **[NF-01](/docs)** – Prestazioni: ≥ 100 robot a 30 fps.
- **[NF-02](/docs)** – Usabilità CLI: questionario rivolto a N utenti per valutare l'interfaccia
- **[NF-03](/docs)** – Portabilità: compatibilità multi-OS e versioni (verifiche CI).
- **[NF-04](/docs)** – Riproducibilità: simulazione identica con stesso seed e parametri.
- **[NF-05](/docs)** – Robustezza: copertura unit-test ≥ N %.
- **[NF-06](/docs)** – Sicurezza: validazione preventiva del file di configurazione.
- **[NF-07](/docs)** – Estensibilità: aggiunta di sensori e/o attuatori senza modifiche al core.

## Di implementazione

- Stack tecnologico:
  - Scala 3.0;
  - sbt;
  - ScalaTest.
- Pipeline CI/CD:
  - GitHub Actions;
  - Test automatizzati;
  - Quality Assurance;
  - Documentazione.
- Gestione progetto:
  - Backlog Trello;
  - Sprint bisettimanali.

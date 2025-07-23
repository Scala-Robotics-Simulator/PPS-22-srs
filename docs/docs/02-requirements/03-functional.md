---
sidebar_label: 2.3. Funzionali
sidebar_position: 3
description: "Requisiti funzionali e di sistema del progetto."
---

# Funzionali

## Intervista

All'inizio del progetto, il Product Owner e l'Esperto di Dominio hanno condotto un'intervista per raccogliere le
informazioni necessarie a definire i requisiti funzionali utente e di sistema del progetto. L'intervista ha avuto come
obiettivo principale quello di comprendere le aspettative degli utenti e le funzionalità chiave richieste per il sistema
di simulazione robotica.

> Il sistema deve consentire all’utente di gestire e configurare in modo completo i parametri della simulazione,
riguardanti l’ambiente in cui operano i robot, gli ostacoli presenti e i robot stessi.

> Tra le principali funzionalità richieste c’è la possibilità per l’utente di scegliere uno scenario preconfigurato che
definisce la disposizione dell’ambiente, degli ostacoli e dei robot. In alternativa, l’utente potrà caricare una
configurazione precedentemente salvata o salvarne una nuova, così da riutilizzarla o modificarla successivamente.

> Un aspetto cruciale riguarda l’avvio della simulazione: prima di iniziare, il sistema deve verificare la correttezza e
la validità dei parametri configurati dall’utente. Una volta superata questa fase di validazione, la simulazione può
partire e l’utente ha la possibilità di controllarne il ciclo di vita attraverso comandi di pausa e arresto. Durante
l’esecuzione, è inoltre fondamentale garantire la possibilità di monitorare in tempo reale lo stato dei robot, con dati
aggiornati costantemente per consentire un controllo puntuale delle attività e delle interazioni.

> Il cuore operativo del sistema è rappresentato dal motore di simulazione, che lavora dietro le quinte e si occupa di
elaborare la logica della simulazione. Questo motore gestisce aspetti fisici e comportamentali fondamentali, quali il
movimento dei robot, la gestione delle collisioni e la diffusione della luce nell’ambiente simulato.

## Casi d'uso

Sulla base delle informazioni raccolte durante l’intervista, sono stati modellati i seguenti casi d’uso.

| ![Use case diagram 01](../../static/img/02-requirements/use-cases-diagram-01.png) |
|-----------------------------------------------------------------------------------|
| ![Use case diagram 02](../../static/img/02-requirements/use-cases-diagram-02.png) |

Di seguito sono riportati i requisiti funzionali, sia utente che di sistema, emersi dall'analisi dell'intervista e dei
casi d'uso.

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

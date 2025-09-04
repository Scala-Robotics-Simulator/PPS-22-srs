---
sidebar_position: 1
---

# Organizzazione del Codice

La struttura del codice è organizzata in maniera _modulare_, per facilitare comprensione e manutenzione.
La seguente immagine mostra la disposizione delle cartelle:

![Code Organization](../../static/img/04-detailed-design/code-organization.png)

> Nota: alcune cartelle sono state omesse per semplificare la visualizzazione, ad esempio quelle in `utils` e tutte le
> cartelle `dsl`.

## Descrizione delle Cartelle

Il codice è suddiviso in 5 _package_ principali:

- `model`: classi che rappresentano il modello di dominio del simulatore;
- `view`: classi che gestiscono l'interfaccia e l'interazione con l'utente;
- `controller`: classi che gestiscono la logica di controllo e l'interazione tra modello e vista;
- `utils`: classi di utilità e librerie di supporto;
- `config`: classi specifiche per la configurazione del simulatore.

---
sidebar_position: 5
---

# Implementazione della GUI di configurazione

Eseguendo il simulatore senza indicare `--headless`, viene mostrata inizialmente l'interfaccia grafica di configurazione.

Il simulatore può essere configurato in due modalità, tramite file `YAML`, come descritto nella sezione
[Configuration](../../04-detailed-design/08-configuration.md), oppure tramite interfaccia grafica.

La **configuration gui** è l’interfaccia grafica che permette di modificare i parametri del simulatore in modo
interattivo, senza dover modificare manualmente il file `YAML`.

Tramite l'interfaccia grafica, è possibile:

- Visualizzare i parametri correnti del simulatore;
- Modificare i valori dei parametri;
- Salvare la configurazione in un file `YAML`;
- Caricare una configurazione da un file `YAML`;
- Caricare una delle configurazioni predefinite.

## Componenti

Ogni elemento dell'interfaccia è gestito da una componente, ovvero un `JPanel`, per permettere maggiore modularità e
riutilizzo del codice.
I componenti utilizzati sono i seguenti:

- `ConfigurationControlsPanel`: gestisce i controlli per salvare e caricare le configurazioni personalizzate e quelle predefinite.
- `SimulationSettingsPanel`: gestisce i parametri specifici della simulazione, come la durata e il seed.
- `EnvironmentSettingsPanel`: gestisce i parametri dell'ambiente, come la dimensione della mappa.
- `EntitiesPanel`: gestisce le entità presenti nella simulazione, ovvero i robot, le luci e gli ostacoli.
- `SimulationCanvas`: per ottenere una preview dell'ambiente di simulazione.

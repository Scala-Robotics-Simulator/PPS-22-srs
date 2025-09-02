---
sidebar_position: 7
---

# Action

![Action](../../static/img/04-detailed-design/action.png)

Il modulo `Action` definisce il comportamento eseguibile dalle entità dinamiche all’interno della simulazione, modellando
le possibili azioni in maniera astratta, composabile e indipendente dall’implementazione concreta.

Un’interfaccia dedicata `ActionAlg` specifica quali operazioni sono disponibili per un’entità dinamica,
come l’assegnazione di velocità differenziate alle ruote per controllarne il movimento. L’algebra definisce _cosa_ può
essere fatto, lasciando a implementazioni concrete la responsabilità di stabilire _come_ avviene l’azione.

Diverse tipologie di azioni sono disponibili, tra cui:
- movimenti che applicano velocità diverse alle ruote dell’entità;
- azioni nulle, che lasciano invariato lo stato;
- sequenze di azioni, che consentono la composizione ordinata e monadica di più comportamenti.

Sono inoltre fornite azioni predefinite per i movimenti più comuni (avanti, indietro, svolta a sinistra/destra, stop),
semplificando l’utilizzo. È anche possibile definire azioni personalizzate, con validazione dei parametri in fase di
creazione per garantirne la coerenza dei parametri.

In questo modo, il modulo `Action` si presenta come un componente estendibile, che mantiene la distinzione tra interfaccia
astratta e implementazioni concrete, permettendo l’evoluzione futura del sistema con l’aggiunta di nuove tipologie di
azioni senza modificare le logiche esistenti.

:::info
Per i dettagli di implementazione del modulo **Action**, si rimanda alla
sezione [Action](../05-implementation/04-giulia-nardicchia/action.md).
:::
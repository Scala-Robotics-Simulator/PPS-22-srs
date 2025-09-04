---
sidebar_label: 3. Design Architetturale
sidebar_position: 3
---

# Design Architetturale

## Architettura del sistema

L'architettura del sistema è basata sul pattern **Model-View-Controller** (MVC). Questo paradigma
consente di separare le responsabilità all'interno dell'applicazione favorendo una chiara distinzione tra la logica di
business e quella di presentazione.

Tale architettura è ulteriormente modulata tramite l'utilizzo del **Cake Pattern**, una tecnica di _dependency
injection_ che permette di definire i componenti Model, View e Controller come moduli indipendenti (tramite _trait_),
specificando in modo esplicito le dipendenze tra di essi. Questo approccio facilita la composizione, la testabilità e la
sostituzione dei singoli componenti.

L’intera architettura è definita in modo generico rispetto al tipo di stato `S`, che rappresenta l’informazione
mantenuta e gestita durante la simulazione. I trait `Model`, `View` e `Controller` sono parametrizzati rispetto a `S`, garantendo
riusabilità e indipendenza rispetto a una specifica rappresentazione dello stato.

![MVC](../static/img/03-architectural-design/mvc.png)
> Nota: il diagramma mostra la versione utilizzata nell'implementazione.

## Componenti dell'architettura

L'architettura è costituita da tre componenti principali:

### Model

Rappresenta la logica di business dell'applicazione. In particolare, gestisce lo stato del simulatore e le
regole di interazione tra le entità. Lo stato viene esposto in sola lettura; l'unico modo per
modificarlo è tramite la funzione `update`, che riceve lo stato corrente e una funzione di
aggiornamento `f: S ⇒ S`, restituendo un nuovo stato aggiornato. Questa scelta consente di mantenere il _Model_
immutabile e facilmente testabile, riducendo i possibili effetti collaterali.

### View

È responsabile della visualizzazione dello stato dell'applicazione e dell'interazione con l'utente. Riceve dal
_Controller_ le informazioni, che a sua volta le ottiene dal _Model_, e le visualizza in modo appropriato. La _View_ si
limita a presentare i dati ricevuti e mostrarne gli aggiornamenti tramite la funzione `render`.
L'interazione con l'utente viene interpretata dal _Controller_, che si occupa di inviare gli eventi al _Model_. In questo
modo, la _View_ rimane indipendente dalla logica del _Model_ e può essere facilmente sostituita o modificata senza
impattare il funzionamento del simulatore.

### Controller

È il componente che gestisce la logica di controllo dell'applicazione. Funge da intermediario tra _Model_ e _View_,
mantenendo una chiara separazione delle responsabilità. Il _Controller_ espone la funzione `simulationLoop`,
che rappresenta il ciclo principale della simulazione. All'interno di questo ciclo, il _Controller_ riceve l'attuale stato
dell'applicazione, elabora gli eventi provenienti dalla _View_, costruisce le funzioni di aggiornamento da applicare al
_Model_ e richiama la _View_ per visualizzare i risultati tramite `render`.

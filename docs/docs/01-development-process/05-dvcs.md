# DVCS

Sono stati utilizzati _Git_ e _GitHub_ come sistema di controllo versione distribuito (DVCS) per gestire il codice sorgente
del progetto. Il modello di branching adottato è **Trunk Based Development**, che prevede l'utilizzo di un branch
principale (`main`) per lo sviluppo continuo e la creazione di branch secondari per funzionalità specifiche.

## Branching Model

Il modello di branching adottato è stato il seguente:

- **main**: branch principale, sempre in stato _production-ready_. Tutte le modifiche vengono integrate nel branch
  dopo essere state testate e revisionate;
- **feature/**: branch per lo sviluppo di nuove funzionalità. Ogni feature ha un proprio branch dedicato, creati da `main`
  e uniti su `main` una volta completati e testati;
- **hotfix/**: branch per risolvere bug critici in produzione. Creati da `main`; una volta risolto il problema, le
  modifiche vengono integrate su `main` e nei branch delle feature attive;
- **doc**: branch dedicato alla documentazione del progetto; le modifiche alla documentazione vengono poi integrate su
  `main`.

## Commit e Pull Request

I commit e i titoli delle pull request seguono [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/).
Le release sono state effettuate tramite [GitHub Actions](https://github.com/features/actions), seguendo le convenzioni di versioning
semantico [SemVer](https://semver.org/).
Per il controllo del testo dei commit è stato utilizzato un git hook `commit-msg`, che ne verifica la conformità alle
convenzioni. Per la verifica della correttezza del codice è stato invece utilizzato un hook `pre-push`, che esegue
automaticamente i controlli di linting, formattazione e test.
Per le pull request, il titolo è il contenuto è stato controllato con [Mergify](https://mergify.com/), che ne verifica
la conformità alle convenzioni
e richiede che siano state superate tutte le verifiche di Continuous Integration (CI) prima di poter essere unite a
`main`.

## Definition of Done

Per chiudere una pull request, è necessario che siano soddisfatte le seguenti condizioni:

- il codice è stato testato e verificato;
- la pull request è stata revisionata da almeno due membri del team;
- il titolo della pull request segue i [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/);
- tutti i test passano con successo;
- non ci sono conflitti di merge con il branch `main`;
- non ci sono conversazioni aperte nella pull request;
- la documentazione è aggiornata, se necessario.

# DVCS

Si è utilizzato Git e GitHub come sistema di controllo versione distribuito (DVCS) per gestire il codice sorgente del progetto.
Il modello di branching adottato è stato **Trunk Based Development**, che prevede l'utilizzo di un branch principale (main) per lo sviluppo continuo e la creazione di branch secondari per le funzionalità specifiche.

## Branching Model

Il modello di branching adottato è stato il seguente:

- **main**: Il branch principale, che contiene sempre il codice pronto per la produzione. Tutte le modifiche vengono integrate in questo branch dopo essere state testate e revisionate.
- **feature/**: Branch creati per lo sviluppo di nuove funzionalità. Ogni feature ha un proprio branch dedicato, che viene creato a partire dal branch main e successivamente unito al main una volta completata e testata.
- **hotfix/**: Branch creati per risolvere bug critici in produzione. Questi branch vengono creati a partire dal branch main e, una volta risolto il problema, le modifiche vengono integrate nel main e nei branch delle feature attive.
- **doc**: Branch dedicato alla documentazione del progetto. Le modifiche alla documentazione vengono effettuate in questo branch e successivamente integrate nel main.

## Commit e Pull Request

I commit e i titoli delle pull request seguono [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/).
Le release sono state effettuate tramite GitHub Actions, seguendo le convenzioni di versionamento semantico [SemVer](https://semver.org/).
Per il controllo del testo dei commit è stato utilizzato un git hook pre-commit, che verifica che i commit rispettino le convenzioni stabilite.
Per le pull request, il titolo è stato controllato utilizzando l'app [Mergify](https://mergify.com/), che verifica che il titolo della pull request rispetti le convenzioni stabilite.

## Definition of Done

Per poter chiudere una pull request, è necessario che siano soddisfatte le seguenti condizioni:

- Il codice deve essere stato testato e verificato.
- La pull request deve essere stata revisionata da almeno due altri membri del team.
- Il titolo della pull request deve seguire le convenzioni di [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/).
- Tutti i test devono passare con successo.
- La documentazione deve essere aggiornata, se necessario.

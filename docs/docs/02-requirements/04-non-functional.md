---
sidebar_label: 2.4. Non funzionali
sidebar_position: 4
description: "Requisiti non funzionali del progetto."
---

# Non funzionali

- **[NF-01](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/blob/main/src/test/scala/io/github/srs/model/dsl/GridDSLTest.scala)** – Prestazioni: ≥ 30 robot a 10 fps.
- **[NF-02](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/actions)** – Portabilità: compatibilità multi-OS e versioni.
- **[NF-03](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/blob/main/src/test/scala/io/github/srs/model/dsl/GridDSLTest.scala)** – Riproducibilità: simulazione identica con stesso seed e parametri.
- **[NF-04](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/blob/main/src/test/scala/io/github/srs/model/dsl/GridDSLTest.scala)** – Sicurezza: validazione preventiva del file di configurazione.
- **[NF-05](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/blob/main/src/main/scala/io/github/srs/model/entity/dynamicentity/sensor/Sensor.scala)** – Estensibilità: aggiunta di sensori e/o attuatori senza modifiche al core.

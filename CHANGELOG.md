## [0.3.0](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/compare/v0.2.0...v0.3.0) (2025-08-11)

### Features

* **actuator:** add differential wheel motor dsl ([146c5f6](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/146c5f607cc3d03cf25e2d20b20808dce509be83))
* add GridConfig and validation logic for grid lighting ([daf94be](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/daf94bee879308b947842e0eb8e40b1c16e429c2))
* add ResistanceMap for managing grid resistance values ([0c81f2d](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/0c81f2de4ab72dc37f7748e99d20d58183bd4b2d))
* **behavior:** add BehaviorTypes and corresponding tests for behavior DSL ([adccb8e](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/adccb8e52f93f6f1cb7c0d88f762ee00505327c7))
* **behavior:** add Policy object with simple reactive policy and unit tests ([5641476](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/5641476bd4b14768ede8afa55e59a728e41c5dfa))
* **behavior:** add RobotBehavior type and predefined behaviors for robots ([20ece6c](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/20ece6c1ab05e57fd6ca8cd26debf792dcd34f9c))
* **behavior:** add stateless behavior rules and corresponding unit tests ([db39429](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/db39429d74b4734dfac936646570878a7e7321e3))
* **behavior:** enhance Behavior with constructors, combinators, and extension methods ([ae0805d](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/ae0805d8064e06e00129f17afa08dee32b9acb5f))
* **behavior:** first implementation of Behavior with associated methods for action handling ([1578964](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/1578964bf623991e72a8a43712e2bb8347ffc425))
* **behavior:** implement core DSL for behavior rules and conditions with unit tests ([988ecac](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/988ecac29238e70da859659d5c59f6811c303e70))
* **boundary:** add dsl for creation ([e7382b9](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/e7382b98b16e9c575ef62b3a4c950ceacf4857bc))
* **config:** add robot speed management ([1b5b395](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/1b5b395729726f59c999e9d4a1babd790c6620ba))
* **config:** implement serialization of environment ([f3e0bfc](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/f3e0bfc687bd4ef7c1fa04abaffc749215d4920f))
* **config:** implement simulation portion of config convertion to yaml ([2b8d9e7](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/2b8d9e78f641ed92743d6782c3e82129e4b8887b))
* **config:** implement yaml configuration loader ([3a004d1](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/3a004d1f2f083a269c4ee5fa801a992e8c31cb02))
* **config:** parse a custom differential wheel motor from yaml ([4172e4f](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/4172e4f3a668d2fd57e3024afd68e84e797c8624))
* **config:** parsing of custom proximity sensor from yaml ([bcc24f6](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/bcc24f6b04ae01a27f6ff5a5f54cad46b619cfde))
* **config:** update decoders and add optional fields management ([a363635](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/a36363502558f1a65db8c7fd74edadd4b2d8ce79))
* **environment:** add boundaries ([93e6ff7](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/93e6ff79a92803355124e32f15391b7ad4dff877))
* **environment:** add creation dsl ([ea9677c](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/ea9677c02a6318593f667d3ca41c8ba925ceb277))
* **environment:** check collision between circles and rectangles ([39729db](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/39729db14d2d57f674b68eb730590cc23f19fb2c))
* **environment:** check collision between rectangles ([8360476](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/83604765ceff8b370b1f9c00fc948e7e62f444e1))
* **environment:** check collision between robots, or other cirles ([e0b9189](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/e0b91897efcf7bd20c8e14a6f1de3e812e074aa1))
* **environment:** validate entities quantity ([052eadb](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/052eadbe21317959e73bbeaf1d42e1428de27166))
* **environment:** validate environment size ([2026f09](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/2026f0987a1fc500bc4eb3bfd453fc9450b3c164))
* **environment:** validate outside environment entities ([ea30a5c](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/ea30a5cae00d5fe661ff9afbbb50145e28732eb8))
* implement Rand type and RandomDSL for random value generation ([91e52f1](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/91e52f187f83163874ee143a1e56e426d14e09bc))
* introduce ActionDsl for chaining actions and add MovementActionDsl for custom movement actions ([83de39b](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/83de39be57133daf72fea3a1a0e1bcd4cdb0343d))
* **light:** add dsl for creation ([2775d84](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/2775d84edb8136a68ca1a9c41031ae8483a8395a))
* model boundary ([de8d001](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/de8d001ce764490f9f2201fcd43fa1a03f1c210d))
* **obstacle:** add dsl for creation ([ddcdccd](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/ddcdccd7e3d32fa9ec513ce988b05608725ca874))
* **robot:** add dsl for creation ([618e3ff](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/618e3ff2ea31026659eea5516dad7e204270de1d))
* simplify act method in DifferentialWheelMotor using functional composition with DifferentialKinematics, an object introduced for robot motion calculations ([aa4a516](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/aa4a5162decb16a63419ae89aa60d0dcb6e2a0a7))
* **subgrid:** add SubGrid and SubGridIndexing with related tests ([8096fb5](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/8096fb55381b69cc891e5e65aaeb5a798a7baa6b))
* **subgrid:** implement SubGridEnv for coordinate conversion ([fbdee46](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/fbdee46230637a3d19abe0cd3333f735477fb694))

### Dependency updates

* **core-deps:** update dependency scala to v3.7.2 ([#20](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/issues/20)) ([d59e23a](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/d59e23ad87f01afb1455eee1ffd9f59562faacd3))
* **deps:** migrate from monix to cats.effect ([9c2d1ed](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/9c2d1edfa750d7afed760f8855ad9fb3f13d1963))
* **deps:** update dependency org.typelevel:cats-effect to v3.7-4972921 ([#33](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/issues/33)) ([d9b7b94](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/d9b7b94e8d1a40c55c122768a3a6b3030a683f4d))
* **deps:** update dependency sbt/sbt to v1.11.4 ([#29](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/issues/29)) ([b079128](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/b0791289d9afb2c82baadf6ce504ac39279f9a69))
* **deps:** update dependency scalafmt to v3.9.9 ([#26](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/issues/26)) ([8e157ff](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/8e157ff735756163ac020379cc8a4e0f02a6c5be))
* **deps:** update dependency semantic-release-preconfigured-conventional-commits to v1.1.139 ([#30](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/issues/30)) ([82f8616](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/82f861654062b4e8040ff25f45d3b5b94e3f76f2))
* **deps:** update node.js to 22.18 ([#24](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/issues/24)) ([2eb6dc6](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/2eb6dc6f7237aa0e2f1a322cb3d4e34e323bf162))
* **deps:** update react monorepo to v19.1.1 ([#23](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/issues/23)) ([4cab20f](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/4cab20fded4f7cc8e21689a142b62c18fb1c5dd3))

### Bug Fixes

* **ActionDsl:** add missing newline at end of file for proper formatting ([da70265](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/da70265b0d8c3fd6ad47d0767ced9e8c2fd768a0))
* **config:** correctly manage missing simulation fields ([b310ff1](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/b310ff1a5d441ba6a1296a21712422cdf0eb9377))
* **proximity-sensor:** manage rectangle objects orientation ([f610716](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/f61071615b2fa77312f433c2972d4d54eaa409bd))
* remove all residual Robot type from Policy and Rules tests ([df41133](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/df411336c4929fa56f7a100b0f05dbd59c01bec1))
* update Action type parameter in Policy and Rules to new Action ([e149067](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/e149067d5210f1c00dae454d56a05543dba330c8))

### Documentation

* add Backlog items for Sprint 2 ([5df176f](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/5df176f1abc86c9b941a0b6ab587eb3e7a464538))
* add scaladocs for GridConfig ([c16591b](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/c16591bc3f66146de4bd52989af715c8bbb7a02e))
* update documentation ([#14](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/issues/14)) ([474dc0c](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/474dc0c8e96f84a0a3d78092d66f998976953c01))

### Tests

* add Action tests for dynamic entities and reorganize action-related test files ([d31bf09](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/d31bf09d64dadc04207efea1788d4042df6cd821))
* add Action tests for dynamic entities and reorganize action-related test files ([0721498](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/07214987cef52002f3359dc3f4f3f29f4dfe2d12))
* add unit tests for Sensor DSL conditions ([0f2a015](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/0f2a01536a234e844491ad65b4f78377b25e1e99))
* **environment:** add specific dsl tests ([8a14197](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/8a141971908a365f8a4e0c3145e51c4d210f0456))
* **environment:** move testing robot away from top left corner ([3d95c9d](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/3d95c9dccd72e8c6761619f0182d24792ac80b0a))
* **environment:** use dsl environment definition ([fc0a845](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/fc0a84595c40e2fca8ce45a9d1f4a315b638aff3))
* remove overlapping entities from tests ([5ad87ca](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/5ad87ca869fe1e3d6f19027590b2a7d4e4792cc8))
* rename GridConfigTest and enhance validation checks ([b8f2639](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/b8f26399b7b59d59b2a6e036dc372302dcc2ea78))
* update YamlConfigLoader and YamlParser tests to work on windows ([92f03b3](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/92f03b33674a8e65bc4e0af9302f439336e66dcd))

### Build and continuous integration

* add project name ([8aef58e](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/8aef58eba51ab9be299b386571cbe066e1f5dd59))
* update fatjar name ([05b40ed](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/05b40edf79bb975cce785082f9741ffaec82b205))

### General maintenance

* **actuators:** migrate to tagless final style with monadic effects ([e5fbbf0](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/e5fbbf0230d82761b32efdf2956ab04dc0632b85))
* add clean script to package.json for improved project maintenance ([8d93a02](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/8d93a0265cb05cc9f12143285d617ed621bf5410))
* delete placeholder file made by the initial commit ([f970aa8](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/f970aa81c8ad1d991e68f831740a542b6f844c3a))
* format code ([e6afca7](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/e6afca792c22b2a7bb02ea5754590b2eae3e14b5))
* improve format and doc for GridConfigTest ([77e3fb1](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/77e3fb1926b197c20a7a7827a7a52f9fdf2bdc60))
* init feat: implement light sensors ([7ba7df9](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/7ba7df964750fcb127d7be24705ca22a72a34ab5))
* init feat: model behaviour ([da816c5](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/da816c565d469c2c896876d4cd8629dfe92e8a93))
* remove comment ([0b3288c](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/0b3288cf27ea266e9f664d4cfdb4c4633288e957))
* remove comment ([97b39af](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/97b39afe6d6af9e7640be51098de603f882bcb4e))
* remove comments ([0f2763c](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/0f2763ce4c541b2d5c2943476f49ca1ced36a8e8))
* remove placeholder file used for creating PR ([4d6fc85](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/4d6fc85412a3f04a7a9ca6dcd5825e3c341a3614))
* remove RobotBehaviors ([f302f0b](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/f302f0b4f12049870f626ea105ceec213f17fa80))
* removed old RobotBehaviorTest ([a84c284](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/a84c284623e88d34a0a3b8fab7934cc153167e12))
* separate radius from illumination radius in light ([0e06be0](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/0e06be03d9d0ce970d7b92b10b0405a832478118))
* standardize spelling of "behavior" in documentation and code comments ([d980663](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/d980663d0c3a74c27f8f450abad9a62f845c9c27))
* structure yaml config loader ([5b7f23e](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/5b7f23e70b3b0741fba53cf1961192d7f43781f1))

### Refactoring

* **action:** modified action to allow for custom validated speeds ([f5d7ce8](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/f5d7ce81febc70d2c33cb8b6a83c89b42e3c83e0))
* add end extension to Rand for scalafmt ([6a0366a](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/6a0366ade35375a0967da4db5bf4de60772d3171))
* add Monix library into dependencies ([1a8fccd](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/1a8fccda746e518dad314c907b1e65ad916a3514))
* add pause and resume functionality to simulation controller and update event handling ([34d5b13](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/34d5b13c89193eda1c7cab6c38a631331caa175f))
* add start and stop button to SimpleView for improved event control ([3c40397](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/3c40397ba1a58ad143773a4ef050b281dedcc8ea))
* **behavior:** clean up imports and improve test readability in BehaviorTest ([325f459](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/325f4597fcfd26aa839e39f634b7e5454c5ee239))
* **behavior:** improve documentation formatting for filter and combining methods ([3a4c0d3](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/3a4c0d3c69513b0f0a0fd8cef6455ab0d276f59e))
* **behavior:** improve documentation formatting in BehaviorTypes and BehaviorTypesTest ([6361619](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/636161913f1c83a17f5f107b3f4c8f7dc3d588a3))
* **behavior:** rename Behavior and BehaviorTest packages for better organization ([9c49cdc](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/9c49cdc6d5136e72e063a6d25c21d29110ff921f))
* **behavior:** update package structure for RobotBehaviors and RobotBehaviorTest ([370e478](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/370e4787d29a194d939baed41216b35527c3ad51))
* change package structure for yaml parser ([760afd8](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/760afd837a99e320f976987e16446a40f881ecb4))
* clean up imports in ProximitySensorTest for better readability ([75ff6af](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/75ff6afdc3d8e44718c5edc744515dd59feaee26))
* enhance simulation model with asynchronous event handling and state management ([a62c068](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/a62c0681abddc143abcae2fa63a01c61abf082b3))
* enhance simulation state management by introducing SimulationStatus and updating related logic ([a184ef7](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/a184ef70a80f41cf51190697621d5b69238c044c))
* extract state increment logic into UpdateLogic module ([c4fcd5d](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/c4fcd5d21ec03dd412420cb9e9c88d8ddc17c271))
* fix merge conflict ([6d4c89a](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/6d4c89a6017002f495de29e61eef21d2c9c185a7))
* fix suggestions ([2b633cc](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/2b633cce49e87444e4c858a7390d293e81bf2d37))
* fix suggestions ([5851ee0](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/5851ee0b5b2721c187b390b85144940867d1342c))
* fix suggestions ([888a93f](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/888a93f4da283e0c6c8800dc81a5b5b4b57e65e0))
* fix suggestions ([8470e6d](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/8470e6d0a802bc1b3dccf6ae3314bf6ca05f7bd5))
* **geometry:** group all geometry utilities into a new package ([b20d926](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/b20d9266107930299c1b039c37ef989766d67510))
* implement change time and tick event handling in simulation controller ([cfaee76](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/cfaee76ab2e305c257ac30fae2886ee3244b0bc7))
* implement Random Number Generator with deterministic and range-based outputs ([aebaf4e](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/aebaf4e3c80ef6c5049d9852cdb83f08e2b95dc3))
* integrate SimpleRNG for dynamic event generation and enhance simulation state ([4d118c8](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/4d118c851be5630723fe999028ece209f9b3b17e))
* introduce simulation speed management and update event handling ([208732e](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/208732ecfd1dc9be7b9f919a32db57917f1d2384))
* redesign Action trait to support effect types, with the tagless final pattern and introduce MovementAction for speed-based actions ([fbb0152](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/fbb01520ee4f2cbcd7460383085ea9baee3fbccf))
* redesign Action trait to support effect types, with the tagless final pattern and introduce MovementAction for speed-based actions ([f7f5f00](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/f7f5f00e37634bd0ac367bbf9fe474fee272f24c))
* remove ChangeTime event handling and reorganize logic for increment operations ([087c596](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/087c596ea815511dc78526cdce24abf56ebf7d21))
* rename RNG and RandomTest files, update package structure, and improve RNG implementation ([33b6e1b](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/33b6e1b9f6145949ccec24a740e9719c83a11995))
* reorganize action-related files into a dedicated package for better structure ([9768c60](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/9768c6099b2a4af55ca147485e6e69fc7d2d5deb))
* reorganize action-related files into a dedicated package for better structure ([6280512](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/6280512d5bb69e655018096a1e3f5f90dc0b20f0))
* reorganize actuator package structure and update imports for dynamic entities ([09ec862](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/09ec86257051b37c881ca1c745063e9aca9bbc79))
* reorganize dynamic entity packages and update imports for actuator components ([c63ce28](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/c63ce2896015c09b34899d4b50d15a4f70da2eca))
* reorganize package structure for actuator-related classes and update imports ([6d703cc](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/6d703cc7664a8af5cb40528bef7a98ffa74e0313))
* **sensor:** improve sensor to use tagless final ([3a06787](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/3a067877a0d9a7956d2f38d4db7f79c4862930e7))
* switch to elapsedTime for timing, update simulation state management and enhance time formatting ([a17f104](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/a17f104b39936487018840851fa5c6fe977728c4))
* update Action and ActionAlg traits to support dynamic entities and rename related files ([c1805f2](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/c1805f280dd35ac554a4e64ef91157d2edaac057))
* update Action and ActionAlg traits to support dynamic entities and rename related files ([05ee355](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/05ee355c31c6f27d2d5279b6e6425c456d9d356e))
* update documentation ([c7270b8](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/c7270b80e5672221ff0ebf18ee536764efaf1748))
* update duration handling and introduce SimulationConfig for improved simulation management ([87f9c03](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/87f9c038712a1035b625b39217a4298e56ce5f43))
* update lighting package structure ([5b53e16](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/5b53e16084c4e16675407a815fc408c0f8a23693))
* update simulation loop and model to use Monix Task for asynchronous processing ([fa9f089](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/fa9f089b6d1242451f39413223cade3c551db9b1))
* update view initialization to accept event queue for better event handling ([c8ea8ed](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/c8ea8ed1413343ac3788ebf7cf0a05a15352c451))
* update WheelMotor and Actuator to use FiniteDuration for time-based actions ([5a35ab9](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/5a35ab987ff9d1c411b76785ccef365c188de3bf))

## [0.2.0](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/compare/v0.1.0...v0.2.0) (2025-07-28)

### Features

* add DynamicEntity and Actuator with initial implementation ([b20f439](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/b20f43927eddbc87002c05da8647a08109016ccd))
* add Entity trait ([193772a](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/193772a913de992bf4d8e28cbd0d3a0c23608b99))
* add moveTo method to Robot trait and corresponding test ([c83e297](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/c83e2979d2ad984546dc2490cd1032eb953a039b))
* add Robot trait and implementation with initial test ([1e0a4c0](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/1e0a4c0fc6400929ee51577fdd56a828d98b5944))
* add validation for radius in StaticEntity.light method and corresponding test case ([5ae925e](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/5ae925e11c86cb237a6af89c00ca48751d965f1b))
* add validation logic and tests for positive values ([e05812e](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/e05812e973a1799b7e1d0978fefa48f8ae3c1f6f))
* control the diffusion of the lights on the enviroment ([#12](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/issues/12)) ([862057e](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/862057e4159791677c0dbe935a0df1d5d3e6430d))
* **environment:** add entities ([69dd959](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/69dd959ceeff811b452f5631a7b1b32131876f36))
* extend DynamicEntity to include actuators and update tests ([d775e30](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/d775e30bee5d67b5ab28a431b5b1e8ba36a34f71))
* implement StaticEntity enum with obstacle and light cases, including validation methods ([2dcd2b8](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/2dcd2b8d31eed4b681af1f1b35c075751e035f22))
* **proximity:** implement proximity sensor with circle and square collison ([4341270](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/4341270c2bbb565492ff46756f670e8452b222a3))
* **sensor:** implement ProximitySensor and associated types ([ac19163](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/ac191638b941fb4bae49b588cbb18527c33fecbf))

### Dependency updates

* **deps:** update dependency semantic-release-preconfigured-conventional-commits to v1.1.137 ([#8](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/issues/8)) ([299350c](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/299350cd22cd753c9caed26692aca5df74452fa9))
* **deps:** update dependency semantic-release-preconfigured-conventional-commits to v1.1.138 ([#9](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/issues/9)) ([d71498e](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/d71498e10b0686b477a150feadf346efb31be75e))

### Bug Fixes

* **environment:** fix unapply ([3770325](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/3770325bbf9a389685d545e64ce554aaa6019194))
* **proximity-sensor:** ensure ray only intersects finite rectangle edges ([276cef6](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/276cef6ddee42b076480f30402846a87e5014926))
* **proximity-sensor:** fix coordinate system ([3605165](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/3605165697bf663a598a01077712db96d3c499a3))

### Documentation

* add documentation for Entity trait and its members ([cd74868](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/cd74868b5afb40ebb250f1847a2cccfb3dc21177))
* add documentation for Orientation trait and its companion object ([422cf27](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/422cf27965da4c9c20837f039fc8141763bf72b9))
* add documentation for ShapeType and its cases ([9b29a18](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/9b29a187c4e7119ef6f7858cd49f0ffe274b6d61))
* add Point2D documentation ([231b31f](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/231b31f8d2dbb191fc126bb2dce870967cca9b93))
* add scaladoc to new point2d methods ([833b24b](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/833b24b765a648171bd47bfafef5aa9d32e245b5))
* add scaladoc to ray ([1029a82](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/1029a82dfa0ac0cdeaa9bbf35a7bf33436687501))
* enhance Actuator trait documentation ([f4a48ca](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/f4a48ca4a5cb37b87502bdb41e54ca7e3e14bbe9))
* enhance documentation for DynamicEntity, Robot, and WheelMotor ([23d9764](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/23d9764c7cbdf8fe8d99a8284740dbe4c07a57f8))
* enhance documentation for Point2D and ShapeType ([9628204](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/9628204ae93371947df1378326a6198235c102e6))
* enhance documentation for StaticEntity and Validation with detailed descriptions and examples ([7fd1eb5](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/7fd1eb558a4f4f06cb9742e7d2a40e216fca4501))
* refactor documentation ([41cb934](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/41cb9341b8cdfc1fdd089fa3a122212f2c8bc0ce))
* rename velocity and position variables for consistency in WheelMotor ([a4b4529](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/a4b4529baae5db80928099813510f47be934bf4c))
* to applyActions method to update robot state based on action sequence ([a46733f](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/a46733faedb51f677057860b2e1487eed6cb495b))

### Tests

* add unit tests for Entity ([73998b0](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/73998b094e81da66e1eaae61f7f48fcf4f355b30))
* add unit tests for Orientation ([2281c09](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/2281c09ab29582b1ea7e951469672564c30b9929))
* add unit tests for Point2D class ([7942b07](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/7942b0793eb7bf95d47afa3355d4a79e0bce4e30))
* add unit tests for ShapeType ([32fdc8f](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/32fdc8f63eb7e87f5412a054469f18573dd30daa))
* add unit tests for Wheel implementation ([4ec17b0](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/4ec17b07103b4f40c4dcdc94afe93fe805ed6c19))
* check validation in proximity sensor ([c045ab4](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/c045ab4a23bf423f18fe402763557020cc5322eb))
* enhance StaticEntityTest with additional validation cases for obstacle and light entities ([fbdb941](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/fbdb94182a5db13882fd8ea141fb818c2e03010c))
* **environment:** hide createEntity function ([c2f54dd](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/c2f54ddb6e04affc3fab740e78889243f2ac0411))
* move sensor tests outside dummy class ([80857c6](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/80857c6b9df236e25fd5861898f8ae8a927c15ec))
* **point2d:** individually test point2d methods ([0188bbb](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/0188bbb2dac2639f44f7c3970c92196775a35d15))
* **proximity-sensor:** check different robot/sensor orientations ([23befc1](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/23befc1d49385ef0878394b41d92afc31502fc9a))
* refactor proximity sensor test ([1318021](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/13180215efb026d00eb7b5425e13d86931c3ef96))
* **sensor-suite:** add test for multiple sensors ([c0a7982](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/c0a79829c4cbec1cb36a6a102cb6b9cb652336aa))

### General maintenance

* **environment:** add validation on width and height ([efbd054](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/efbd0547383a2c33306a20c55ddc116f885aec3b))
* **environment:** add width and height ([db799d2](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/db799d2e267c10129fb9e81e6c4310df22696d18))
* introduce Action enum for robot movement and applyActions method in WheelMotor ([0a69da6](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/0a69da67b034ac92d30809762c2f260507740b19))
* **point2d:** remove constructor ([436762c](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/436762c2f3e16bb4e98ae44a2a93e056b0b03438))
* remove noDefaultArgs from scalafix ([8c52f2a](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/8c52f2a9b190c15625d808c6ecbe396c3d6fba83))
* use tolerance when checking doubles ([4632847](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/46328471fbfc4f12e9b57222ef2d70772680a697))

### Refactoring

* Actuator to use Wheel trait and update tests ([0adb497](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/0adb497153a29dbfb4033248bb0333b58bd2d4da))
* add missing end marker for Component in ModelModule ([dd59398](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/dd59398b0bd54d8d37e75e93b7e655861a6106c4))
* add WheelMotor tests for position and orientation updates based on wheel speeds ([8dd6bd0](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/8dd6bd0055bcaeb739edbc2af1e947af11f73a99))
* clean up code formatting and add missing newlines in StaticEntity and Validation ([31ec17f](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/31ec17fd7ed19f30f78be389b8d9abab081c8f84))
* clean up Point2D implementation and remove import Point2D from Point2DTest ([60d2543](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/60d254390b0310a1d36bf64927509e1877cb43e5))
* code format ([d8b392a](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/d8b392ae23c98d36996ff53bce3ac405f0ae1323))
* enhance ControllerModule with detailed documentation for clarity ([ebf3b6e](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/ebf3b6ef9f6169fa693cb001871e182f41d1be6d))
* enhance documentation in Launcher and ModelModule for better clarity ([bcba48e](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/bcba48e7386ea353a5ea17e194afd1530b6a813e))
* enhance Orientation trait with normalization and additional factory method along with the test ([7c6879f](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/7c6879faa6662fc07d8ff31154be569818c3d232))
* enhance Robot validation with new test cases ([33a8795](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/33a8795ca4fa1b8e710455993e37f4a1100b6571))
* enhance ViewModule with detailed documentation ([a69fda1](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/a69fda191a440811951d3da6a65c7b060cf3cc38))
* enhance Wheel implementation to include shape and update tests ([80a6770](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/80a67707597fdf69408271c4ef9645e85dff3732))
* enhance WheelMotor act method with detailed physics model documentation ([122bb8f](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/122bb8fbac75261a2a30a691db79e0be12e87c4d))
* format import statements for consistency ([a3402e3](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/a3402e30ac42bcc439e439631174b28f8c95d0b7))
* improve formatting and consistency in Entity and related tests ([e4c7a08](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/e4c7a08fc6f19124e5621f084f885b511f8bfa30))
* limit model iterations in Launcher to prevent excessive computation ([8eb63ab](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/8eb63ab747a055f20bdd15c784717ed7cbd45390))
* make proximity sensor a trait to allow for validation ([1fa2162](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/1fa216230a3ad9aa9da140095f6cd324f38afa60))
* move validation externally from Point2D and Orientation ([ae97fcb](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/ae97fcb31af4d2b938ac2ce40060439b493eb47e))
* move Wheel trait and implementation to a new file ([20179e8](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/20179e80faeb127d4696437e57caa89f21341cad))
* move WheelMotor implementation to separate file ([82a9774](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/82a977430407528454c5891c18800e9764d41220))
* re-organize package and fix imports ([2e282b8](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/2e282b87667e6bb5f9f1a2e09fcfa2f2e383b6ca))
* remove Infinite orientation test from Robot validation ([3884613](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/3884613460bdbf7af4b9b38bf22b74ac8dbe6984))
* remove positiveWithZero validation from Point2D ([6318d68](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/6318d68b4cc2a9ead79be7c0e0912c25b815b420))
* rename dynamic and static entity packages to adhere to naming conventions ([3072ef6](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/3072ef68fc8cb83e9c4445297a4a7c208fa77269))
* rename parameter from 'deg' to 'degree' in Orientation implementation ([8c71dc1](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/8c71dc1b3b2a293aac42330c7a23b49e8619bfe7))
* update Action and Robot classes to use Validation for improved error handling ([fd59522](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/fd5952207a3768bd295c0bf663cdea011ae15653))
* update Actuator and DynamicEntity traits for type safety ([5d34e90](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/5d34e90b4c9a6b869d288059481f11bce5d031ff))
* update Controller, Model, View modules to support generic state management and app simulationLoop function ([33a3d0d](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/33a3d0dc8232c81cf6ad5cbe4375ac2b50e8faff))
* update DynamicEntity and Robot tests to use DomainError for validation ([1388843](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/138884327f83c6af225107be54b27a0086fc30d8))
* update import paths for Entity ([34eda91](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/34eda9106ac42ae79f7696b8d4ac38e4fae26700))
* update Robot and WheelMotor tests for improved clarity and accuracy in movement calculations ([2b9a6a1](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/2b9a6a12cb4a11bc7a379b9edbd6d9c554c9c916))
* update Wheel trait to case class ([d51c1e6](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/d51c1e6c77bd46ae939404ee64e944f3c018c5d9))
* use named parameter for state copy in Launcher ([ab77c6e](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/ab77c6ead266cdbd5ed766bd46fd04488a814107))
* WheelMotor actuator and update Robot and DynamicEntity traits ([96a99b5](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/96a99b5ecf02a0dfa966be36a0976b8d3860fd7b))

## [0.1.0](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/compare/v0.0.0...v0.1.0) (2025-07-14)

### Features

* implement MVC architecture with Cake Pattern ([b378572](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/b37857229b9d8a40ae305116df46c624e2c13e03))

### Dependency updates

* **deps:** update dependency sbt/sbt to v1.11.3 ([#3](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/issues/3)) ([d6df90f](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/d6df90f2b9389c2a8e2a5bd46aa10c077057de3e))
* **deps:** update dependency semantic-release-preconfigured-conventional-commits to v1.1.136 ([#1](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/issues/1)) ([7159c8c](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/7159c8c56583c54c2b99ecf6b4debc3ba1b2d8e4))

### Documentation

* create documentation for sprint-0 ([#5](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/issues/5)) ([7fe7900](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/7fe7900232cce28cbf7670a16f4ef829dac4d0dd))
* fix base url ([35c49d5](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/35c49d586bc7424c35132d042407598b3788255a))
* fix broken link ([4681889](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/468188937124bb1cf29287a95a199174d583010e))
* fix locale ([cba1327](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/cba13272201e37d701577a3b36eec9805273759b))

### General maintenance

* add githooks ([eca9b7b](https://github.com/Scala-Robotics-Simulator/PPS-22-srs/commit/eca9b7b9b9f3bf845cc53e039e24b6ae6d46953d))

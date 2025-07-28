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

<!--
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

[![mvn verify][ci_img]][ci_link]
[![Maven Central][maven_img]][maven_link]
[![License][license_img]][license_link]


# TOTP4j (TOTP For Java)

View this plugin's documentation at:
https://code.revelc.net/totp4j

A TOTP implementation in Java

To build:

```
mvn clean verify
```

A basic GUI can be run with:

```
mvn exec:java -Dexec.mainClass=net.revelc.code.otp.totp.TotpUi
```

This plugin uses [Semantic Versioning 2.0.0][1] for its own versioning. Its
public API is the names of the goals and configuration options.

[1]: https://semver.org/spec/v2.0.0.html
[ci_img]: https://github.com/revelc/totp4j/workflows/mvn%20verify/badge.svg
[ci_link]: https://github.com/revelc/totp4j/actions
[license_img]: https://img.shields.io/badge/license-Apache%202.0-blue.svg
[license_link]: https://github.com/revelc/totp4j/blob/main/LICENSE
[maven_img]: https://maven-badges.herokuapp.com/maven-central/net.revelc.code/totp4j/badge.svg
[maven_link]: https://maven-badges.herokuapp.com/maven-central/net.revelc.code/totp4j

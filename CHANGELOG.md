# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Add Calendar Versioning [#253](https://github.com/nbbrd/heylogs/issues/253)
- Add forge configuration [#421](https://github.com/nbbrd/heylogs/issues/421)

### Changed

- Modify versioning configuration [#420](https://github.com/nbbrd/heylogs/issues/420)

## [0.12.1] - 2025-08-18

### Fixed

- Fix javadoc generation on deploy

## [0.12.0] - 2025-08-08

### Added

- Add no-empty-group rule [#406](https://github.com/nbbrd/heylogs/issues/406)
- Add no-empty-release rule [#407](https://github.com/nbbrd/heylogs/issues/407)
- Add unique-release rule [#408](https://github.com/nbbrd/heylogs/issues/408)
- Add context to rule evaluation [#411](https://github.com/nbbrd/heylogs/issues/411)
- Add Forgejo forge and rules [#226](https://github.com/nbbrd/heylogs/issues/226)
- Add GitLab forge and rules [#225](https://github.com/nbbrd/heylogs/issues/225)
- Add imbalanced braces detection rule [#412](https://github.com/nbbrd/heylogs/issues/412)

### Changed

- Switch to JSpecify [#143](https://github.com/nbbrd/heylogs/issues/143)
- Replace category property with module id property [#414](https://github.com/nbbrd/heylogs/issues/414)

## [0.11.1] - 2025-07-24

### Fixed

- Fix Maven Central deployment

## [0.11.0] - 2025-07-23

### Added

- Check for duplicate headings [#402](https://github.com/nbbrd/heylogs/issues/402)
- Check versions against versioning on release [#403](https://github.com/nbbrd/heylogs/issues/403)

### Fixed

- Fix GitHub ref prefix [#368](https://github.com/nbbrd/heylogs/issues/368)

### Changed

- Migrate OSSRH to Central Portal

## [0.10.0] - 2025-05-23

This release focuses on simplifying the CLI and Maven plugin parameters.

### Added

- Add auto-detection of format by filename [#251](https://github.com/nbbrd/heylogs/issues/251)

### Changed

- Simplify mojos parameter properties [#386](https://github.com/nbbrd/heylogs/issues/386)
- Align mojos parameters with CLI parameters [#387](https://github.com/nbbrd/heylogs/issues/387)
- Override changelog during release [#388](https://github.com/nbbrd/heylogs/issues/388)

## [0.9.3] - 2025-02-11

### Fixed

- Fix case sensitivity in link parsing [#355](https://github.com/nbbrd/heylogs/issues/355)

### Changed

- Modernize use of NIO API

## [0.9.2] - 2024-08-30

### Fixed

- Fix blank line at the end of file [#299](https://github.com/nbbrd/heylogs/issues/299)

## [0.9.1] - 2024-08-28

### Fixed

- Fix missing javadoc resources

## [0.9.0] - 2024-08-28

This release adds a release command that greatly improves the use of heylogs in automations.  
It also brings some code refactoring to split the code into more manageable parts.

### Added

- Add release command to promote the Unreleased section into a new release version section [#10](https://github.com/nbbrd/heylogs/issues/10)

### Changed

- Improve code modularization

## [0.8.1] - 2024-04-18

### Fixed

- Fix NoSuchFileException when output parent directories are nonexistent [#342](https://github.com/nbbrd/java-console-properties/issues/342)
- Fix file ordering in MultiFileInput [#343](https://github.com/nbbrd/java-console-properties/issues/343)
- Fix forge URL in summary [#248](https://github.com/nbbrd/heylogs/issues/248)
- Fix missing output file in scan, check and list mojos [#246](https://github.com/nbbrd/heylogs/issues/246)
- Fix several mojo issues when pom project is not available [#249](https://github.com/nbbrd/heylogs/issues/249)

## [0.8.0] - 2024-04-10

This release adds rules for GitHub references and the ability to integrate Heylogs into an automated workflow thanks to json output.
The commands have been refined to improve overall consistency.
A major refactoring has also been done to allow for the next features.

### Added

- Add check on GitHub Pull Request links [#173](https://github.com/nbbrd/heylogs/issues/173)
- Add check on GitHub mentions of people and teams [#157](https://github.com/nbbrd/heylogs/issues/157)
- Add check on GitHub commit SHAs [#223](https://github.com/nbbrd/heylogs/issues/223)
- Improve list command output [#231](https://github.com/nbbrd/heylogs/issues/231)
- Add error severity to failures [#17](https://github.com/nbbrd/heylogs/issues/17)
- Add json formatting [#118](https://github.com/nbbrd/heylogs/issues/118)
- Add versioning extension point [#235](https://github.com/nbbrd/heylogs/issues/235)
- Add forge extension point [#236](https://github.com/nbbrd/heylogs/issues/236)
- Add basic rule check before scanning and extracting [#243](https://github.com/nbbrd/heylogs/issues/243)
- Add scan for forge [#227](https://github.com/nbbrd/heylogs/issues/227)

### Changed

- Refactor API and SPI (breaking changes)
- Set CHANGELOG.md as default value for input file in command line [#237](https://github.com/nbbrd/heylogs/issues/237)

### Fixed

- Fix scan of unreleased version [#228](https://github.com/nbbrd/heylogs/issues/228)

## [0.7.2] - 2023-11-10

### Changed

- Output all separators in error message by [@koppor](https://github.com/koppor) [#164](https://github.com/nbbrd/heylogs/pull/164)

### Fixed

- Fix consistent-separator rule when unreleased keyword is present [#163](https://github.com/nbbrd/heylogs/issues/163)

## [0.7.1] - 2023-10-19

### Fixed

- Fix missing error code when problems are found in check command [#159](https://github.com/nbbrd/heylogs/issues/159)

## [0.7.0] - 2023-10-10

This release adds support for a custom separator between version and date. It also provides some basic documentation.

### Added

- Add support for en dash (`U+2013`) and em dash (`U+2014`) as separator between version and date by [@koppor](https://github.com/koppor) [#140](https://github.com/nbbrd/heylogs/issues/140)
- Add basic documentation [#4](https://github.com/nbbrd/heylogs/issues/4)

### Fixed

- Fix project description in release distribution [#1](https://github.com/nbbrd/jbang-catalog/issues/1)

### Changed

- Rename rule `entry-for-every-versions` to `all-h2-contain-a-version`[@koppor](https://github.com/koppor) [#141](https://github.com/nbbrd/heylogs/issues/141)

## [0.6.0] - 2023-06-20

This release improves extension points and also aligns features of Maven plugin and CLI.

### Added

- Add extension point for scan formatting [#119](https://github.com/nbbrd/heylogs/issues/119)
- Add scan mojo [#120](https://github.com/nbbrd/heylogs/issues/120)
- Add list command and mojo [#120](https://github.com/nbbrd/heylogs/issues/120)

### Changed

- Refactor extension points [#119](https://github.com/nbbrd/heylogs/issues/119)
- Merge old list command into extract command [#120](https://github.com/nbbrd/heylogs/issues/120)
- Improve output of errors in check mojo [#119](https://github.com/nbbrd/heylogs/issues/119)

## [0.5.0] - 2022-11-29

### Added

- Validate project version with Semantic Versioning specification in Maven plugin [#45](https://github.com/nbbrd/heylogs/issues/45)

### Changed

- Modify check goal in Maven plugin to raise error if changelog is missing on root project [#46](https://github.com/nbbrd/heylogs/issues/46)
- Change default phase of check goal to `VALIDATE` in Maven plugin [#47](https://github.com/nbbrd/heylogs/issues/47)

## [0.4.0] - 2022-10-20

### Added

- Add optional semantic versioning rule [#22](https://github.com/nbbrd/heylogs/issues/22)
- Add command to scan content of changelog [#26](https://github.com/nbbrd/heylogs/issues/26)
- Add missing descriptions in CLI [#29](https://github.com/nbbrd/heylogs/issues/29)

### Changed

- Simplify command parameters [#11](https://github.com/nbbrd/heylogs/issues/11)

## [0.3.2] - 2022-10-03

### Removed

- Remove limit-heading-depth rule [#23](https://github.com/nbbrd/heylogs/issues/23)

## [0.3.1] - 2022-09-28

### Changed

- Modify default ref parameter of extract mojo to `${project.version}` [#20](https://github.com/nbbrd/heylogs/issues/20)
- Modify extract mojo to fail if changelog is not found [#21](https://github.com/nbbrd/heylogs/issues/21)

## [0.3.0] - 2022-09-27

### Added

- Add rule extension point [#12](https://github.com/nbbrd/heylogs/issues/12)
- Add failure format extension point [#13](https://github.com/nbbrd/heylogs/issues/13)
- Add unreleased pattern to version filtering [#19](https://github.com/nbbrd/heylogs/issues/19)

### Changed

- Simplify properties naming of Maven plugin [#18](https://github.com/nbbrd/heylogs/issues/18)

## [0.2.0] - 2022-09-21

### Added

- Add Maven Plugin [#5](https://github.com/nbbrd/heylogs/issues/5)

## [0.1.0] - 2022-09-08

### Added

- Initial release

[Unreleased]: https://github.com/nbbrd/heylogs/compare/v0.12.1...HEAD
[0.12.1]: https://github.com/nbbrd/heylogs/compare/v0.12.0...v0.12.1
[0.12.0]: https://github.com/nbbrd/heylogs/compare/v0.11.1...v0.12.0
[0.11.1]: https://github.com/nbbrd/heylogs/compare/v0.11.0...v0.11.1
[0.11.0]: https://github.com/nbbrd/heylogs/compare/v0.10.0...v0.11.0
[0.10.0]: https://github.com/nbbrd/heylogs/compare/v0.9.3...v0.10.0
[0.9.3]: https://github.com/nbbrd/heylogs/compare/v0.9.2...v0.9.3
[0.9.2]: https://github.com/nbbrd/heylogs/compare/v0.9.1...v0.9.2
[0.9.1]: https://github.com/nbbrd/heylogs/compare/v0.9.0...v0.9.1
[0.9.0]: https://github.com/nbbrd/heylogs/compare/v0.8.1...v0.9.0
[0.8.1]: https://github.com/nbbrd/heylogs/compare/v0.8.0...v0.8.1
[0.8.0]: https://github.com/nbbrd/heylogs/compare/v0.7.2...v0.8.0
[0.7.2]: https://github.com/nbbrd/heylogs/compare/v0.7.1...v0.7.2
[0.7.1]: https://github.com/nbbrd/heylogs/compare/v0.7.0...v0.7.1
[0.7.0]: https://github.com/nbbrd/heylogs/compare/v0.6.0...v0.7.0
[0.6.0]: https://github.com/nbbrd/heylogs/compare/v0.5.0...v0.6.0
[0.5.0]: https://github.com/nbbrd/heylogs/compare/v0.4.0...v0.5.0
[0.4.0]: https://github.com/nbbrd/heylogs/compare/v0.3.2...v0.4.0
[0.3.2]: https://github.com/nbbrd/heylogs/compare/v0.3.1...v0.3.2
[0.3.1]: https://github.com/nbbrd/heylogs/compare/v0.3.0...v0.3.1
[0.3.0]: https://github.com/nbbrd/heylogs/compare/v0.2.0...v0.3.0
[0.2.0]: https://github.com/nbbrd/heylogs/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/nbbrd/heylogs/releases/tag/v0.1.0

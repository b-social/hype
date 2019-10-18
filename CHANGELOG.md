# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com)
and this project adheres to 
[Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.0.20] — 2019-10-18

## [0.0.19] — 2019-10-18

## [0.0.18] — 2019-10-18

## [0.0.17] — 2019-09-10
### Added
- Support for query parameters and query template parameters to 
  `absolute-url-for`.
- An `absolute-path-for` function for generating path component only.
- An `absolute-path->absolute-url` function for resolving an absolute path
  against the base URL from a request. 

### Changed
- The signature of `absolute-url-for` (breaking).

### Removed
- `parameterised-url-for` since `absolute-url-for` supersedes its functionality. 

## [0.0.16] — 2019-09-10
Released without _CHANGELOG.md_.

[0.0.16]: https://github.com/b-social/hype/compare/0.0.1...0.0.16
[0.0.17]: https://github.com/b-social/hype/compare/0.0.16...0.0.17
[0.0.18]: https://github.com/b-social/hype/compare/0.0.17...0.0.18
[0.0.19]: https://github.com/b-social/hype/compare/0.0.18...0.0.19
[0.0.20]: https://github.com/b-social/hype/compare/0.0.19...0.0.20
[Unreleased]: https://github.com/b-social/hype/compare/0.0.20...HEAD

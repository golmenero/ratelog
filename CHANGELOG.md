# Ratelog - Changelog

All notable changes to this project will be documented in this file.

## Unreleased
### ⚡ Changed
- Adjustments on release skill

## Release v1.1.15
### ➕ Added
- Add skill for release
### ⚡ Changed
- Disable maven wrapper unneeded

## Release v1.1.14
### ➕ Added
- Added media type dependant search filters
- Added infinite load on search query

## Release v1.1.13
### ➕ Added
- New metadataLang for split language between system and metadata

## Release v1.1.12
### ➕ Added
- I18N on movie and shows metadata

## Release v1.1.9
### ➕ Added
- New loader animation
### ⚡ Changed
- Refactor of detail and other views

## Release v1.1.9
### 🔧 Fixed
- Fix bug on imports

## Release v1.1.8
### ➕ Added
### ⚡ Changed
- Unify followed users use case with community use case
### 🔧 Fixed
- Fix contracts in community handler and controller

## Release v1.1.7
### ➕ Added
- Optimizations on Community tabs. Rework of front
- Optimizations on Profile tabs. Rework of front
### ⚡ Changed
- Infere season on rating modal
### 🔧 Fixed
- Fixed tag styling

## Release v1.1.6
_2026-06-29_
### ⚡ Changed
- Increment responsivity on top view

## Release v1.1.5
_2026-06-21_
### ⚡ Changed
- Major rework on community and profile sections
- Minor rework on search cards

## Release v1.1.4
_2026-06-21_
### ➕ Added
- Added translations for de, fr, it, ja, pt, ru and zh;

## Release v1.1.3
_2026-06-20_
### ➕ Added
- Persist season ratings on DB for feed inference
- Load more pagination on feed and profile sections
### ⚡ Changed
- Now the env var REMEMBER_ME_KEY is not mandatory anymore. If the key is not set, remember me will be disabled.
### 🔧 Fixed
- Fixed some styling of titles on premiere cards and titles

## Release v1.1.2
_2026-06-20_
### 🔧 Fixed
- Fixed tests on CommunityHandler


## Release v1.1.1
_2026-06-20_
### ⚡ Changed
- New UX design of Community and Profile sections
- Unified styling for cards and other minor components


## Release v1.1.0 
_2026-06-20_
### ⚠️ Breaking
The REMEMBER_ME_KEY environment variable is now required. The application will not start without it. See .env.example and README.md for details.
### ➕ Added
- CSRF validation for forms.
- Rate limiter for TMDB requests.
- Dynamic rememberMeKey from environment.
### ⚡ Changed
- Moved format validations to VOs.
- Optimized lang and locale handling.
- Repackage on user use cases.
- Make all handlers transactional.
- Calculated rank on query level.
- Resolved N+1 problem on users.
### 🔧 Fixed
- Added missing translations.
- Protect exceptions on Generic global exceptions handler.
- NPE on tops.
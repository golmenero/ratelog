# Ratelog - Changelog

All notable changes to this project will be documented in this file.

## Unreleased
_2026-06-20_
### ⚠️ Breaking
### ➕ Added
### ⚡ Changed
### 🔧 Fixed


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
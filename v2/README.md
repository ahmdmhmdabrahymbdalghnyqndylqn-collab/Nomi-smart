# Nomi V2 — foundation only
Separate preview package com.nomismart.preview; legacy main unchanged. Do not release or migrate user data from this scaffold. The home is a first glass UI implementation; route placeholders are explicitly labeled. Auth is a PKCE integration skeleton, not a production route guard. No production credentials, live database or provider selected. SDK 54 baseline requires dependency and device verification.
## Setup
Node 22, npm install, npx expo install --fix, npm run doctor, npm run typecheck, npm run test, npm run android (Android SDK required). Commit the generated lockfile after verification. Copy .env.example to .env locally; only publishable values. Configure Google OAuth and exact redirect scheme in Supabase before testing login.
## Release gates
Implement session gate and account linking, RLS and RPC migrations in staging, offline persistence, all task/finance/voice/prayer/alarm operations, native alarm/notification routing, licensed adhan, weather service, admin MFA, RTL/LTR localization and accessibility. Device tests and migration rehearsal required. Do not replace legacy signing/package or merge without verification.

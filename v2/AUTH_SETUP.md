# Nomi Google authentication
Status: Auth logs showed provider is not enabled, HTTP 400. Preflight checks now prevent redirect when disabled. Google Cloud OAuth web application client and Supabase Google provider configuration must be performed by account owner. No client secret belongs in repository, app or chat.
Google callback: https://hybocuzpbvqnlplsehtf.supabase.co/auth/v1/callback
Supabase Site URL: https://ahmdmhmdabrahymbdalghnyqndylqn-collab.github.io/Nomi-smart/
Allowlist exact web routes: /green/, /purple/, /admin-green/, /admin-purple/ under Site URL; mobile nomiv2green://login and nomiv2purple://login (verify actual release redirect). Configure external app audience, test users while in testing, publish when ready. Verify both website and Android sessions; do not assign admin role automatically.

# Required verification (not yet executed)
- anonymous cannot read/write each exposed table.
- authenticated A can read and write own data; B cannot read, insert as A, change ownership, update or delete A rows.
- A cannot attach reminders to B tasks.
- task hard delete denied; soft delete allowed.
- balances separate currencies and omit other owners.
- ordinary users cannot read private admin schema or escalate roles.
- verify grants, indexes and constraints in local PostgreSQL/Supabase.

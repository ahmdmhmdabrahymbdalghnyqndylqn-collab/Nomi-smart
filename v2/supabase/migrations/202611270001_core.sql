-- Staging migration only; do not apply to existing production.
begin;
create table public.profiles (
 id uuid primary key references auth.users(id) on delete cascade,
 display_name text not null default '' check (char_length(display_name)<=120),
 country text not null check (country in ('EG','JO')),
 locale text not null check (locale in ('ar-EG','ar-JO','en')),
 timezone text not null check (timezone in ('Africa/Cairo','Asia/Amman')),
 created_at timestamptz not null default now()
);
alter table public.profiles enable row level security;
revoke all on public.profiles from anon, authenticated;
grant select,insert,update on public.profiles to authenticated;
create policy own_profile_select on public.profiles for select to authenticated using (id=(select auth.uid()));
create policy own_profile_insert on public.profiles for insert to authenticated with check (id=(select auth.uid()));
create policy own_profile_update on public.profiles for update to authenticated using (id=(select auth.uid())) with check (id=(select auth.uid()));
create table public.user_settings (
 user_id uuid primary key references public.profiles(id) on delete cascade,
 theme text not null default 'system' check(theme in ('light','dark','system')),
 variant text not null default 'green' check(variant in ('green','purple')),
 sound_enabled boolean not null default true,
 volume integer not null default 80 check(volume between 0 and 100),
 notifications_enabled boolean not null default true
);
create table public.tasks (
 id uuid primary key default gen_random_uuid(),
 user_id uuid not null references public.profiles(id) on delete cascade,
 title text not null check(char_length(trim(title)) between 1 and 240),
 category text not null default 'personal' check(category in ('personal','work','study')),
 kind text not null default 'task' check(kind in ('task','appointment')),
 due_at timestamptz,
 local_day date not null,
 status text not null default 'pending' check(status in ('pending','done','missed')),
 completed_at timestamptz,
 deleted_at timestamptz,
 created_at timestamptz not null default now(),
 check ((status='done')=(completed_at is not null)),
 unique(user_id,id)
);
create index tasks_owner_day on public.tasks(user_id,local_day) where deleted_at is null;
create table public.transactions (
 id uuid primary key default gen_random_uuid(),
 user_id uuid not null references public.profiles(id) on delete cascade,
 kind text not null check(kind in ('income','expense')),
 currency text not null check(currency in ('EGP','JOD')),
 amount_minor bigint not null check(amount_minor between 1 and 9007199254740991),
 note text not null default '' check(char_length(note)<=500),
 occurred_at timestamptz not null default now(),
 created_at timestamptz not null default now()
);
create index transactions_owner_date on public.transactions(user_id,occurred_at desc);
create table public.alarms (
 id uuid primary key default gen_random_uuid(),
 user_id uuid not null references public.profiles(id) on delete cascade,
 label text not null default '' check(char_length(label)<=120),
 scheduled_at timestamptz not null,
 enabled boolean not null default true
);
create index alarms_owner_time on public.alarms(user_id,scheduled_at);
create table public.prayer_settings (
 user_id uuid primary key references public.profiles(id) on delete cascade,
 city text not null check(char_length(city) between 1 and 120),
 calculation_method text not null check(char_length(calculation_method) between 1 and 80),
 enabled boolean not null default false
);
create table public.reminder_rules (
 id uuid primary key default gen_random_uuid(),
 user_id uuid not null references public.profiles(id) on delete cascade,
 task_id uuid not null,
 minutes_before integer not null check(minutes_before between 0 and 10080),
 foreign key(user_id,task_id) references public.tasks(user_id,id) on delete cascade,
 unique(task_id,minutes_before)
);
create index reminder_rules_owner on public.reminder_rules(user_id);
-- No client access to internal administrative records.
create schema if not exists nomi_private;
revoke all on schema nomi_private from public, anon, authenticated;
create table nomi_private.admin_members (
 user_id uuid primary key references auth.users(id) on delete cascade,
 role text not null check(role in ('support','analyst','owner'))
);
alter table nomi_private.admin_members enable row level security;
create table nomi_private.admin_audit (
 id bigint generated always as identity primary key,
 actor_id uuid references auth.users(id) on delete set null,
 action text not null,
 created_at timestamptz not null default now()
);
alter table nomi_private.admin_audit enable row level security;
do $$ declare t text; begin
 foreach t in array array['user_settings','tasks','transactions','alarms','prayer_settings','reminder_rules'] loop
  execute format('alter table public.%I enable row level security',t);
  execute format('revoke all on public.%I from anon, authenticated',t);
  execute format('grant select,insert,update,delete on public.%I to authenticated',t);
  execute format('create policy owner_select on public.%I for select to authenticated using (user_id=(select auth.uid()))',t);
  execute format('create policy owner_insert on public.%I for insert to authenticated with check (user_id=(select auth.uid()))',t);
  execute format('create policy owner_update on public.%I for update to authenticated using (user_id=(select auth.uid())) with check (user_id=(select auth.uid()))',t);
  execute format('create policy owner_delete on public.%I for delete to authenticated using (user_id=(select auth.uid()))',t);
 end loop;
end $$;
-- Task removal must preserve history: soft-delete through update.
revoke delete on public.tasks from authenticated;
drop policy owner_delete on public.tasks;
create function public.my_balance(p_currency text) returns numeric language sql stable security invoker set search_path = '' as $$
 select coalesce(sum(case when kind='income' then amount_minor else -amount_minor end),0)
 from public.transactions where user_id=(select auth.uid()) and currency=p_currency;
$$;
revoke all on function public.my_balance(text) from public,anon;
grant execute on function public.my_balance(text) to authenticated;
commit;

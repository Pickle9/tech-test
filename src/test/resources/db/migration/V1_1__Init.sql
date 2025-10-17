create table if not exists times
(
    id   bigserial primary key,
    time timestamp not null
)
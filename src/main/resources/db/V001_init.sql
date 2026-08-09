create table article
(
    id     bigserial primary key,
    title  varchar(255) not null,
    status varchar(30)  not null
);

create table processed_article
(
    id         bigserial primary key,
    article_id bigint       not null,
    title      varchar(255) not null
);

insert into article (title, status)
values ('  Spring Batch 시작하기  ', 'PENDING'),
       ('Java 21 Virtual Threads', 'PENDING'),
       ('  PostgreSQL MVCC  ', 'PENDING'),
       ('Already Processed', 'COMPLETED');

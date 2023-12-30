create table if not exists person(
	person_id bigserial primary key,
	name varchar(255) not null,
	name_local varchar(255) not null,
	username varchar(255) unique not null,
	password varchar(255) not null,
	email varchar(255) unique not null,
	phone varchar(255) unique not null,
	created_at timestamp not null,
	updated_at timestamp not null,
	last_login timestamp null
);
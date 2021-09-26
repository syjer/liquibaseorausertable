--liquibase formatted sql
--changeset syjer:2

alter table test_table add column_name2 varchar2(1024);
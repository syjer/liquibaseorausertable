--liquibase formatted sql
--changeset syjer:2

CREATE TABLE test_table(
  column_name VARCHAR2(355)
);

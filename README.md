# liquibaseorausertable

##  Reason

If you are using liquibase on an oracle db with _a lot_ of users and tables, you may have noticed that
running the application, even without any changes is extremely slow at start time.

This is due to the following issue: https://github.com/liquibase/liquibase/issues/1312 .

This is a workaround project for handling this problem by avoiding the problematic calls in the normal code path.

## Maven coordinates:

```
<dependency>
  <groupId>ch.digitalfondue.liquibaseorausertable</groupId>
  <artifactId>liquibaseorausertable</artifactId>
  <version>1.8</version>
</dependency>
```

## Configuration

You can configure the query for checking the existence of a table by defining the **system property**:
```
orausertable.hasTableNamedQueryInAll=false|true
```
Possible values:
- `false` is the default, the query will search in the USER_TABLES table.
- `true` the query will search in the ALL_TABLES

This is useful when doing migrations with a different user than the one of the application.

## Notes

Most of the code, is simply taken from the main liquibase repository at https://github.com/liquibase/liquibase, and thus under the term of the following license (apache v2.0): https://github.com/liquibase/liquibase/blob/master/LICENSE.txt .

My modifications/additions are also under Apache v2.0 license.

This project has a custom LockService and ChangeLogHistoryService that are not doing any call on the oracle ALL_ tables.
This mean that any "database snapshotting" functionality will still be slow.

To run test, you need an oracle service (docker / podman):

> docker run --rm --name oracle-xe-slim -p 1521:1521 -e ORACLE_RANDOM_PASSWORD=true -e APP_USER=test -e APP_USER_PASSWORD=test gvenzl/oracle-xe:11-slim
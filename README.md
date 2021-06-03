#liquibaseorausertable

##  Reason

If you are using liquibase on a oracle db with _a lot_ of users and tables, you may have noticed that
running the application, even without any changes is extremely slow.

This is due to the following issue: https://github.com/liquibase/liquibase/issues/1312 .

This is a workaround project for handling this problem by avoiding the problematic calls in the normal code path.

## Notes

Most of the code, is simply taken from the main liquibase repository at https://github.com/liquibase/liquibase, and thus under the term of the following license (apache v2.0): https://github.com/liquibase/liquibase/blob/master/LICENSE.txt .

My modifications/additions are also under Apache v2.0 license.

This project has a custom LockService and ChangeLogHistoryService that are not doing any call on the oracle ALL_ tables.
This mean that any "database snapshotting" functionality will still be slow.

Maven coordinates:

```
<dependency>
  <groupId>ch.digitalfondue.liquibaseorausertable</groupId>
  <artifactId>liquibaseorausertable</artifactId>
  <version>1.8</version>
</dependency>
```
This is a test/workaround project for handling the issue https://github.com/liquibase/liquibase/issues/1312 .

99.99% of the code, is simply taken from the main liquibase repository at https://github.com/liquibase/liquibase, and thus under the term of the following license (apache v2.0): https://github.com/liquibase/liquibase/blob/master/LICENSE.txt .

My modifications are also under Apache v2.0 license.

This project has a custom LockService and ChangeLogHistoryService that are not doing any call on the oracle ALL_ tables.
This mean that any "database snapshotting" functionality will still be slow.
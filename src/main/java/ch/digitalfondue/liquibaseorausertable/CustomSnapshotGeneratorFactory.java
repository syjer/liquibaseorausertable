package ch.digitalfondue.liquibaseorausertable;

import liquibase.snapshot.CustomJdbcDatabaseSnapshot;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.OfflineConnection;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.*;
import liquibase.structure.DatabaseObject;

public class CustomSnapshotGeneratorFactory extends SnapshotGeneratorFactory {

    private static SnapshotGeneratorFactory instance;

    public DatabaseSnapshot createSnapshot(DatabaseObject[] examples, Database database,
                                           SnapshotControl snapshotControl)
            throws DatabaseException, InvalidExampleException {
        DatabaseConnection conn = database.getConnection();
        if (conn == null) {
            return new EmptyDatabaseSnapshot(database, snapshotControl);
        }
        if (conn instanceof OfflineConnection) {
            DatabaseSnapshot snapshot = ((OfflineConnection) conn).getSnapshot(examples);
            if (snapshot == null) {
                throw new DatabaseException("No snapshotFile parameter specified for offline database");
            }
            return snapshot;
        }
        return new CustomJdbcDatabaseSnapshot(examples, database, snapshotControl);
    }

    protected CustomSnapshotGeneratorFactory() {
        super();
    }

    public static synchronized SnapshotGeneratorFactory getInstance() {
        if (instance == null) {
            instance = new CustomSnapshotGeneratorFactory();
        }
        return instance;
    }
}

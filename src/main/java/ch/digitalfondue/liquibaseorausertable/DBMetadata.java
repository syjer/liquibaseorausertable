package ch.digitalfondue.liquibaseorausertable;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.DataType;
import liquibase.structure.core.Table;

import java.util.Map;

public class DBMetadata {

    public static Table getDatabaseChangeLogTable(Database database) throws DatabaseException {
        if (!hasDatabaseChangeLogTable(database)) {
            return null;
        }
        String tableName = database.getDatabaseChangeLogTableName();
        Table table = new Table(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), tableName);
        RawSqlStatement tableMetadataInfoStatement = new RawSqlStatement("select TABLE_NAME, COLUMN_NAME, DATA_TYPE AS DATA_TYPE_NAME, DATA_TYPE_MOD, DATA_TYPE_OWNER," +
                "                DECODE (data_type, 'CHAR', 1, 'VARCHAR2', 12, 'NUMBER', 3, 'LONG', -1, 'DATE', 93 , 'RAW', -3, 'LONG RAW', -4, 'BLOB', 2004, 'CLOB', 2005, 'BFILE', -13, 'FLOAT', 6, 'TIMESTAMP(6)', 93, 'TIMESTAMP(6) WITH TIME ZONE', -101, 'TIMESTAMP(6) WITH LOCAL TIME ZONE', -102, 'INTERVAL YEAR(2) TO MONTH', -103, 'INTERVAL DAY(2) TO SECOND(6)', -104, 'BINARY_FLOAT', 100, 'BINARY_DOUBLE', 101, 'XMLTYPE', 2009, 1111) AS data_type," +
                "                DECODE( CHAR_USED, 'C',CHAR_LENGTH, DATA_LENGTH ) as DATA_LENGTH," +
                "                DATA_PRECISION, DATA_SCALE, NULLABLE, COLUMN_ID as ORDINAL_POSITION, DEFAULT_LENGTH," +
                "                DATA_DEFAULT," +
                "                NUM_BUCKETS, CHARACTER_SET_NAME, " +
                "                CHAR_COL_DECL_LENGTH, CHAR_LENGTH, " +
                "                CHAR_USED, VIRTUAL_COLUMN " +
                "                FROM USER_TAB_COLS WHERE upper(TABLE_NAME) = upper('" + tableName + "')");
        Scope.getCurrentScope().getSingleton(ExecutorService.class)
                .getExecutor("jdbc", database)
                .queryForList(tableMetadataInfoStatement).forEach(kv -> {
                    Column c = new Column();
                    c.setName((String) kv.get("COLUMN_NAME"));
                    c.setType(extractType(kv));
                    table.addColumn(c);
                });
        //
        return table;
    }

    private static String getStringFromMetadata(Map<String, ?> columnMetadataResultSet, String name) {
        return (String) columnMetadataResultSet.get(name);
    }

    private static Integer getIntegerFromMetadata(Map<String, ?> columnMetadataResultSet, String name) {
        Object o = columnMetadataResultSet.get(name);
        if (o instanceof Number) {
            return ((Number) o).intValue();
        } else if (o instanceof String) {
            return Integer.valueOf((String) o);
        }
        return (Integer) o;
    }

    // imported from ColumnSnapshotGenerator / ColumnSnapshotGeneratorOracle
    private static DataType extractType(Map<String, ?> columnMetadataResultSet) {
        String dataType = getStringFromMetadata(columnMetadataResultSet, "DATA_TYPE_NAME");
        dataType = dataType.replace("VARCHAR2", "VARCHAR");
        dataType = dataType.replace("NVARCHAR2", "NVARCHAR");

        DataType type = new DataType(dataType);
        type.setDataTypeId(getIntegerFromMetadata(columnMetadataResultSet, "DATA_TYPE"));
        if ("NUMBER".equalsIgnoreCase(dataType)) {
            type.setColumnSize(getIntegerFromMetadata(columnMetadataResultSet, "DATA_PRECISION"));
            type.setDecimalDigits(getIntegerFromMetadata(columnMetadataResultSet, "DATA_SCALE"));

        } else {
            type.setColumnSize(getIntegerFromMetadata(columnMetadataResultSet, "DATA_LENGTH"));

            if ("NCLOB".equalsIgnoreCase(dataType) || "BLOB".equalsIgnoreCase(dataType) || "CLOB".equalsIgnoreCase
                    (dataType)) {
                type.setColumnSize(null);
            } else if ("NVARCHAR".equalsIgnoreCase(dataType) || "NCHAR".equalsIgnoreCase(dataType)) {
                type.setColumnSize(getIntegerFromMetadata(columnMetadataResultSet, "CHAR_LENGTH"));
                type.setColumnSizeUnit(DataType.ColumnSizeUnit.CHAR);
            } else {
                String charUsed = getStringFromMetadata(columnMetadataResultSet, "CHAR_USED");
                DataType.ColumnSizeUnit unit = null;
                if ("C".equals(charUsed)) {
                    unit = DataType.ColumnSizeUnit.CHAR;
                    type.setColumnSize(getIntegerFromMetadata(columnMetadataResultSet, "CHAR_LENGTH"));
                } else if ("B".equals(charUsed)) {
                    unit = DataType.ColumnSizeUnit.BYTE;
                }
                type.setColumnSizeUnit(unit);
            }
        }
        return type;
    }
    //

    public static boolean hasDatabaseChangeLogLockTable(Database database) {
        return hasTableNamed(database.getDatabaseChangeLogLockTableName(), database);
    }

    public static boolean hasDatabaseChangeLogTable(Database database) {
        return hasTableNamed(database.getDatabaseChangeLogTableName(), database);
    }

    private static boolean hasTableNamed(String tableName, Database database) {
        String query = "SELECT CASE WHEN EXISTS (SELECT * FROM user_tables WHERE upper(table_name) = upper('" + tableName + "')) THEN 1 ELSE 0 END FROM dual";
        boolean useAllTableQuery = OraUserTableConfiguration.HAS_TABLE_NAMED_QUERY_IN_ALL_TABLES.getCurrentValue();
        if (useAllTableQuery) {
            query = "SELECT CASE WHEN EXISTS (SELECT * FROM all_tables WHERE owner = '" + database.getDefaultSchemaName() + "' AND upper(table_name) = upper('" + tableName + "')) THEN 1 ELSE 0 END FROM dual";
        }
        try {
            RawSqlStatement tableExistsStatement = new RawSqlStatement(query);
            int res = Scope.getCurrentScope().getSingleton(ExecutorService.class)
                    .getExecutor("jdbc", database)
                    .queryForInt(tableExistsStatement);
            return res == 1 ? true : false;
        } catch (DatabaseException e) {
            return false;
        }
    }
}

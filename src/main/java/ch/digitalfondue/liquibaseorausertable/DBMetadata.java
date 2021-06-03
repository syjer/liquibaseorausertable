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

        RawSqlStatement tableMetadataInfoStatement = new RawSqlStatement("SELECT * FROM USER_TAB_COLS WHERE upper(TABLE_NAME) = upper('" + tableName + "')");
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

    // imported from ColumnSnapshotGenerator
    private static DataType extractType(Map<String, ?> columnMetadataResultSet) {
        String dataType = getStringFromMetadata(columnMetadataResultSet, "DATA_TYPE_NAME");
        dataType = dataType.replace("VARCHAR2", "VARCHAR");
        dataType = dataType.replace("NVARCHAR2", "NVARCHAR");

        DataType type = new DataType(dataType);
        type.setDataTypeId(getIntegerFromMetadata(columnMetadataResultSet, "DATA_TYPE"));
        if (dataType.equalsIgnoreCase("NUMBER")) {
            type.setColumnSize(getIntegerFromMetadata(columnMetadataResultSet, "DATA_PRECISION"));
//                if (type.getColumnSize() == null) {
//                    type.setColumnSize(38);
//                }
            type.setDecimalDigits(getIntegerFromMetadata(columnMetadataResultSet, "DATA_SCALE"));
//                if (type.getDecimalDigits() == null) {
//                    type.setDecimalDigits(0);
//                }
//            type.setRadix(10);
        } else {
            if ("FLOAT".equalsIgnoreCase(dataType)) { //FLOAT [(precision)]
                type.setColumnSize(getIntegerFromMetadata(columnMetadataResultSet, "DATA_PRECISION"));
            } else {
                type.setColumnSize(getIntegerFromMetadata(columnMetadataResultSet, "DATA_LENGTH"));
            }

            boolean isTimeStampDataType = dataType.toUpperCase().contains("TIMESTAMP");

            if (isTimeStampDataType || dataType.equalsIgnoreCase("NCLOB") || dataType.equalsIgnoreCase("BLOB") || dataType.equalsIgnoreCase("CLOB")) {
                type.setColumnSize(null);
            } else if (dataType.equalsIgnoreCase("NVARCHAR") || dataType.equalsIgnoreCase("NCHAR")) {
                type.setColumnSize(getIntegerFromMetadata(columnMetadataResultSet, "CHAR_LENGTH"));
                type.setColumnSizeUnit(DataType.ColumnSizeUnit.CHAR);
            } else {
                String charUsed = getStringFromMetadata(columnMetadataResultSet, "CHAR_USED");
                DataType.ColumnSizeUnit unit = null;
                if ("C".equals(charUsed)) {
                    unit = DataType.ColumnSizeUnit.CHAR;
                    type.setColumnSize(getIntegerFromMetadata(columnMetadataResultSet,"CHAR_LENGTH"));
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
        try {
            RawSqlStatement tableExistsStatement = new RawSqlStatement("SELECT CASE WHEN EXISTS (SELECT * FROM user_tables WHERE upper(table_name) = upper('" + tableName + "')) THEN 1 ELSE 0 END FROM dual");
            int res = Scope.getCurrentScope().getSingleton(ExecutorService.class)
                    .getExecutor("jdbc", database)
                    .queryForInt(tableExistsStatement);
            return res == 1 ? true : false;
        } catch (DatabaseException e) {
            return false;
        }
    }
}

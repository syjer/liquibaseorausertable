package ch.digitalfondue.liquibaseorausertable;

import com.p6spy.engine.common.StatementInformation;
import com.p6spy.engine.event.JdbcEventListener;
import com.p6spy.engine.spy.DefaultJdbcEventListenerFactory;
import com.p6spy.engine.spy.JdbcEventListenerFactory;
import com.p6spy.engine.spy.P6DataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TestMigration {

    // https://github.com/testcontainers/testcontainers-java/issues/2313 see
    static {
        System.setProperty("oracle.jdbc.timezoneAsRegion", "false");
    }

    @Test
    public void testSimple() throws LiquibaseException, SQLException {
        List<String> queries = new ArrayList<>();
        P6DataSource ds = getDataSource(queries);
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(ds.getConnection()));
        Liquibase liquibase = new Liquibase("db-changelog.xml", new FileSystemResourceAccessor(new File("./src/test/migration/")), database);
        liquibase.update((String) null);
        Assertions.assertFalse(OraUserTableConfiguration.HAS_TABLE_NAMED_QUERY_IN_ALL_TABLES.getCurrentValue());
        Assertions.assertFalse(queries.stream().anyMatch(s -> s.contains("/* table_named_query_all */")));
        Assertions.assertTrue(queries.stream().anyMatch(s -> s.contains("/* table_named_query_user */")));
    }

    @Test
    public void testSwitchAllTables() throws LiquibaseException, SQLException {
        try {
            System.setProperty("orausertable.hasTableNamedQueryInAll", "true");
            List<String> queries = new ArrayList<>();
            P6DataSource ds = getDataSource(queries);
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(ds.getConnection()));
            Liquibase liquibase = new Liquibase("db-changelog.xml", new FileSystemResourceAccessor(new File("./src/test/migration/")), database);
            liquibase.update((String) null);
            Assertions.assertTrue(OraUserTableConfiguration.HAS_TABLE_NAMED_QUERY_IN_ALL_TABLES.getCurrentValue());
            Assertions.assertTrue(queries.stream().anyMatch(s -> s.contains("/* table_named_query_all */")));
            Assertions.assertFalse(queries.stream().anyMatch(s -> s.contains("/* table_named_query_user */")));
        } finally {
            System.clearProperty("orausertable.hasTableNamedQueryInAll");
        }
    }

    private static P6DataSource getDataSource(List<String> queries) {
        HikariConfig conf = new HikariConfig();
        conf.setJdbcUrl("jdbc:oracle:thin:@localhost:1521:xe");
        conf.setUsername("test");
        conf.setPassword("test");
        conf.setDriverClassName("oracle.jdbc.OracleDriver");
        P6DataSource ds = new P6DataSource(new HikariDataSource(conf));
        ds.setJdbcEventListenerFactory(() -> new JdbcEventListener() {
            @Override
            public void onBeforeExecute(StatementInformation statementInformation, String sql) {
                queries.add(sql);
            }

            @Override
            public void onBeforeExecuteQuery(StatementInformation statementInformation, String sql) {
                queries.add(sql);
            }
        });
        return ds;
    }
}

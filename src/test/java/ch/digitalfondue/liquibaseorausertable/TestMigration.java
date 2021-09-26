package ch.digitalfondue.liquibaseorausertable;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.File;
import java.sql.SQLException;

public class TestMigration {

    @Test
    public void testSimple() throws LiquibaseException, SQLException {
        DataSource ds = getDataSource();
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(ds.getConnection()));
        Liquibase liquibase = new Liquibase("db-changelog.xml", new FileSystemResourceAccessor(new File("./src/test/migration/")), database);
        liquibase.update((String) null);
    }

    private static DataSource getDataSource() {
        HikariConfig conf = new HikariConfig();
        conf.setJdbcUrl("jdbc:oracle:thin:@localhost:1521:xe");
        conf.setUsername("test");
        conf.setPassword("test");
        conf.setDriverClassName("oracle.jdbc.OracleDriver");
        return new HikariDataSource(conf);
    }
}

package ch.digitalfondue.liquibaseorausertable;

import liquibase.configuration.AutoloadedConfigurations;
import liquibase.configuration.ConfigurationDefinition;

public class OraUserTableConfiguration implements AutoloadedConfigurations {

    // if true, it will search the table inside the ALL_TABLES table
    // see issue https://github.com/syjer/liquibaseorausertable/issues/1
    public static ConfigurationDefinition<Boolean> HAS_TABLE_NAMED_QUERY_IN_ALL_TABLES;

    static {
        ConfigurationDefinition.Builder builder = new ConfigurationDefinition.Builder("orausertable");
        HAS_TABLE_NAMED_QUERY_IN_ALL_TABLES = builder.define("hasTableNamedQueryInAll", Boolean.class)
                .setDescription("Switch for checking if a table exist in the ALL_TABLES instead of USER_TABLES")
                .setDefaultValue(false)
                .build();
    }
}

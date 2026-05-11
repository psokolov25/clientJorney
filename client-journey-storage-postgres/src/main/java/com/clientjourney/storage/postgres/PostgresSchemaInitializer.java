package com.clientjourney.storage.postgres;

import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Requires(property = "client-journey.storage.type", value = "postgres")
@Singleton
public class PostgresSchemaInitializer {
    public PostgresSchemaInitializer(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                create table if not exists scenarios (
                    id uuid primary key,
                    code varchar(255) not null unique,
                    name varchar(255) not null,
                    description text,
                    status varchar(50) not null,
                    version int not null,
                    created_at timestamp with time zone not null,
                    updated_at timestamp with time zone not null
                )
                """);
            statement.executeUpdate("""
                create table if not exists scenario_graphs (
                    scenario_id uuid not null,
                    version int not null,
                    nodes_json text not null,
                    edges_json text not null,
                    primary key (scenario_id, version)
                )
                """);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize Postgres schema", e);
        }
    }
}

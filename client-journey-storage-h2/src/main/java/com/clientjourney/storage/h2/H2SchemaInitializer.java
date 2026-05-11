package com.clientjourney.storage.h2;

import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Requires(property = "client-journey.storage.type", value = "h2")
@Singleton
public class H2SchemaInitializer {
    public H2SchemaInitializer(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                create table if not exists scenarios (
                    id uuid primary key,
                    code varchar(255) not null unique,
                    name varchar(255) not null,
                    description clob,
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
                    nodes_json clob not null,
                    edges_json clob not null,
                    primary key (scenario_id, version)
                )
                """);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize H2 schema", e);
        }
    }
}

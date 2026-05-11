package com.clientjourney.storage.postgres;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

@Factory
@Requires(property = "client-journey.storage.type", value = "postgres")
public class PostgresDataSourceFactory {
    @Singleton
    public DataSource dataSource(
        @Value("${datasources.default.url}") String url,
        @Value("${datasources.default.username}") String username,
        @Value("${datasources.default.password}") String password
    ) {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(url);
        dataSource.setUser(username);
        dataSource.setPassword(password);
        return dataSource;
    }
}

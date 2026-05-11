package com.clientjourney.storage.h2;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;

@Factory
@Requires(property = "client-journey.storage.type", value = "h2")
public class H2DataSourceFactory {
    @Singleton
    public DataSource dataSource(
        @Value("${datasources.default.url}") String url,
        @Value("${datasources.default.username:sa}") String username,
        @Value("${datasources.default.password:}") String password
    ) {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL(url);
        dataSource.setUser(username);
        dataSource.setPassword(password);
        return dataSource;
    }
}

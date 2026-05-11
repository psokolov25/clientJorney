package com.clientjourney.storage.h2;

import com.clientjourney.domain.model.Scenario;
import com.clientjourney.domain.model.ScenarioStatus;
import com.clientjourney.storage.spi.ScenarioRepository;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Requires(property = "client-journey.storage.type", value = "h2")
@Singleton
public class H2ScenarioRepository implements ScenarioRepository {
    private final DataSource dataSource;

    public H2ScenarioRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Scenario save(Scenario scenario) {
        String sql = """
            merge into scenarios (id, code, name, description, status, version, created_at, updated_at)
            key (id)
            values (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, scenario.id());
            statement.setString(2, scenario.code());
            statement.setString(3, scenario.name());
            statement.setString(4, scenario.description());
            statement.setString(5, scenario.status().name());
            statement.setInt(6, scenario.version());
            statement.setTimestamp(7, Timestamp.from(scenario.createdAt()));
            statement.setTimestamp(8, Timestamp.from(scenario.updatedAt()));
            statement.executeUpdate();
            return scenario;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save scenario", e);
        }
    }

    @Override
    public Optional<Scenario> findById(UUID id) {
        String sql = "select * from scenarios where id = ?";
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find scenario by id", e);
        }
    }

    @Override
    public Optional<Scenario> findByCode(String code) {
        String sql = "select * from scenarios where code = ?";
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, code);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find scenario by code", e);
        }
    }

    @Override
    public List<Scenario> findAll() {
        String sql = "select * from scenarios";
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet rs = statement.executeQuery()) {
            List<Scenario> scenarios = new ArrayList<>();
            while (rs.next()) {
                scenarios.add(map(rs));
            }
            return scenarios;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list scenarios", e);
        }
    }

    @Override
    public void deleteById(UUID id) {
        String sql = "delete from scenarios where id = ?";
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete scenario", e);
        }
    }

    private Scenario map(ResultSet rs) throws SQLException {
        return new Scenario(
            rs.getObject("id", UUID.class),
            rs.getString("code"),
            rs.getString("name"),
            rs.getString("description"),
            ScenarioStatus.valueOf(rs.getString("status")),
            rs.getInt("version"),
            toInstant(rs.getTimestamp("created_at")),
            toInstant(rs.getTimestamp("updated_at"))
        );
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp.toInstant();
    }
}

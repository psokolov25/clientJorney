package com.clientjourney.storage.postgres;

import com.clientjourney.domain.model.ScenarioEdge;
import com.clientjourney.domain.model.ScenarioGraph;
import com.clientjourney.domain.model.ScenarioNode;
import com.clientjourney.storage.spi.ScenarioGraphRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Requires(property = "client-journey.storage.type", value = "postgres")
@Singleton
public class PostgresScenarioGraphRepository implements ScenarioGraphRepository {
    private final DataSource dataSource;
    private final ObjectMapper objectMapper;

    public PostgresScenarioGraphRepository(DataSource dataSource, ObjectMapper objectMapper) {
        this.dataSource = dataSource;
        this.objectMapper = objectMapper;
    }

    @Override
    public ScenarioGraph save(ScenarioGraph graph) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(upsertSql(connection))) {
            statement.setObject(1, graph.scenarioId());
            statement.setInt(2, graph.version());
            statement.setString(3, objectMapper.writeValueAsString(graph.nodes()));
            statement.setString(4, objectMapper.writeValueAsString(graph.edges()));
            statement.executeUpdate();
            return graph;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to save scenario graph", e);
        }
    }

    private String upsertSql(Connection connection) throws SQLException {
        String productName = connection.getMetaData().getDatabaseProductName();
        if (productName != null && productName.toLowerCase().contains("h2")) {
            return """
                merge into scenario_graphs (scenario_id, version, nodes_json, edges_json)
                key (scenario_id, version)
                values (?, ?, ?, ?)
                """;
        }
        return """
            insert into scenario_graphs (scenario_id, version, nodes_json, edges_json)
            values (?, ?, ?::jsonb, ?::jsonb)
            on conflict (scenario_id, version) do update set
              nodes_json = excluded.nodes_json,
              edges_json = excluded.edges_json
            """;
    }

    @Override
    public Optional<ScenarioGraph> findByScenarioIdAndVersion(UUID scenarioId, int version) {
        String sql = "select * from scenario_graphs where scenario_id = ? and version = ?";
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, scenarioId);
            statement.setInt(2, version);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find scenario graph", e);
        }
    }

    private ScenarioGraph map(ResultSet rs) {
        try {
            List<ScenarioNode> nodes = objectMapper.readValue(rs.getString("nodes_json"), new TypeReference<>() {});
            List<ScenarioEdge> edges = objectMapper.readValue(rs.getString("edges_json"), new TypeReference<>() {});
            return new ScenarioGraph(rs.getObject("scenario_id", UUID.class), rs.getInt("version"), nodes, edges);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to map scenario graph", e);
        }
    }
}

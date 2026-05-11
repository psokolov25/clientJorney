package com.clientjourney.storage.h2;

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

@Requires(property = "client-journey.storage.type", value = "h2")
@Singleton
public class H2ScenarioGraphRepository implements ScenarioGraphRepository {
    private final DataSource dataSource;
    private final ObjectMapper objectMapper;

    public H2ScenarioGraphRepository(DataSource dataSource, ObjectMapper objectMapper) {
        this.dataSource = dataSource;
        this.objectMapper = objectMapper;
    }

    @Override
    public ScenarioGraph save(ScenarioGraph graph) {
        String sql = """
            merge into scenario_graphs (scenario_id, version, nodes_json, edges_json)
            key (scenario_id, version)
            values (?, ?, ?, ?)
            """;
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
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

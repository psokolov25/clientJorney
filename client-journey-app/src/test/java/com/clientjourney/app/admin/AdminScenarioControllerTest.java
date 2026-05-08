package com.clientjourney.app.admin;

import com.clientjourney.app.admin.dto.CreateScenarioRequest;
import com.clientjourney.app.admin.dto.UpdateScenarioRequest;
import com.clientjourney.app.repository.InMemoryScenarioRepository;
import com.clientjourney.app.service.ScenarioService;
import com.clientjourney.domain.model.Scenario;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AdminScenarioControllerTest {

    @Test
    void shouldCreateAndFetchScenario() {
        AdminScenarioController controller = new AdminScenarioController(new ScenarioService(new InMemoryScenarioRepository()));

        Scenario created = controller.create(new CreateScenarioRequest("medical-registration", "Medical", "desc"));
        Scenario loaded = controller.findById(created.id());

        assertEquals(created.id(), loaded.id());
        assertEquals("medical-registration", loaded.code());
        assertEquals("DRAFT", loaded.status().name());
    }

    @Test
    void shouldListUpdateAndDeleteScenario() {
        AdminScenarioController controller = new AdminScenarioController(new ScenarioService(new InMemoryScenarioRepository()));

        Scenario created = controller.create(new CreateScenarioRequest("test-code", "Name", "desc"));
        List<Scenario> all = controller.findAll();
        assertEquals(1, all.size());

        Scenario updated = controller.update(created.id(), new UpdateScenarioRequest("New name", "New desc"));
        assertEquals("New name", updated.name());

        controller.delete(created.id());
        assertTrue(controller.findAll().isEmpty());
    }

    @Test
    void shouldPublishArchiveAndCloneVersion() {
        AdminScenarioController controller = new AdminScenarioController(new ScenarioService(new InMemoryScenarioRepository()));
        Scenario created = controller.create(new CreateScenarioRequest("flow-1", "Flow", "desc"));

        Scenario published = controller.publish(created.id());
        assertEquals("PUBLISHED", published.status().name());
        assertEquals(1, published.version());

        Scenario archived = controller.archive(created.id());
        assertEquals("ARCHIVED", archived.status().name());

        Scenario cloned = controller.cloneVersion(created.id());
        assertEquals("DRAFT", cloned.status().name());
        assertEquals(2, cloned.version());
    }
}

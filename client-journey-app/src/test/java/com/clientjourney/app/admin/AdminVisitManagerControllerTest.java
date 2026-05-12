package com.clientjourney.app.admin;

import com.clientjourney.app.admin.dto.VisitManagerServiceDto;
import com.clientjourney.app.service.VisitManagerServiceCatalogService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdminVisitManagerControllerTest {

    @Test
    void should_return_service_catalog() {
        AdminVisitManagerController controller = new AdminVisitManagerController(new VisitManagerServiceCatalogService());
        List<VisitManagerServiceDto> services = controller.listServices("http://visitmanager:8080", "branch-1", "all");

        assertEquals(2, services.size());
        assertEquals("CONSULT", services.get(0).code());
    }
}

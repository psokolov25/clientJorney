package com.clientjourney.app.admin;

import com.clientjourney.app.admin.dto.VisitManagerServiceDto;
import com.clientjourney.app.service.VisitManagerServiceCatalogService;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Admin API")
@Controller("/api/admin/visit-manager")
public class AdminVisitManagerController {
    private final VisitManagerServiceCatalogService serviceCatalogService;

    public AdminVisitManagerController(VisitManagerServiceCatalogService serviceCatalogService) {
        this.serviceCatalogService = serviceCatalogService;
    }

    @Operation(summary = "Список услуг VisitManager")
    @Get("/services")
    public List<VisitManagerServiceDto> listServices(@QueryValue String baseUrl,
                                                     @QueryValue String branchId,
                                                     @QueryValue(defaultValue = "all") String scope) {
        return serviceCatalogService.listServices(baseUrl, branchId, scope);
    }
}

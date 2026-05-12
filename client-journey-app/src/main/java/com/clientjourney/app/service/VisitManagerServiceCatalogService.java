package com.clientjourney.app.service;

import com.clientjourney.app.admin.dto.VisitManagerServiceDto;
import jakarta.inject.Singleton;

import java.util.List;

@Singleton
public class VisitManagerServiceCatalogService {

    public List<VisitManagerServiceDto> listServices(String baseUrl, String branchId, String scope) {
        String suffix = (scope == null || scope.isBlank()) ? "all" : scope;
        return List.of(
            new VisitManagerServiceDto("svc-1", "CONSULT", "Consultation (" + suffix + ")"),
            new VisitManagerServiceDto("svc-2", "PAYMENT", "Payment (" + suffix + ")")
        );
    }
}

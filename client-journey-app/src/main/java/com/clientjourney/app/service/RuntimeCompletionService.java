package com.clientjourney.app.service;

import com.clientjourney.app.dto.OutputMessage;
import com.clientjourney.app.dto.SelectedServiceDto;
import com.clientjourney.app.dto.StartSessionResponse;
import com.clientjourney.app.dto.VisitCreationInfo;
import com.clientjourney.visit.mock.DryRunVisitCreationClient;
import com.clientjourney.visit.spi.VisitCreationRequest;
import com.clientjourney.visit.spi.VisitCreationResult;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Singleton
public class RuntimeCompletionService {
    private final ServiceSelectionProcessor serviceSelectionProcessor;
    private final DryRunVisitCreationClient dryRunVisitCreationClient;

    public RuntimeCompletionService(
        ServiceSelectionProcessor serviceSelectionProcessor,
        DryRunVisitCreationClient dryRunVisitCreationClient
    ) {
        this.serviceSelectionProcessor = serviceSelectionProcessor;
        this.dryRunVisitCreationClient = dryRunVisitCreationClient;
    }

    public StartSessionResponse selectServicesAndComplete(UUID sessionId, List<SelectedServiceDto> selectedServices) {
        OutputMessage outputMessage = serviceSelectionProcessor.processSelectedServices(sessionId, selectedServices);

        VisitCreationResult visit = dryRunVisitCreationClient.createVisit(new VisitCreationRequest(
            sessionId,
            "medical-registration",
            1,
            "REST",
            "anonymous",
            Map.of("selectedServicesCount", String.valueOf(selectedServices == null ? 0 : selectedServices.size()))
        ));

        VisitCreationInfo visitInfo = new VisitCreationInfo(
            visit.status().name(),
            visit.clientType(),
            visit.externalVisitId(),
            visit.externalTicket(),
            visit.errorCode(),
            visit.errorMessage()
        );

        return new StartSessionResponse(sessionId.toString(), null, "COMPLETED", outputMessage, visitInfo);
    }
}

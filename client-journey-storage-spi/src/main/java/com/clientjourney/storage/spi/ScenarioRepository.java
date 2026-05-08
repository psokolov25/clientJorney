package com.clientjourney.storage.spi;

import com.clientjourney.domain.model.Scenario;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScenarioRepository {
    Scenario save(Scenario scenario);

    Optional<Scenario> findById(UUID id);

    Optional<Scenario> findByCode(String code);

    List<Scenario> findAll();

    void deleteById(UUID id);
}

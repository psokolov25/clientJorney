package com.clientjourney.app;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(packages = "com.clientjourney.app", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule app_layers_must_be_respected = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Controller").definedBy("com.clientjourney.app..")
            .layer("Service").definedBy("com.clientjourney.app.service..")
            .layer("Repository").definedBy("com.clientjourney.app.repository..")
            .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller")
            .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service");

}

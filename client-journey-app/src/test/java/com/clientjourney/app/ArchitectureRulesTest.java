package com.clientjourney.app;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.clientjourney.app", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureRulesTest {

    @ArchTest
    static final ArchRule controllers_should_not_depend_on_repositories =
            noClasses()
                    .that().resideInAnyPackage("..app", "..app.admin")
                    .and().haveSimpleNameEndingWith("Controller")
                    .should().dependOnClassesThat().resideInAnyPackage("..repository..");

    @ArchTest
    static final ArchRule dto_should_not_depend_on_services_or_repositories =
            noClasses()
                    .that().resideInAnyPackage("..app.dto..", "..app.admin.dto..")
                    .should().dependOnClassesThat().resideInAnyPackage("..app.service..", "..repository..");

    @ArchTest
    static final ArchRule services_should_not_depend_on_controller_layer =
            noClasses()
                    .that().resideInAnyPackage("..app.service..")
                    .should().dependOnClassesThat().haveSimpleNameEndingWith("Controller");
}

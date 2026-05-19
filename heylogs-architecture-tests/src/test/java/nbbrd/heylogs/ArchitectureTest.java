package nbbrd.heylogs;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import nbbrd.heylogs.spi.Format;
import nbbrd.heylogs.spi.Forge;
import nbbrd.heylogs.spi.HttpFactory;
import nbbrd.heylogs.spi.Tagging;
import nbbrd.heylogs.spi.Versioning;
import nbbrd.service.ServiceId;
import nbbrd.service.ServiceProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.core.domain.JavaModifier.FINAL;
import static com.tngtech.archunit.core.domain.JavaModifier.PUBLIC;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Global architecture tests using ArchUnit to enforce documented conventions
 * across all Heylogs modules.
 * <p>
 * This test module depends on all other modules, ensuring complete classpath
 * visibility for comprehensive architecture validation.
 *
 * @see <a href="https://www.archunit.org/">ArchUnit</a>
 */
public class ArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("nbbrd.heylogs", "internal.heylogs");
    }

    /**
     * Rule 1: Internal packages must not be accessed from outside their own module.
     * <p>
     * AGENTS.md: "Internal implementation lives under internal.* packages —
     * never reference these types from public API."
     * <p>
     * This rule specifically prevents cross-module violations where extension modules
     * access internal classes from heylogs-api. Within-module access to internal
     * packages is permitted (e.g., CalVer extension can use internal.heylogs.ext.calver.*).
     */
    @Test
    void internalPackagesShouldNotBeAccessedFromOutsideTheirModule() {
        // Extension modules (nbbrd.heylogs.ext.*) should not access internal classes
        // from heylogs-api (internal.heylogs.* but NOT internal.heylogs.ext.*)
        ArchRule rule = classes()
                .that().resideInAPackage("..ext..")
                .should().onlyAccessClassesThat()
                .resideOutsideOfPackages("internal.heylogs.base..", "internal.heylogs.git..", 
                        "internal.heylogs.spi..", "internal.heylogs.maven..", "internal.heylogs.cli..");

        rule.check(classes);
    }

    /**
     * Rule 2: SPI implementations must be annotated with @ServiceProvider.
     * <p>
     * AGENTS.md: "Each implements one or more SPI via @ServiceProvider + @DirectImpl"
     * <p>
     * Note: @DirectImpl has @Retention(SOURCE) so cannot be checked at runtime.
     * *Support classes are builders/helpers, not actual implementations.
     * Rule interface is excluded because its implementations are internal enums, not extensions.
     */
    @Test
    void spiImplementationsMustHaveServiceProviderAnnotation() {
        ArchRule rule = classes()
                .that().implement(Forge.class)
                .or().implement(Versioning.class)
                .or().implement(Format.class)
                .or().implement(Tagging.class)
                .or().implement(HttpFactory.class)
                .and().areNotInterfaces()
                .and().areNotMemberClasses() // Exclude inner classes like Batch classes
                .and().haveSimpleNameNotEndingWith("Support") // Exclude *Support builder classes
                .and().resideOutsideOfPackages("internal..") // Exclude internal fallback implementations
                .should().beAnnotatedWith(ServiceProvider.class);

        rule.check(classes);
    }

    /**
     * Rule 3: SPI extension classes must be final and public.
     * <p>
     * AGENTS.md: Extension modules should create a single public final class
     * that implements the SPI interface.
     * <p>
     * This rule validates all extension modules since they are on the classpath
     * of this dedicated architecture test module.
     */
    @Test
    void spiExtensionsShouldBeFinal() {
        ArchRule rule = classes()
                .that().implement(Forge.class)
                .or().implement(Versioning.class)
                .or().implement(Format.class)
                .or().implement(Tagging.class)
                .or().implement(HttpFactory.class)
                .and().areNotInterfaces()
                .and().areNotMemberClasses() // Exclude inner classes like Batch classes
                .and().resideInAPackage("..ext..")
                .should().haveModifier(FINAL)
                .andShould().haveModifier(PUBLIC);

        rule.check(classes);
    }

    /**
     * Rule 4: Support classes in SPI package must be public and final.
     * <p>
     * AGENTS.md: "*Support classes are builders/helpers" that provide builder pattern
     * for SPI implementations. They should be public (for use by extensions) and final
     * (not meant to be extended).
     */
    @Test
    void supportClassesShouldBeFinalAndPublic() {
        ArchRule rule = classes()
                .that().resideInAPackage("..spi..")
                .and().haveSimpleNameEndingWith("Support")
                .and().areNotInterfaces()
                .should().haveModifier(FINAL)
                .andShould().haveModifier(PUBLIC);

        rule.check(classes);
    }

    /**
     * Rule 5: SPI ID getter methods must be annotated with @ServiceId(pattern = ServiceId.KEBAB_CASE).
     * <p>
     * AGENTS.md: "IDs must follow ServiceId.KEBAB_CASE (enforced by @ServiceId(pattern = ServiceId.KEBAB_CASE)
     * on the SPI interface)"
     * <p>
     * This validates that all SPI interfaces properly declare the ID pattern constraint.
     * Rule interface is excluded because its ID method pattern is validated, but implementations
     * are internal enums rather than extension modules.
     */
    @Test
    void spiIdMethodsMustHaveServiceIdAnnotation() {
        ArchRule rule = com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods()
                .that().haveName("getForgeId")
                .or().haveName("getFormatId")
                .or().haveName("getVersioningId")
                .or().haveName("getTaggingId")
                .or().haveName("getRuleId")
                .and().areDeclaredInClassesThat().areInterfaces()
                .and().areDeclaredInClassesThat().resideInAPackage("..spi..")
                .should().beAnnotatedWith(ServiceId.class);

        rule.check(classes);
    }
}


package dk.sunepoulsen.tes.springboot.template.stresstest

import dk.sunepoulsen.tes.docker.containers.TESBackendContainer
import groovy.util.logging.Slf4j
import org.spockframework.runtime.extension.IGlobalExtension
import org.spockframework.runtime.model.SpecInfo
import org.testcontainers.containers.GenericContainer

@Slf4j
class DeploymentSpockExtension implements IGlobalExtension {
    private static TESBackendContainer templateBackendContainer = null

    static GenericContainer templateBackendContainer() {
        return templateBackendContainer
    }

    @Override
    void start() {
        templateBackendContainer = new TESBackendContainer('tes-spring-boot-template-backend-service', '1.0.0-SNAPSHOT', 'stress-test')
        templateBackendContainer.start()

        log.info('Template Backend Exported Port: {}', templateBackendContainer.getMappedPort(8080))
    }

    @Override
    void visitSpec(SpecInfo spec) {
    }

    @Override
    void stop() {
        templateBackendContainer.copyLogFile('build/logs/tes-spring-boot-template-backend-service.log')
        templateBackendContainer.stop()
    }
}

package dk.sunepoulsen.tes.springboot.template.stresstest

import dk.sunepoulsen.tes.docker.containers.TESBackendContainer
import groovy.util.logging.Slf4j
import org.spockframework.runtime.extension.IGlobalExtension
import org.spockframework.runtime.model.SpecInfo
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.utility.DockerImageName

@Slf4j
class DeploymentSpockExtension implements IGlobalExtension {
    private static TESBackendContainer templateBackendContainer = null
    private static GenericContainer postgresqlContainer = null

    static GenericContainer templateBackendContainer() {
        return templateBackendContainer
    }

    @Override
    void start() {
        DockerImageName imageName

        Network network = Network.newNetwork()

        imageName = DockerImageName.parse('postgres:latest')
        postgresqlContainer = new GenericContainer<>(imageName)
            .withEnv('POSTGRES_DB', 'stress-test')
            .withEnv('POSTGRES_USER', 'template')
            .withEnv('POSTGRES_PASSWORD', 'jukilo90')
            .withExposedPorts(5432)
            .withNetwork(network)
            .withNetworkAliases('postgres')
        postgresqlContainer.start()

        templateBackendContainer = new TESBackendContainer('tes-spring-boot-template-backend-service', '1.0.0-SNAPSHOT', 'stress-test')
            .withConfigMapping('config/application-stress-test.yml')
            .withNetwork(network)
        templateBackendContainer.start()

        log.info('Template Postgres Exported Port: {}', postgresqlContainer.getMappedPort(5432))
        log.info('Template Backend Exported Port: {}', templateBackendContainer.getMappedPort(8080))
    }

    @Override
    void visitSpec(SpecInfo spec) {
    }

    @Override
    void stop() {
        templateBackendContainer.copyLogFile('build/logs/tes-spring-boot-template-backend-service.log')
        templateBackendContainer.stop()

        postgresqlContainer.stop()
    }
}

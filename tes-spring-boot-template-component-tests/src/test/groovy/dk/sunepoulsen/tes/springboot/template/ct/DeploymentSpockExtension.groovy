package dk.sunepoulsen.tes.springboot.template.ct

import dk.sunepoulsen.tes.docker.containers.TESBackendContainer
import dk.sunepoulsen.tes.springboot.template.client.rs.TemplateIntegrator
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import org.spockframework.runtime.extension.IGlobalExtension
import org.spockframework.runtime.model.SpecInfo
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.utility.DockerImageName

@Slf4j
class DeploymentSpockExtension implements IGlobalExtension {
    private static final String PG_DATABASE = 'ct'
    private static final String PG_USER = 'template'
    private static final String PG_PASSWORD = 'jukilo90'
    private static final String PG_DRIVER = 'org.postgresql.Driver'

    private static TESBackendContainer templateBackendContainer = null
    private static GenericContainer postgresqlContainer = null

    static TESBackendContainer templateBackendContainer() {
        return templateBackendContainer
    }

    static TemplateIntegrator templateBackendIntegrator() {
        return new TemplateIntegrator(templateBackendContainer.createClient())
    }

    static void clearDatabase() {
        String[] tableNames = ['templates']

        Integer port = postgresqlContainer.getMappedPort(5432)
        String dbUrl = "jdbc:postgresql://localhost:${port}/${PG_DATABASE}"
        Sql sql = Sql.newInstance(dbUrl, PG_USER, PG_PASSWORD, PG_DRIVER)

        log.info("Clear all tables in the database: {}", dbUrl)

        tableNames.each { it ->
            String executeSql = "DELETE FROM ${it}"
            log.debug("Execute SQL against: ${executeSql}")

            sql.execute(executeSql)
        }

        sql.close()
    }

    @Override
    void start() {
        DockerImageName imageName

        Network network = Network.newNetwork()

        imageName = DockerImageName.parse('postgres:latest')
        postgresqlContainer = new GenericContainer<>(imageName)
            .withEnv('POSTGRES_DB', PG_DATABASE)
            .withEnv('POSTGRES_USER', PG_USER)
            .withEnv('POSTGRES_PASSWORD', PG_PASSWORD)
            .withExposedPorts(5432)
            .withNetwork(network)
            .withNetworkAliases('postgres')
        postgresqlContainer.start()

        templateBackendContainer = new TESBackendContainer('tes-spring-boot-template-backend-service', '1.0.0-SNAPSHOT', 'ct')
            .withConfigMapping('application-ct.yml')
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

package dk.sunepoulsen.tes.holidays.ct

import dk.sunepoulsen.tes.rest.models.monitoring.ServiceHealth
import dk.sunepoulsen.tes.rest.models.monitoring.ServiceHealthStatusCode
import spock.lang.Specification

class ActuatorSpec extends Specification {

    void "GET /actuator/health returns OK"() {
        given: 'Holiday service is available'
            DeploymentSpockExtension.holidaysBackendContainer().isHostAccessible()

        when: 'Call GET /actuator/health'
            ServiceHealth result = DeploymentSpockExtension.holidaysBackendIntegrator().health().blockingGet()

        then: 'Verify health body'
            result == new ServiceHealth(
                status: ServiceHealthStatusCode.UP
            )
    }
}

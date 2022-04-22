package dk.sunepoulsen.tes.springboot.template.ct

import dk.sunepoulsen.tes.rest.models.monitoring.ServiceHealth
import dk.sunepoulsen.tes.rest.models.monitoring.ServiceHealthStatusCode
import spock.lang.Specification

class ActuatorSpec extends Specification {

    void "GET /actuator/health returns OK"() {
        given: 'Template service is available'
            DeploymentSpockExtension.templateBackendContainer().isHostAccessible()

        when: 'Call GET /actuator/health'
            ServiceHealth result = DeploymentSpockExtension.templateBackendIntegrator().health().blockingGet()

        then: 'Verify health body'
            result == new ServiceHealth(
                status: ServiceHealthStatusCode.UP
            )
    }
}

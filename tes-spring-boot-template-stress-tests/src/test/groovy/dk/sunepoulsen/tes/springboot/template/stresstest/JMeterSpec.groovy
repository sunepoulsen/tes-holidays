package dk.sunepoulsen.tes.springboot.template.stresstest

import dk.sunepoulsen.tes.jmeter.JMeterExecutor
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import spock.lang.Specification

@Slf4j
class JMeterSpec extends Specification {

    void "Execute JMeter Test"() {
        given: 'Health service is available'
            DeploymentSpockExtension.templateBackendContainer().isHostAccessible()

        and: 'JMeter executor is prepared'
            JMeterExecutor executor = new JMeterExecutor()
            executor.prepareExecutor(DeploymentSpockExtension.templateBackendContainer())

        when: 'Execute JMeter test'
            Boolean jMeterResult = executor.runTests()
            def statisticResults = new JsonSlurper().parse(JMeterExecutor.STATISTIC_RESULT_FILE)

        then: 'Verify stress test result'
            jMeterResult
            statisticResults.Total.errorPct == 0.0
    }

}

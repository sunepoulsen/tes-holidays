package dk.sunepoulsen.tes.springboot.template.ct

import dk.sunepoulsen.tes.http.HttpHelper
import dk.sunepoulsen.tes.http.HttpResponseVerificator
import spock.lang.Specification

import java.net.http.HttpRequest

class SwaggerSpec extends Specification {

    void "GET /swagger-ui.html returns OK"() {
        given: 'Template service is available'
            DeploymentSpockExtension.templateBackendContainer().isHostAccessible()
            String baseUrl = "http://${DeploymentSpockExtension.templateBackendContainer().host}:${DeploymentSpockExtension.templateBackendContainer().getMappedPort(8080)}"

        when: 'Call GET /swagger-ui.html'
            HttpHelper httpHelper = new HttpHelper()
            HttpRequest httpRequest = httpHelper.newRequestBuilder(DeploymentSpockExtension.templateBackendContainer(),"/swagger-ui.html")
                .GET()
                .build()

            HttpResponseVerificator verificator = httpHelper.sendRequest(httpRequest)

        then: 'Response Code is 200'
            verificator.responseCode(200)

        and: 'Content Type is text/html'
            verificator.contentType('text/html')
    }
}

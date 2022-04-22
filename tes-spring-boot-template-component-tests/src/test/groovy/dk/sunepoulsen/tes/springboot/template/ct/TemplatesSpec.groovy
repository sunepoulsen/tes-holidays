package dk.sunepoulsen.tes.springboot.template.ct

import dk.sunepoulsen.tes.http.HttpHelper
import dk.sunepoulsen.tes.http.HttpResponseVerificator
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientBadRequestException
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientNotFoundException
import dk.sunepoulsen.tes.rest.models.PaginationModel
import dk.sunepoulsen.tes.rest.models.ServiceErrorModel
import dk.sunepoulsen.tes.springboot.template.client.rs.TemplateIntegrator
import dk.sunepoulsen.tes.springboot.template.client.rs.model.TemplateModel
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import spock.lang.Specification

import java.net.http.HttpRequest

class TemplatesSpec extends Specification {

    TemplateIntegrator integrator

    void setup() {
        this.integrator = DeploymentSpockExtension.templateBackendIntegrator()
        DeploymentSpockExtension.clearDatabase()
    }

    void "POST /templates returns OK"() {
        given: 'Template service is available'
            DeploymentSpockExtension.templateBackendContainer().isHostAccessible()

            TemplateModel model = new TemplateModel(
                name: 'name',
                description: 'description'
            )

        when: 'Call POST /templates'
            TemplateModel result = integrator.create(model).blockingGet()

        then: 'Verify health body'
            result.id > 0L
            result == new TemplateModel(
                id: result.id,
                name: 'name',
                description: 'description'
            )
    }

    void "POST /templates returns BAD_REQUEST"() {
        given: 'Template service is available'
            DeploymentSpockExtension.templateBackendContainer().isHostAccessible()

        when: 'Call POST /templates'
            integrator.create(
                new TemplateModel(
                    description: 'description'
                )
            ).blockingGet()

        then: 'Verify exception for bad_request'
            ClientBadRequestException exception = thrown(ClientBadRequestException)
            exception.serviceError == new ServiceErrorModel(
                param: 'name',
                message: 'must not be null'
            )
    }

    void "GET /templates with no sorting returns OK"() {
        given: 'Template service is available'
            DeploymentSpockExtension.templateBackendContainer().isHostAccessible()

            (1..5).each {
                integrator.create(new TemplateModel(
                    name: "name-${it}",
                    description: "description-${it}"
                )).blockingGet()
            }

        when: 'Call POST /templates'
            Pageable pageable = PageRequest.of(0, 20)
            PaginationModel<TemplateModel> result = integrator.findAll(pageable).blockingGet()

        then: 'Verify health body'
            result.metadata.page == 0
            result.metadata.size == 20
            result.metadata.totalPages == 1
            result.metadata.totalItems == 5
            result.results.size() == 5
            result.results[0].name == 'name-1'
    }

    void "GET /templates with sorting returns OK"() {
        given: 'Template service is available'
            DeploymentSpockExtension.templateBackendContainer().isHostAccessible()

            (1..5).each {
                integrator.create(new TemplateModel(
                    name: "name-${it}",
                    description: "description-${it}"
                )).blockingGet()
            }

        when: 'Call POST /templates'
            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, 'name'))
            PaginationModel<TemplateModel> result = integrator.findAll(pageable).blockingGet()

        then: 'Verify health body'
            result.metadata.page == 0
            result.metadata.size == 20
            result.metadata.totalPages == 1
            result.metadata.totalItems == 5
            result.results.size() == 5
            result.results[0].name == 'name-5'
    }

    void "GET /templates with bad sorting property"() {
        given: 'Template service is available'
            DeploymentSpockExtension.templateBackendContainer().isHostAccessible()

        when: 'Call POST /templates'
            Pageable pageable = PageRequest.of(0, 20, Sort.by('wrong'))
            PaginationModel<TemplateModel> result = integrator.findAll(pageable).blockingGet()

        then: 'Verify health body'
            ClientBadRequestException exception = thrown(ClientBadRequestException)
            exception.serviceError == new ServiceErrorModel(
                param: 'sort',
                message: 'Unknown sort property'
            )
    }

    void "GET /templates with bad query parameters"() {
        given: 'Template service is available'
            DeploymentSpockExtension.templateBackendContainer().isHostAccessible()
            String baseUrl = "http://${DeploymentSpockExtension.templateBackendContainer().host}:${DeploymentSpockExtension.templateBackendContainer().getMappedPort(8080)}"

            (1..5).each {
                integrator.create(new TemplateModel(
                    name: "name-${it}",
                    description: "description-${it}"
                )).blockingGet()
            }

        when: 'Call GET /templates'
            HttpHelper httpHelper = new HttpHelper()
            HttpRequest httpRequest = httpHelper.newRequestBuilder(DeploymentSpockExtension.templateBackendContainer(),"/templates?size=wrong&page=0")
                .GET()
                .build()

            HttpResponseVerificator verificator = httpHelper.sendRequest(httpRequest)

        then: 'Response Code is 200'
            verificator.responseCode(200)

        and: 'Content Type is json'
            verificator.contentType('application/json')

        and: 'Check error body'
            PaginationModel<TemplateModel> body = verificator.bodyAsJsonOfType(PaginationModel)
            body.metadata.page == 0
            body.metadata.size == 20
            body.metadata.totalPages == 1
            body.metadata.totalItems == 5
            body.results.size() == 5
            body.results[0].name == 'name-1'
    }

    void "GET /templates/{id}: Found"() {
        given: 'Template service is available'
            DeploymentSpockExtension.templateBackendContainer().isHostAccessible()

            TemplateModel model = integrator.create(new TemplateModel(
                name: "name",
                description: "description"
            )).blockingGet()

        when: 'Call GET /templates/{id}'
            TemplateModel result = integrator.get(model.id).blockingGet()

        then: 'Verify returned template'
            result == model
    }

    void "GET /templates/{id}: Not Found"() {
        given: 'Template service is available'
            DeploymentSpockExtension.templateBackendContainer().isHostAccessible()

        when: 'Call GET /templates/{id}'
            integrator.get(1L).blockingGet()

        then: 'Verify returned template'
            ClientNotFoundException exception = thrown(ClientNotFoundException)
            exception.serviceError == new ServiceErrorModel(
                param: 'id',
                message: 'The resource does not exist'
            )
    }

    void "PATCH /templates/{id}: Found and patched"() {
        given: 'Template service is available'
            DeploymentSpockExtension.templateBackendContainer().isHostAccessible()

            TemplateModel model = integrator.create(new TemplateModel(
                name: 'name',
                description: 'description'
            )).blockingGet()

        when: 'Call PATCH /templates/{id}'
            TemplateModel result = integrator.patch(model.id, new TemplateModel(name: 'new-name')).blockingGet()

        then: 'Verify returned template'
            result == new TemplateModel(
                id: model.id,
                name: 'new-name',
                description: model.description
            )
    }

    void "PATCH /templates/{id}: Bad Request"() {
        given: 'Template service is available'
            DeploymentSpockExtension.templateBackendContainer().isHostAccessible()

            TemplateModel model = integrator.create(new TemplateModel(
                name: 'name',
                description: 'description'
            )).blockingGet()

        when: 'Call PATCH /templates/{id}'
            integrator.patch(model.id, new TemplateModel(id: 18L)).blockingGet()

        then: 'Verify thrown exception'
            ClientBadRequestException exception = thrown(ClientBadRequestException)
            exception.serviceError == new ServiceErrorModel(
                param: 'id',
                message: 'must be null'
            )
    }

    void "PATCH /templates/{id}: Not Found"() {
        given: 'Template service is available'
            DeploymentSpockExtension.templateBackendContainer().isHostAccessible()

        when: 'Call PATCH /templates/{id}'
            integrator.patch(1L, new TemplateModel(name: 'new-name')).blockingGet()

        then: 'Verify thrown exception'
            ClientNotFoundException exception = thrown(ClientNotFoundException)
            exception.serviceError == new ServiceErrorModel(
                param: 'id',
                message: 'The resource does not exist'
            )
    }

    void "DELETE /templates/{id}: Found"() {
        given: 'Template service is available'
            DeploymentSpockExtension.templateBackendContainer().isHostAccessible()

            TemplateModel model = integrator.create(new TemplateModel(
                name: "name",
                description: "description"
            )).blockingGet()

        when: 'Call DELETE /templates/{id}'
            integrator.delete(model.id).blockingAwait()

        then: 'Verify no exceptions'
            noExceptionThrown()
    }

    void "DELETE /templates/{id}: Not Found"() {
        given: 'Template service is available'
            DeploymentSpockExtension.templateBackendContainer().isHostAccessible()

        when: 'Call DELETE /templates/{id}'
            integrator.delete(1L).blockingAwait()

        then: 'Verify NotFoundException'
            ClientNotFoundException exception = thrown(ClientNotFoundException)
            exception.serviceError == new ServiceErrorModel(
                param: 'id',
                message: 'The resource does not exist'
            )
    }

}

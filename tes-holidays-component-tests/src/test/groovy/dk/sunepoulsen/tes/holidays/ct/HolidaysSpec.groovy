package dk.sunepoulsen.tes.holidays.ct

import dk.sunepoulsen.tes.http.HttpHelper
import dk.sunepoulsen.tes.http.HttpResponseVerificator
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientBadRequestException
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientNotFoundException
import dk.sunepoulsen.tes.rest.models.PaginationModel
import dk.sunepoulsen.tes.rest.models.ServiceErrorModel
import dk.sunepoulsen.tes.holidays.client.rs.HolidayIntegrator
import dk.sunepoulsen.tes.holidays.client.rs.model.HolidayModel
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import spock.lang.Specification

import java.net.http.HttpRequest
import java.time.LocalDate

class HolidaysSpec extends Specification {

    HolidayIntegrator integrator

    void setup() {
        this.integrator = DeploymentSpockExtension.holidaysBackendIntegrator()
        DeploymentSpockExtension.clearDatabase()
    }

    void "POST /holidays returns OK"() {
        given: 'Holidays service is available'
            DeploymentSpockExtension.holidaysBackendContainer().isHostAccessible()

            HolidayModel model = new HolidayModel(
                name: 'name',
                date: LocalDate.now()
            )

        when: 'Call POST /holidays'
            HolidayModel result = integrator.create(model).blockingGet()

        then: 'Verify health body'
            result.id > 0L
            result == new HolidayModel(
                id: result.id,
                name: 'name',
                date: LocalDate.now()
            )
    }

    void "POST /holidays returns BAD_REQUEST"() {
        given: 'Holidays service is available'
            DeploymentSpockExtension.holidaysBackendContainer().isHostAccessible()

        when: 'Call POST /holidays'
            integrator.create(
                new HolidayModel(
                    name: 'name'
                )
            ).blockingGet()

        then: 'Verify exception for bad_request'
            ClientBadRequestException exception = thrown(ClientBadRequestException)
            exception.serviceError == new ServiceErrorModel(
                param: 'date',
                message: 'must not be null'
            )
    }

    void "GET /holidays with no sorting returns OK"() {
        given: 'Holidays service is available'
            DeploymentSpockExtension.holidaysBackendContainer().isHostAccessible()

            (1..5).each {
                integrator.create(new HolidayModel(
                    name: "name-${it}",
                    date: LocalDate.now().plusDays(it)
                )).blockingGet()
            }

        when: 'Call POST /holidays'
            Pageable pageable = PageRequest.of(0, 20)
            PaginationModel<HolidayModel> result = integrator.findAll(pageable).blockingGet()

        then: 'Verify health body'
            result.metadata.page == 0
            result.metadata.size == 20
            result.metadata.totalPages == 1
            result.metadata.totalItems == 5
            result.results.size() == 5
            result.results[0].name == 'name-1'
    }

    void "GET /holidays with sorting returns OK"() {
        given: 'Holidays service is available'
            DeploymentSpockExtension.holidaysBackendContainer().isHostAccessible()

            (1..5).each {
                integrator.create(new HolidayModel(
                    name: "name-${it}",
                    date: LocalDate.now().plusDays(it)
                )).blockingGet()
            }

        when: 'Call POST /holidays'
            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, 'name'))
            PaginationModel<HolidayModel> result = integrator.findAll(pageable).blockingGet()

        then: 'Verify health body'
            result.metadata.page == 0
            result.metadata.size == 20
            result.metadata.totalPages == 1
            result.metadata.totalItems == 5
            result.results.size() == 5
            result.results[0].date == LocalDate.now().plusDays(5)
    }

    void "GET /holidays with bad sorting property"() {
        given: 'Holidays service is available'
            DeploymentSpockExtension.holidaysBackendContainer().isHostAccessible()

        when: 'Call POST /holidays'
            Pageable pageable = PageRequest.of(0, 20, Sort.by('wrong'))
            PaginationModel<HolidayModel> result = integrator.findAll(pageable).blockingGet()

        then: 'Verify health body'
            ClientBadRequestException exception = thrown(ClientBadRequestException)
            exception.serviceError == new ServiceErrorModel(
                param: 'sort',
                message: 'Unknown sort property'
            )
    }

    void "GET /holidays with bad query parameters"() {
        given: 'Holidays service is available'
            DeploymentSpockExtension.holidaysBackendContainer().isHostAccessible()
            String baseUrl = "http://${DeploymentSpockExtension.holidaysBackendContainer().host}:${DeploymentSpockExtension.holidaysBackendContainer().getMappedPort(8080)}"

            (1..5).each {
                integrator.create(new HolidayModel(
                    name: "name-${it}",
                    date: LocalDate.now().plusDays(it)
                )).blockingGet()
            }

        when: 'Call GET /holidays'
            HttpHelper httpHelper = new HttpHelper()
            HttpRequest httpRequest = httpHelper.newRequestBuilder(DeploymentSpockExtension.holidaysBackendContainer(),"/holidays?size=wrong&page=0")
                .GET()
                .build()

            HttpResponseVerificator verificator = httpHelper.sendRequest(httpRequest)

        then: 'Response Code is 200'
            verificator.responseCode(200)

        and: 'Content Type is json'
            verificator.contentType('application/json')

        and: 'Check error body'
            PaginationModel<HolidayModel> body = verificator.bodyAsJsonOfType(PaginationModel)
            body.metadata.page == 0
            body.metadata.size == 20
            body.metadata.totalPages == 1
            body.metadata.totalItems == 5
            body.results.size() == 5
            body.results[0].date == LocalDate.now().plusDays(1)
    }

    void "GET /holidays/{id}: Found"() {
        given: 'Holidays service is available'
            DeploymentSpockExtension.holidaysBackendContainer().isHostAccessible()

            HolidayModel model = integrator.create(new HolidayModel(
                name: "name",
                date: LocalDate.now()
            )).blockingGet()

        when: 'Call GET /holidays/{id}'
            HolidayModel result = integrator.get(model.id).blockingGet()

        then: 'Verify returned template'
            result == model
    }

    void "GET /holidays/{id}: Not Found"() {
        given: 'Holidays service is available'
            DeploymentSpockExtension.holidaysBackendContainer().isHostAccessible()

        when: 'Call GET /holidays/{id}'
            integrator.get(1L).blockingGet()

        then: 'Verify returned template'
            ClientNotFoundException exception = thrown(ClientNotFoundException)
            exception.serviceError == new ServiceErrorModel(
                param: 'id',
                message: 'The resource does not exist'
            )
    }

    void "PATCH /holidays/{id}: Found and patched"() {
        given: 'Holidays service is available'
            DeploymentSpockExtension.holidaysBackendContainer().isHostAccessible()

            HolidayModel model = integrator.create(new HolidayModel(
                name: 'name',
                date: LocalDate.now()
            )).blockingGet()

        when: 'Call PATCH /holidays/{id}'
            HolidayModel result = integrator.patch(model.id, new HolidayModel(name: 'new-name')).blockingGet()

        then: 'Verify returned template'
            result == new HolidayModel(
                id: model.id,
                name: 'new-name',
                date: model.date
            )
    }

    void "PATCH /holidays/{id}: Bad Request"() {
        given: 'Holidays service is available'
            DeploymentSpockExtension.holidaysBackendContainer().isHostAccessible()

            HolidayModel model = integrator.create(new HolidayModel(
                name: 'name',
                date: LocalDate.now()
            )).blockingGet()

        when: 'Call PATCH /holidays/{id}'
            integrator.patch(model.id, new HolidayModel(id: 18L)).blockingGet()

        then: 'Verify thrown exception'
            ClientBadRequestException exception = thrown(ClientBadRequestException)
            exception.serviceError == new ServiceErrorModel(
                param: 'id',
                message: 'must be null'
            )
    }

    void "PATCH /holidays/{id}: Not Found"() {
        given: 'Holidays service is available'
            DeploymentSpockExtension.holidaysBackendContainer().isHostAccessible()

        when: 'Call PATCH /holidays/{id}'
            integrator.patch(1L, new HolidayModel(name: 'new-name')).blockingGet()

        then: 'Verify thrown exception'
            ClientNotFoundException exception = thrown(ClientNotFoundException)
            exception.serviceError == new ServiceErrorModel(
                param: 'id',
                message: 'The resource does not exist'
            )
    }

    void "DELETE /holidays/{id}: Found"() {
        given: 'Holidays service is available'
            DeploymentSpockExtension.holidaysBackendContainer().isHostAccessible()

            HolidayModel model = integrator.create(new HolidayModel(
                name: "name",
                date: LocalDate.now()
            )).blockingGet()

        when: 'Call DELETE /holidays/{id}'
            integrator.delete(model.id).blockingAwait()

        then: 'Verify no exceptions'
            noExceptionThrown()
    }

    void "DELETE /holidays/{id}: Not Found"() {
        given: 'Holidays service is available'
            DeploymentSpockExtension.holidaysBackendContainer().isHostAccessible()

        when: 'Call DELETE /holidays/{id}'
            integrator.delete(1L).blockingAwait()

        then: 'Verify NotFoundException'
            ClientNotFoundException exception = thrown(ClientNotFoundException)
            exception.serviceError == new ServiceErrorModel(
                param: 'id',
                message: 'The resource does not exist'
            )
    }

}

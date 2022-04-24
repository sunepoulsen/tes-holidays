package dk.sunepoulsen.tes.holidays.client.rs


import dk.sunepoulsen.tes.holidays.client.rs.model.HolidayModel
import dk.sunepoulsen.tes.rest.integrations.TechEasySolutionsClient
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientBadRequestException
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientNotFoundException
import dk.sunepoulsen.tes.rest.models.PaginationMetaData
import dk.sunepoulsen.tes.rest.models.PaginationModel
import dk.sunepoulsen.tes.rest.models.ServiceErrorModel
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.CompletableFuture

class HolidayIntegratorSpec extends Specification {

    private TechEasySolutionsClient client
    private HolidayIntegrator sut

    void setup() {
        this.client = Mock(TechEasySolutionsClient)
        this.sut = new HolidayIntegrator(this.client)
    }

    void "Create new holiday: OK"() {
        given:
        HolidayModel model = new HolidayModel(name: 'name')

        when:
            HolidayModel result = sut.create(model).blockingGet()

        then:
            result.name == 'name'

            1 * client.post(HolidayIntegrator.BASE_ENDPOINT_PATH, model, HolidayModel) >> CompletableFuture.completedFuture(model)
    }

    void "Create new holiday: Map exception"() {
        given:
            HolidayModel model = new HolidayModel(name: 'name')

        when:
            sut.create(new HolidayModel(name: 'name')).blockingGet()

        then:
            thrown(ClientBadRequestException)

            1 * client.post(HolidayIntegrator.BASE_ENDPOINT_PATH, model, HolidayModel) >> CompletableFuture.supplyAsync(() -> {
                throw new ClientBadRequestException(null, new ServiceErrorModel(message: 'message'))
            })
    }

    void "Get holidays with no pagination: OK"() {
        when:
            PaginationModel<HolidayModel> result = sut.findAll().blockingGet()

        then:
            result.metadata.totalItems == 20

            1 * client.get(HolidayIntegrator.BASE_ENDPOINT_PATH, PaginationModel) >> CompletableFuture.completedFuture(
                new PaginationModel<HolidayModel>(
                    metadata: new PaginationMetaData(
                        totalItems: 20
                    )
                )
            )
    }

    @Unroll
    void "Get holidays with pagination: #_testcase"() {
        when:
            PaginationModel<HolidayModel> result = sut.findAll(_pagination).blockingGet()

        then:
            result.metadata.totalItems == 20

            1 * client.get("${HolidayIntegrator.BASE_ENDPOINT_PATH}?${_query}", PaginationModel) >> CompletableFuture.completedFuture(
                new PaginationModel<HolidayModel>(
                    metadata: new PaginationMetaData(
                        totalItems: 20
                    )
                )
            )

        where:
            _testcase                       | _query                                        | _pagination
            'Default page'                  | 'page=0&size=20'                              | PageRequest.of(0, 20)
            'Sort by one field'             | 'page=0&size=20&sort=field1'                  | PageRequest.of(0, 20, Sort.by('field1'))
            'Sort by one field descending'  | 'page=0&size=20&sort=field1,desc'             | PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, 'field1'))
            'Sort by two fields'            | 'page=0&size=20&sort=field1&sort=field2'      | PageRequest.of(0, 20, Sort.by('field1', 'field2'))
            'Sort by two fields descending' | 'page=0&size=20&sort=field1&sort=field2,desc' | PageRequest.of(0, 20, Sort.by([Sort.Order.asc('field1'), Sort.Order.desc('field2')]))
    }

    void "Get holiday: OK"() {
        given:
            HolidayModel foundModel = new HolidayModel(id: 5L, name: 'name')

        when:
            HolidayModel result = sut.get(5L).blockingGet()

        then:
            result == foundModel

            1 * client.get("${HolidayIntegrator.BASE_ENDPOINT_PATH}/5", HolidayModel) >> CompletableFuture.completedFuture(foundModel)
    }

    void "Get holiday: Map Exceptions"() {
        when:
            sut.get(5L).blockingGet()

        then:
            ClientBadRequestException exception = thrown(ClientBadRequestException)
            exception.getServiceError() == new ServiceErrorModel(
                message: 'message'
            )

            1 * client.get("${HolidayIntegrator.BASE_ENDPOINT_PATH}/5", HolidayModel) >> CompletableFuture.supplyAsync(() -> {
                throw new ClientBadRequestException(null, new ServiceErrorModel(message: 'message'))
            })
    }

    void "Patch holiday: OK"() {
        given:
            HolidayModel patchModel = new HolidayModel(name: 'name')
            HolidayModel returnedModel = new HolidayModel(id: 5L, name: 'name')

        when:
            HolidayModel result = sut.patch(5L, patchModel).blockingGet()

        then:
            result == returnedModel

            1 * client.patch("${HolidayIntegrator.BASE_ENDPOINT_PATH}/5", patchModel, HolidayModel) >> CompletableFuture.completedFuture(returnedModel)
    }

    void "Patch holiday: Map exception"() {
        given:
            HolidayModel model = new HolidayModel(name: 'name')

        when:
            sut.patch(5L, new HolidayModel(name: 'name')).blockingGet()

        then:
            thrown(ClientBadRequestException)

            1 * client.patch("${HolidayIntegrator.BASE_ENDPOINT_PATH}/5", model, HolidayModel) >> CompletableFuture.supplyAsync(() -> {
                throw new ClientBadRequestException(null, new ServiceErrorModel(message: 'message'))
            })
    }

    void "Delete holiday: OK"() {
        when:
            sut.delete(5L).blockingGet()

        then:
            1 * client.delete("${HolidayIntegrator.BASE_ENDPOINT_PATH}/5") >> CompletableFuture.completedFuture('string')
    }

    void "Delete holiday: Not Found"() {
        when:
            sut.delete(5L).blockingAwait()

        then:
            ClientNotFoundException exception = thrown(ClientNotFoundException)
            exception.getServiceError() == new ServiceErrorModel(
                message: 'message'
            )

            1 * client.delete("${HolidayIntegrator.BASE_ENDPOINT_PATH}/5") >> CompletableFuture.supplyAsync(() -> {
                throw new ClientNotFoundException(null, new ServiceErrorModel(message: 'message'))
            })
    }
}

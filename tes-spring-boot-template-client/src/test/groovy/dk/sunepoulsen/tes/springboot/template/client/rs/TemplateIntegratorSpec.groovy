package dk.sunepoulsen.tes.springboot.template.client.rs

import dk.sunepoulsen.tes.rest.integrations.TechEasySolutionsClient
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientBadRequestException
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientNotFoundException
import dk.sunepoulsen.tes.rest.models.PaginationMetaData
import dk.sunepoulsen.tes.rest.models.PaginationModel
import dk.sunepoulsen.tes.rest.models.ServiceErrorModel
import dk.sunepoulsen.tes.springboot.template.client.rs.model.TemplateModel
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.CompletableFuture

class TemplateIntegratorSpec extends Specification {

    private TechEasySolutionsClient client
    private TemplateIntegrator sut

    void setup() {
        this.client = Mock(TechEasySolutionsClient)
        this.sut = new TemplateIntegrator(this.client)
    }

    void "Create new template: OK"() {
        given:
            TemplateModel model = new TemplateModel(name: 'name')

        when:
            TemplateModel result = sut.create(model).blockingGet()

        then:
            result.name == 'name'

            1 * client.post('/templates', model, TemplateModel) >> CompletableFuture.completedFuture(model)
    }

    void "Create new template: Map exception"() {
        given:
            TemplateModel model = new TemplateModel(name: 'name')

        when:
            sut.create(new TemplateModel(name: 'name')).blockingGet()

        then:
            thrown(ClientBadRequestException)

            1 * client.post('/templates', model, TemplateModel) >> CompletableFuture.supplyAsync(() -> {
                throw new ClientBadRequestException(null, new ServiceErrorModel(message: 'message'))
            })
    }

    void "Get templates with no pagination: OK"() {
        when:
            PaginationModel<TemplateModel> result = sut.findAll().blockingGet()

        then:
            result.metadata.totalItems == 20

            1 * client.get('/templates', PaginationModel) >> CompletableFuture.completedFuture(
                new PaginationModel<TemplateModel>(
                    metadata: new PaginationMetaData(
                        totalItems: 20
                    )
                )
            )
    }

    @Unroll
    void "Get templates with pagination: #_testcase"() {
        when:
            PaginationModel<TemplateModel> result = sut.findAll(_pagination).blockingGet()

        then:
            result.metadata.totalItems == 20

            1 * client.get("/templates?${_query}", PaginationModel) >> CompletableFuture.completedFuture(
                new PaginationModel<TemplateModel>(
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

    void "Get template: OK"() {
        given:
            TemplateModel foundModel = new TemplateModel(id: 5L, name: 'name')

        when:
            TemplateModel result = sut.get(5L).blockingGet()

        then:
            result == foundModel

            1 * client.get('/templates/5', TemplateModel) >> CompletableFuture.completedFuture(foundModel)
    }

    void "Get template: Map Exceptions"() {
        when:
            sut.get(5L).blockingGet()

        then:
            ClientBadRequestException exception = thrown(ClientBadRequestException)
            exception.getServiceError() == new ServiceErrorModel(
                message: 'message'
            )

            1 * client.get('/templates/5', TemplateModel) >> CompletableFuture.supplyAsync(() -> {
                throw new ClientBadRequestException(null, new ServiceErrorModel(message: 'message'))
            })
    }

    void "Patch template: OK"() {
        given:
            TemplateModel patchModel = new TemplateModel(name: 'name')
            TemplateModel returnedModel = new TemplateModel(id: 5L, name: 'name')

        when:
            TemplateModel result = sut.patch(5L, patchModel).blockingGet()

        then:
            result == returnedModel

            1 * client.patch('/templates/5', patchModel, TemplateModel) >> CompletableFuture.completedFuture(returnedModel)
    }

    void "Patch template: Map exception"() {
        given:
            TemplateModel model = new TemplateModel(name: 'name')

        when:
            sut.patch(5L, new TemplateModel(name: 'name')).blockingGet()

        then:
            thrown(ClientBadRequestException)

            1 * client.patch('/templates/5', model, TemplateModel) >> CompletableFuture.supplyAsync(() -> {
                throw new ClientBadRequestException(null, new ServiceErrorModel(message: 'message'))
            })
    }

    void "Delete template: OK"() {
        when:
            sut.delete(5L).blockingGet()

        then:
            1 * client.delete('/templates/5') >> CompletableFuture.completedFuture('string')
    }

    void "Delete template: Not Found"() {
        when:
            sut.delete(5L).blockingAwait()

        then:
            ClientNotFoundException exception = thrown(ClientNotFoundException)
            exception.getServiceError() == new ServiceErrorModel(
                message: 'message'
            )

            1 * client.delete('/templates/5') >> CompletableFuture.supplyAsync(() -> {
                throw new ClientNotFoundException(null, new ServiceErrorModel(message: 'message'))
            })
    }
}

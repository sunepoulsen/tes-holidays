package dk.sunepoulsen.tes.springboot.template.service.domain.template

import dk.sunepoulsen.tes.rest.models.ServiceErrorModel
import dk.sunepoulsen.tes.springboot.rest.exceptions.ApiBadRequestException
import dk.sunepoulsen.tes.springboot.rest.exceptions.ApiNotFoundException
import dk.sunepoulsen.tes.springboot.rest.logic.exceptions.ResourceNotFoundException
import dk.sunepoulsen.tes.springboot.template.client.rs.model.TemplateModel
import dk.sunepoulsen.tes.springboot.template.service.domain.persistence.model.TemplateEntity
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mapping.PropertyReferenceException
import org.springframework.data.util.ClassTypeInformation
import spock.lang.Specification
import spock.lang.Unroll

class TemplateControllerSpec extends Specification {

    TemplateLogic templateLogic
    TemplateController sut

    void setup() {
        templateLogic = Mock(TemplateLogic)
        sut = new TemplateController(templateLogic)
    }

    @Unroll
    void "Create Template returns OK: #_testcase"() {
        given:
            TemplateModel model = new TemplateModel(
                id: null,
                name: 'name',
                description: _description
            )
            TemplateModel expected = new TemplateModel(
                id: 1L,
                name: model.name,
                description: model.description
            )

        when:
            TemplateModel result = sut.create(model)

        then:
            result == expected
            1 * templateLogic.create(model) >> expected

        where:
            _testcase                 | _description
            'description is not null' | 'description'
            'description is null'     | null
    }

    @Unroll
    void "Create Template returns bad request: #_testcase"() {
        given:
            TemplateModel model = new TemplateModel(
                id: _id,
                name: _name
            )

        when:
            sut.create(model)

        then:
            ApiBadRequestException exception = thrown(ApiBadRequestException)
            exception.getServiceError().code == _errorCode
            exception.getServiceError().param == _errorParam
            exception.getServiceError().message == _errorMessage

            0 * templateLogic.create(_)

        where:
            _testcase        | _id  | _name  | _errorCode | _errorParam | _errorMessage
            'id is not null' | 10   | 'name' | null       | 'id'        | 'must be null'
            'name is null'   | null | null   | null       | 'name'      | 'must not be null'
    }

    void "Find all templates with unknown sorting"() {
        given:
            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, 'wrong'))

        when:
            sut.findAll(pageable)

        then:
            ApiBadRequestException exception = thrown(ApiBadRequestException)
            exception.getServiceError() == new ServiceErrorModel(
                param: 'sort',
                message: 'Unknown sort property'
            )

            0 * templateLogic.create(_)
            1 * templateLogic.findAll(pageable) >> {
                throw new PropertyReferenceException('wrong', ClassTypeInformation.from(TemplateEntity.class), [])
            }
    }

    void "Get Template returns OK"() {
        given:
            TemplateModel model = new TemplateModel(
                id: 5L,
                name: 'name',
                description: 'description'
            )

        when:
            TemplateModel result = sut.get(model.id)

        then:
            result == model
            1 * templateLogic.get(model.id) >> model
    }

    void "Get Template returns IllegalArgumentException"() {
        when:
            sut.get(null)

        then:
            ApiBadRequestException exception = thrown(ApiBadRequestException)
            exception.getServiceError() == new ServiceErrorModel(
                param: 'id',
                message: 'message'
            )

            1 * templateLogic.get(null) >> {
                throw new IllegalArgumentException('message')
            }
    }

    void "Get Template returns ResourceNotFoundException"() {
        when:
            sut.get(5L)

        then:
            ApiNotFoundException exception = thrown(ApiNotFoundException)
            exception.getServiceError() == new ServiceErrorModel(
                param: 'id',
                message: 'message'
            )

            1 * templateLogic.get(5L) >> {
                throw new ResourceNotFoundException('id', 'message')
            }
    }

    void "Patch Template returns OK"() {
        given:
            TemplateModel model = new TemplateModel(
                name: 'new-name'
            )
            TemplateModel expected = new TemplateModel(
                id: 5L,
                name: 'new-name',
                description: 'description'
            )

        when:
            TemplateModel result = sut.patch(5L, model)

        then:
            result == expected
            1 * templateLogic.patch(5L, model) >> expected
    }

    @Unroll
    void "Patch Template accepts null values: #_testcase"() {
        given:
            TemplateModel model = new TemplateModel(
                name: _name,
                description: _description
            )

        when:
            sut.patch(5L, model)

        then:
            1 * templateLogic.patch(5L, model)

        where:
            _testcase             | _name   | _description
            'All values'          | null    | null
            'Name is null'        | null    | 'value'
            'Description is null' | 'value' | null
    }

    void "Patch Template returns bad request"() {
        when:
            sut.patch(5L, new TemplateModel(id: 9L))

        then:
            ApiBadRequestException exception = thrown(ApiBadRequestException)
            exception.getServiceError() == new ServiceErrorModel(
                param: 'id',
                message: 'must be null'
            )

            0 * templateLogic.patch(_, _)
    }

    void "Delete Template returns OK"() {
        when:
            sut.delete(5L)

        then:
            1 * templateLogic.delete(5L)
    }

    void "Delete Template returns IllegalArgumentException"() {
        when:
            sut.delete(null)

        then:
            ApiBadRequestException exception = thrown(ApiBadRequestException)
            exception.getServiceError() == new ServiceErrorModel(
                param: 'id',
                message: 'message'
            )

            1 * templateLogic.delete(null) >> {
                throw new IllegalArgumentException('message')
            }
    }

    void "Delete Template returns ResourceNotFoundException"() {
        when:
            sut.delete(5L)

        then:
            ApiNotFoundException exception = thrown(ApiNotFoundException)
            exception.getServiceError() == new ServiceErrorModel(
                param: 'id',
                message: 'message'
            )

            1 * templateLogic.delete(5L) >> {
                throw new ResourceNotFoundException('id', 'message')
            }
    }

}

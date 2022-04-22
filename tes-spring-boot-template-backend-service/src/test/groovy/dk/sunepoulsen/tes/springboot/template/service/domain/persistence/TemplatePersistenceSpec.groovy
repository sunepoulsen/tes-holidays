package dk.sunepoulsen.tes.springboot.template.service.domain.persistence

import dk.sunepoulsen.tes.springboot.rest.logic.exceptions.ResourceNotFoundException
import dk.sunepoulsen.tes.springboot.template.service.domain.persistence.model.TemplateEntity
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.mapping.PropertyReferenceException
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import spock.lang.Specification
import spock.lang.Unroll

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles(['ut'])
class TemplatePersistenceSpec extends Specification {

    @Autowired
    TemplateRepository templateRepository

    @Autowired
    TemplatePersistence templatePersistence

    void setup() {
        this.templateRepository.deleteAll()
    }

    void "Check injections"() {
        expect:
            templateRepository != null
            templatePersistence != null
    }

    void "Create new Template"() {
        given:
            TemplateEntity entity = new TemplateEntity(
                id: null,
                name: 'name',
                description: 'description'
            )

        when:
            TemplateEntity result = templatePersistence.create(entity)

        then:
            result == templateRepository.findById(result.id).get()
    }

    void "Get all templates: OK"() {
        given:
            TemplateEntity entity = templatePersistence.create(new TemplateEntity(
                id: null,
                name: 'name',
                description: 'description'
            ))

        when:
            Page<TemplateEntity> result = templatePersistence.findAll(PageRequest.of(0, 20))

        then:
            result.number == 0
            result.size == 20
            result.totalElements == 1L
            result.totalPages == 1
            result.toList() == [entity]
    }

    void "Get all templates with sorting: OK"() {
        given:
            TemplateEntity entity = templatePersistence.create(new TemplateEntity(
                id: null,
                name: 'name',
                description: 'description'
            ))

        when:
            Page<TemplateEntity> result = templatePersistence.findAll(PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, 'name')))

        then:
            result.number == 0
            result.size == 20
            result.totalElements == 1L
            result.totalPages == 1
            result.toList() == [entity]
    }

    void "Get all templates with sorting: Unknown property"() {
        when:
            templatePersistence.findAll(PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, 'wrong')))

        then:
            PropertyReferenceException exception = thrown(PropertyReferenceException)
            exception.message == 'No property wrong found for type TemplateEntity!'

    }

    void "Get template: Found"() {
        given:
            TemplateEntity entity = templatePersistence.create(new TemplateEntity(
                id: null,
                name: 'name',
                description: 'description'
            ))

        expect:
            templatePersistence.get(entity.getId()) == entity
    }

    void "Get template: Not found"() {
        when:
            templatePersistence.get(5L)

        then:
            ResourceNotFoundException exception = thrown(ResourceNotFoundException)
            exception.param == 'id'
            exception.message == 'The resource does not exist'
    }

    void "Get template: Id is null"() {
        when:
            templatePersistence.get(null)

        then:
            IllegalArgumentException exception = thrown(IllegalArgumentException)
            exception.message == 'May not be null'
    }

    void "Patch template: Found"() {
        given:
            TemplateEntity entity = templatePersistence.create(new TemplateEntity(
                id: null,
                name: 'name',
                description: 'description'
            ))

        expect:
            templatePersistence.patch(entity.id, new TemplateEntity(name: 'new-name')) == new TemplateEntity(
                id: entity.id,
                name: 'new-name',
                description: entity.description
            )
    }

    void "Patch template: Not found"() {
        when:
            templatePersistence.patch(5L, new TemplateEntity())

        then:
            ResourceNotFoundException exception = thrown(ResourceNotFoundException)
            exception.param == 'id'
            exception.message == 'The resource does not exist'
    }

    @Unroll
    void "Patch template: #_testcase"() {
        when:
            templatePersistence.patch(_id, _entity)

        then:
            IllegalArgumentException exception = thrown(IllegalArgumentException)
            exception.message == 'May not be null'

        where:
            _testcase        | _id  | _entity
            'Id is null'     | null | new TemplateEntity()
            'Entity is null' | 5L   | null
    }

    void "Delete template: Found"() {
        given:
            TemplateEntity entity = templatePersistence.create(new TemplateEntity(
                id: null,
                name: 'name',
                description: 'description'
            ))

        expect:
            templatePersistence.delete(entity.getId())
    }

    void "Delete template: Not found"() {
        when:
            templatePersistence.delete(5L)

        then:
            ResourceNotFoundException exception = thrown(ResourceNotFoundException)
            exception.param == 'id'
            exception.message == 'The resource does not exist'
    }

    void "Delete template: Id is null"() {
        when:
            templatePersistence.delete(null)

        then:
            IllegalArgumentException exception = thrown(IllegalArgumentException)
            exception.message == 'May not be null'
    }
}

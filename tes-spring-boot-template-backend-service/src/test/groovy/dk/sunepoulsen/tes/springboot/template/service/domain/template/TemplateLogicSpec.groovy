package dk.sunepoulsen.tes.springboot.template.service.domain.template

import dk.sunepoulsen.tes.springboot.template.service.domain.persistence.TemplatePersistence
import dk.sunepoulsen.tes.springboot.template.service.domain.persistence.model.TemplateEntity
import dk.sunepoulsen.tes.springboot.template.client.rs.model.TemplateModel
import spock.lang.Specification

class TemplateLogicSpec extends Specification {

    private TemplateTransformations templateTransformations
    private TemplatePersistence templatePersistence
    private TemplateLogic sut

    void setup() {
        this.templateTransformations = Mock(TemplateTransformations)
        this.templatePersistence = Mock(TemplatePersistence)
        this.sut = new TemplateLogic(templateTransformations, templatePersistence)
    }

    void "Create new template"() {
        given:
            TemplateModel model = new TemplateModel(
                name: 'name',
                description: 'description'
            )
            TemplateModel expected = new TemplateModel(
                id: 50L,
                name: 'name',
                description: 'description'
            )

        when:
            TemplateModel result = sut.create(model)

        then:
            result == expected
            1 * templateTransformations.toEntity(model) >> new TemplateEntity(id:1L)
            1 * templatePersistence.create(new TemplateEntity(id:1L)) >> new TemplateEntity(id:2L)
            1 * templateTransformations.toModel(new TemplateEntity(id:2L)) >> expected
    }
}

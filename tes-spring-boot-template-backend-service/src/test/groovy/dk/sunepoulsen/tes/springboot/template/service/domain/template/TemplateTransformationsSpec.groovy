package dk.sunepoulsen.tes.springboot.template.service.domain.template

import dk.sunepoulsen.tes.springboot.template.service.domain.persistence.model.TemplateEntity
import dk.sunepoulsen.tes.springboot.template.client.rs.model.TemplateModel
import spock.lang.Specification

class TemplateTransformationsSpec extends Specification {

    private TemplateTransformations sut

    void setup() {
        this.sut = new TemplateTransformations()
    }

    void "Transform model to entity"() {
        given:
            TemplateModel model = new TemplateModel(
                id: 45L,
                name: 'name',
                description: 'description'
            )

        expect:
            sut.toEntity(model) == new TemplateEntity(
                id: model.id,
                name: model.name,
                description: model.description
            )
    }

    void "Transform entity to model"() {
        given:
            TemplateEntity entity = new TemplateEntity(
                id: 45L,
                name: 'name',
                description: 'description'
            )

        expect:
            sut.toModel(entity) == new TemplateModel(
                id: entity.id,
                name: entity.name,
                description: entity.description
            )
    }
}

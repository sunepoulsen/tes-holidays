package dk.sunepoulsen.tes.springboot.template.service.domain.template;

import dk.sunepoulsen.tes.springboot.template.service.domain.persistence.model.TemplateEntity;
import dk.sunepoulsen.tes.springboot.template.client.rs.model.TemplateModel;
import org.springframework.stereotype.Service;

@Service
public class TemplateTransformations {
    TemplateModel toModel(TemplateEntity entity) {
        TemplateModel model = new TemplateModel();
        model.setId(entity.getId());
        model.setName(entity.getName());
        model.setDescription(entity.getDescription());

        return model;
    }

    TemplateEntity toEntity(TemplateModel model) {
        TemplateEntity entity = new TemplateEntity();
        entity.setId(model.getId());
        entity.setName(model.getName());
        entity.setDescription(model.getDescription());

        return entity;
    }
}

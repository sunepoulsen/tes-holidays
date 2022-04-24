package dk.sunepoulsen.tes.holidays.service.domain.holidays;

import dk.sunepoulsen.tes.holidays.service.domain.persistence.model.HolidayEntity;
import dk.sunepoulsen.tes.holidays.client.rs.model.HolidayModel;
import org.springframework.stereotype.Service;

@Service
public class HolidayTransformations {
    HolidayModel toModel(HolidayEntity entity) {
        HolidayModel model = new HolidayModel();
        model.setId(entity.getId());
        model.setName(entity.getName());
        model.setDate(entity.getDate());

        return model;
    }

    HolidayEntity toEntity(HolidayModel model) {
        HolidayEntity entity = new HolidayEntity();
        entity.setId(model.getId());
        entity.setName(model.getName());
        entity.setDate(model.getDate());

        return entity;
    }
}

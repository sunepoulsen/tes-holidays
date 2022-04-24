package dk.sunepoulsen.tes.holidays.service.domain.holidays;

import dk.sunepoulsen.tes.holidays.service.domain.persistence.HolidayPersistence;
import dk.sunepoulsen.tes.holidays.service.domain.persistence.model.HolidayEntity;
import dk.sunepoulsen.tes.holidays.client.rs.model.HolidayModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class HolidayLogic {
    private HolidayTransformations holidayTransformations;
    private HolidayPersistence holidayPersistence;

    @Autowired
    public HolidayLogic(HolidayTransformations holidayTransformations, HolidayPersistence holidayPersistence) {
        this.holidayTransformations = holidayTransformations;
        this.holidayPersistence = holidayPersistence;
    }

    HolidayModel create(HolidayModel model) {
        HolidayEntity entity = holidayTransformations.toEntity(model);
        return holidayTransformations.toModel(holidayPersistence.create(entity));
    }

    Page<HolidayModel> findAll(Pageable pageable) {
        return holidayPersistence.findAll(pageable)
            .map(holidayTransformations::toModel);
    }

    HolidayModel get(Long id) {
        return holidayTransformations.toModel(holidayPersistence.get(id));
    }

    HolidayModel patch(Long id, HolidayModel model) {
        HolidayEntity entity = holidayTransformations.toEntity(model);
        return holidayTransformations.toModel(holidayPersistence.patch(id, entity));
    }

    void delete(Long id) {
        holidayPersistence.delete(id);
    }
}

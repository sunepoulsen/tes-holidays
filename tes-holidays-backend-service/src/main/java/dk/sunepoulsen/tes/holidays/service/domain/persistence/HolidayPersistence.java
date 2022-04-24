package dk.sunepoulsen.tes.holidays.service.domain.persistence;

import dk.sunepoulsen.tes.holidays.service.domain.persistence.model.HolidayEntity;
import dk.sunepoulsen.tes.springboot.rest.logic.PatchUtilities;
import dk.sunepoulsen.tes.springboot.rest.logic.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class HolidayPersistence {
    private HolidayRepository repository;

    @Autowired
    public HolidayPersistence(HolidayRepository repository) {
        this.repository = repository;
    }

    public HolidayEntity create(HolidayEntity entity) {
        return repository.save(entity);
    }

    public Page<HolidayEntity> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public HolidayEntity get(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("May not be null");
        }

        Optional<HolidayEntity> entity = repository.findById(id);
        if (entity.isEmpty()) {
            throw new ResourceNotFoundException("id", "The resource does not exist");
        }

        return entity.get();
    }

    @Transactional
    public HolidayEntity patch(Long id, HolidayEntity patchEntity) {
        if (id == null) {
            throw new IllegalArgumentException("May not be null");
        }
        if (patchEntity == null) {
            throw new IllegalArgumentException("May not be null");
        }

        Optional<HolidayEntity> optionalTemplateEntity = repository.findForUpdate(id);
        if (optionalTemplateEntity.isEmpty()) {
            throw new ResourceNotFoundException("id", "The resource does not exist");
        }

        HolidayEntity entity = optionalTemplateEntity.get();

        entity.setName(PatchUtilities.patchValue(entity.getName(), patchEntity.getName()));
        entity.setDate(PatchUtilities.patchValue(entity.getDate(), patchEntity.getDate()));

        return repository.save(entity);
    }

    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("May not be null");
        }

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("id", "The resource does not exist");
        }

        repository.deleteById(id);
    }
}

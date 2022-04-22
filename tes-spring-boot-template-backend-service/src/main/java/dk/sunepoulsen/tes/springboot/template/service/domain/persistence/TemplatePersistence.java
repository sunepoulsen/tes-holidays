package dk.sunepoulsen.tes.springboot.template.service.domain.persistence;

import dk.sunepoulsen.tes.springboot.rest.logic.PatchUtilities;
import dk.sunepoulsen.tes.springboot.rest.logic.exceptions.ResourceNotFoundException;
import dk.sunepoulsen.tes.springboot.template.service.domain.persistence.model.TemplateEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class TemplatePersistence {
    private TemplateRepository repository;

    @Autowired
    public TemplatePersistence(TemplateRepository repository) {
        this.repository = repository;
    }

    public TemplateEntity create(TemplateEntity entity) {
        return repository.save(entity);
    }

    public Page<TemplateEntity> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public TemplateEntity get(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("May not be null");
        }

        Optional<TemplateEntity> entity = repository.findById(id);
        if (entity.isEmpty()) {
            throw new ResourceNotFoundException("id", "The resource does not exist");
        }

        return entity.get();
    }

    @Transactional
    public TemplateEntity patch(Long id, TemplateEntity patchEntity) {
        if (id == null) {
            throw new IllegalArgumentException("May not be null");
        }
        if (patchEntity == null) {
            throw new IllegalArgumentException("May not be null");
        }

        Optional<TemplateEntity> optionalTemplateEntity = repository.findForUpdate(id);
        if (optionalTemplateEntity.isEmpty()) {
            throw new ResourceNotFoundException("id", "The resource does not exist");
        }

        TemplateEntity entity = optionalTemplateEntity.get();

        entity.setName(PatchUtilities.patchValue(entity.getName(), patchEntity.getName()));
        entity.setDescription(PatchUtilities.patchValue(entity.getDescription(), patchEntity.getDescription()));

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

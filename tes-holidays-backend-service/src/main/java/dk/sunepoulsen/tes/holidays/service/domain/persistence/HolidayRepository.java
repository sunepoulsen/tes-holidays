package dk.sunepoulsen.tes.holidays.service.domain.persistence;

import dk.sunepoulsen.tes.holidays.service.domain.persistence.model.HolidayEntity;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.util.Optional;

public interface HolidayRepository extends PagingAndSortingRepository<HolidayEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM HolidayEntity t WHERE t.id = :id")
    Optional<HolidayEntity> findForUpdate(@Param("id") Long id);
}

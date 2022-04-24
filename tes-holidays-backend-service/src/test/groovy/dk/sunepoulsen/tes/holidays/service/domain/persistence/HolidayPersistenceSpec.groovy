package dk.sunepoulsen.tes.holidays.service.domain.persistence

import dk.sunepoulsen.tes.holidays.service.domain.persistence.model.HolidayEntity
import dk.sunepoulsen.tes.springboot.rest.logic.exceptions.ResourceNotFoundException
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

import java.time.LocalDate

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles(['ut'])
class HolidayPersistenceSpec extends Specification {

    @Autowired
    HolidayRepository holidayRepository

    @Autowired
    HolidayPersistence holidayPersistence

    void setup() {
        this.holidayRepository.deleteAll()
    }

    void "Check injections"() {
        expect:
            holidayRepository != null
            holidayPersistence != null
    }

    void "Create new holiday"() {
        given:
        HolidayEntity entity = new HolidayEntity(
                id: null,
                name: 'name',
                date: LocalDate.now()
            )

        when:
            HolidayEntity result = holidayPersistence.create(entity)

        then:
            result == holidayRepository.findById(result.id).get()
    }

    void "Get all holidays: OK"() {
        given:
            HolidayEntity entity = holidayPersistence.create(new HolidayEntity(
                id: null,
                name: 'name',
                date: LocalDate.now()
            ))

        when:
            Page<HolidayEntity> result = holidayPersistence.findAll(PageRequest.of(0, 20))

        then:
            result.number == 0
            result.size == 20
            result.totalElements == 1L
            result.totalPages == 1
            result.toList() == [entity]
    }

    void "Get all holidays with sorting: OK"() {
        given:
            HolidayEntity entity = holidayPersistence.create(new HolidayEntity(
                id: null,
                name: 'name',
                date: LocalDate.now()
            ))

        when:
            Page<HolidayEntity> result = holidayPersistence.findAll(PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, 'name')))

        then:
            result.number == 0
            result.size == 20
            result.totalElements == 1L
            result.totalPages == 1
            result.toList() == [entity]
    }

    void "Get all holidays with sorting: Unknown property"() {
        when:
            holidayPersistence.findAll(PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, 'wrong')))

        then:
            PropertyReferenceException exception = thrown(PropertyReferenceException)
            exception.message == 'No property wrong found for type HolidayEntity!'

    }

    void "Get holiday: Found"() {
        given:
            HolidayEntity entity = holidayPersistence.create(new HolidayEntity(
                id: null,
                name: 'name',
                date: LocalDate.now()
            ))

        expect:
            holidayPersistence.get(entity.getId()) == entity
    }

    void "Get holiday: Not found"() {
        when:
            holidayPersistence.get(5L)

        then:
            ResourceNotFoundException exception = thrown(ResourceNotFoundException)
            exception.param == 'id'
            exception.message == 'The resource does not exist'
    }

    void "Get holiday: Id is null"() {
        when:
            holidayPersistence.get(null)

        then:
            IllegalArgumentException exception = thrown(IllegalArgumentException)
            exception.message == 'May not be null'
    }

    void "Patch holiday: Found"() {
        given:
            HolidayEntity entity = holidayPersistence.create(new HolidayEntity(
                id: null,
                name: 'name',
                date: LocalDate.now()
            ))

        expect:
            holidayPersistence.patch(entity.id, new HolidayEntity(name: 'new-name')) == new HolidayEntity(
                id: entity.id,
                name: 'new-name',
                date: entity.date
            )
    }

    void "Patch holiday: Not found"() {
        when:
            holidayPersistence.patch(5L, new HolidayEntity())

        then:
            ResourceNotFoundException exception = thrown(ResourceNotFoundException)
            exception.param == 'id'
            exception.message == 'The resource does not exist'
    }

    @Unroll
    void "Patch holiday: #_testcase"() {
        when:
            holidayPersistence.patch(_id, _entity)

        then:
            IllegalArgumentException exception = thrown(IllegalArgumentException)
            exception.message == 'May not be null'

        where:
            _testcase        | _id  | _entity
            'Id is null'     | null | new HolidayEntity()
            'Entity is null' | 5L   | null
    }

    void "Delete holiday: Found"() {
        given:
            HolidayEntity entity = holidayPersistence.create(new HolidayEntity(
                id: null,
                name: 'name',
                date: LocalDate.now()
            ))

        expect:
            holidayPersistence.delete(entity.getId())
    }

    void "Delete holiday: Not found"() {
        when:
            holidayPersistence.delete(5L)

        then:
            ResourceNotFoundException exception = thrown(ResourceNotFoundException)
            exception.param == 'id'
            exception.message == 'The resource does not exist'
    }

    void "Delete holiday: Id is null"() {
        when:
            holidayPersistence.delete(null)

        then:
            IllegalArgumentException exception = thrown(IllegalArgumentException)
            exception.message == 'May not be null'
    }
}

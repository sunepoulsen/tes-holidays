package dk.sunepoulsen.tes.holidays.service.domain.holidays

import dk.sunepoulsen.tes.rest.models.ServiceErrorModel
import dk.sunepoulsen.tes.springboot.rest.exceptions.ApiBadRequestException
import dk.sunepoulsen.tes.springboot.rest.exceptions.ApiNotFoundException
import dk.sunepoulsen.tes.springboot.rest.logic.exceptions.ResourceNotFoundException
import dk.sunepoulsen.tes.holidays.client.rs.model.HolidayModel
import dk.sunepoulsen.tes.holidays.service.domain.persistence.model.HolidayEntity
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mapping.PropertyReferenceException
import org.springframework.data.util.ClassTypeInformation
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate

class HolidayControllerSpec extends Specification {

    HolidayLogic holidayLogic
    HolidayController sut

    void setup() {
        holidayLogic = Mock(HolidayLogic)
        sut = new HolidayController(holidayLogic)
    }

    @Unroll
    void "Create holiday returns OK: #_testcase"() {
        given:
            HolidayModel model = new HolidayModel(
                id: null,
                name: 'name',
                date: _date
            )
            HolidayModel expected = new HolidayModel(
                id: 1L,
                name: model.name,
                date: model.date
            )

        when:
            HolidayModel result = sut.create(model)

        then:
            result == expected
            1 * holidayLogic.create(model) >> expected

        where:
            _testcase          | _date
            'date is not null' | LocalDate.now()
    }

    @Unroll
    void "Create holiday returns bad request: #_testcase"() {
        given:
            HolidayModel model = new HolidayModel(
                id: _id,
                name: _name,
                date: _date
            )

        when:
            sut.create(model)

        then:
            ApiBadRequestException exception = thrown(ApiBadRequestException)
            exception.getServiceError().code == _errorCode
            exception.getServiceError().param == _errorParam
            exception.getServiceError().message == _errorMessage

            0 * holidayLogic.create(_)

        where:
            _testcase        | _id  | _name  | _date           | _errorCode | _errorParam | _errorMessage
            'id is not null' | 10   | 'name' | LocalDate.now() | null       | 'id'        | 'must be null'
            'name is null'   | null | null   | LocalDate.now() | null       | 'name'      | 'must not be null'
            'date is null'   | null | 'name' | null            | null       | 'date'      | 'must not be null'
    }

    void "Find all templates with unknown sorting"() {
        given:
            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, 'wrong'))

        when:
            sut.findAll(pageable)

        then:
            ApiBadRequestException exception = thrown(ApiBadRequestException)
            exception.getServiceError() == new ServiceErrorModel(
                param: 'sort',
                message: 'Unknown sort property'
            )

            0 * holidayLogic.create(_)
            1 * holidayLogic.findAll(pageable) >> {
                throw new PropertyReferenceException('wrong', ClassTypeInformation.from(HolidayEntity.class), [])
            }
    }

    void "Get holidays returns OK"() {
        given:
            HolidayModel model = new HolidayModel(
                id: 5L,
                name: 'name',
                date: LocalDate.now()
            )

        when:
            HolidayModel result = sut.get(model.id)

        then:
            result == model
            1 * holidayLogic.get(model.id) >> model
    }

    void "Get holiday returns IllegalArgumentException"() {
        when:
            sut.get(null)

        then:
            ApiBadRequestException exception = thrown(ApiBadRequestException)
            exception.getServiceError() == new ServiceErrorModel(
                param: 'id',
                message: 'message'
            )

            1 * holidayLogic.get(null) >> {
                throw new IllegalArgumentException('message')
            }
    }

    void "Get holiday returns ResourceNotFoundException"() {
        when:
            sut.get(5L)

        then:
            ApiNotFoundException exception = thrown(ApiNotFoundException)
            exception.getServiceError() == new ServiceErrorModel(
                param: 'id',
                message: 'message'
            )

            1 * holidayLogic.get(5L) >> {
                throw new ResourceNotFoundException('id', 'message')
            }
    }

    void "Patch holiday returns OK"() {
        given:
            HolidayModel model = new HolidayModel(
                name: 'new-name'
            )
            HolidayModel expected = new HolidayModel(
                id: 5L,
                name: 'new-name',
                date: LocalDate.now()
            )

        when:
            HolidayModel result = sut.patch(5L, model)

        then:
            result == expected
            1 * holidayLogic.patch(5L, model) >> expected
    }

    @Unroll
    void "Patch holiday accepts null values: #_testcase"() {
        given:
            HolidayModel model = new HolidayModel(
                name: _name,
                date: _date
            )

        when:
            sut.patch(5L, model)

        then:
            1 * holidayLogic.patch(5L, model)

        where:
            _testcase      | _name   | _date
            'All values'   | null    | null
            'Name is null' | null    | LocalDate.now()
            'Date is null' | 'value' | null
    }

    void "Patch holiday returns bad request"() {
        when:
            sut.patch(5L, new HolidayModel(id: 9L))

        then:
            ApiBadRequestException exception = thrown(ApiBadRequestException)
            exception.getServiceError() == new ServiceErrorModel(
                param: 'id',
                message: 'must be null'
            )

            0 * holidayLogic.patch(_, _)
    }

    void "Delete holiday returns OK"() {
        when:
            sut.delete(5L)

        then:
            1 * holidayLogic.delete(5L)
    }

    void "Delete holiday returns IllegalArgumentException"() {
        when:
            sut.delete(null)

        then:
            ApiBadRequestException exception = thrown(ApiBadRequestException)
            exception.getServiceError() == new ServiceErrorModel(
                param: 'id',
                message: 'message'
            )

            1 * holidayLogic.delete(null) >> {
                throw new IllegalArgumentException('message')
            }
    }

    void "Delete holiday returns ResourceNotFoundException"() {
        when:
            sut.delete(5L)

        then:
            ApiNotFoundException exception = thrown(ApiNotFoundException)
            exception.getServiceError() == new ServiceErrorModel(
                param: 'id',
                message: 'message'
            )

            1 * holidayLogic.delete(5L) >> {
                throw new ResourceNotFoundException('id', 'message')
            }
    }

}

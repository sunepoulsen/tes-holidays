package dk.sunepoulsen.tes.holidays.service.domain.persistence.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@Table( name = "holidays" )
@Data
public class HolidayEntity {
    /**
     * Primary key.
     */
    @Id
    @SequenceGenerator( name = "holidays_id_seq", sequenceName = "holidays_id_seq", allocationSize = 1 )
    @GeneratedValue( strategy = GenerationType.SEQUENCE, generator = "holidays_id_seq" )
    @Column( name = "holiday_id" )
    private Long id;

    @Column( name = "name", nullable = false )
    private String name;

    @Column( name = "date" )
    private LocalDate date;
}

package dk.sunepoulsen.tes.springboot.template.service.domain.persistence.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table( name = "templates" )
@Data
public class TemplateEntity {
    /**
     * Primary key.
     */
    @Id
    @SequenceGenerator( name = "templates_id_seq", sequenceName = "templates_id_seq", allocationSize = 1 )
    @GeneratedValue( strategy = GenerationType.SEQUENCE, generator = "templates_id_seq" )
    @Column( name = "template_id" )
    private Long id;

    @Column( name = "name", nullable = false )
    private String name;

    @Column( name = "description" )
    private String description;
}

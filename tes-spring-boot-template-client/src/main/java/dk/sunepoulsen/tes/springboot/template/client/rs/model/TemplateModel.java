package dk.sunepoulsen.tes.springboot.template.client.rs.model;

import dk.sunepoulsen.tes.rest.models.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "Template model")
public class TemplateModel implements BaseModel {
    @NotNull
    @Schema(description = "Unique id of a template")
    private Long id;

    @NotNull
    @Schema(description = "The name of the template")
    private String name;

    @Schema(description = "The description of the template")
    private String description;
}

package dk.sunepoulsen.tes.springboot.template.service.domain.template;

import dk.sunepoulsen.tes.rest.models.PaginationModel;
import dk.sunepoulsen.tes.rest.models.ServiceErrorModel;
import dk.sunepoulsen.tes.rest.models.transformations.PaginationTransformations;
import dk.sunepoulsen.tes.springboot.rest.exceptions.ApiBadRequestException;
import dk.sunepoulsen.tes.springboot.rest.logic.exceptions.LogicException;
import dk.sunepoulsen.tes.springboot.template.client.rs.model.TemplateModel;
import dk.sunepoulsen.tes.validation.ModelValidator;
import dk.sunepoulsen.tes.validation.exceptions.ModelValidateException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.cfg.defs.NullDef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TemplateController {

    private TemplateLogic templateLogic;

    @Autowired
    public TemplateController(TemplateLogic templateLogic) {
        this.templateLogic = templateLogic;
    }

    @RequestMapping( value = "/templates", method = RequestMethod.POST )
    @ResponseStatus( HttpStatus.CREATED )
    @Operation(summary = "Create a new template")
    @ApiResponses({
        @ApiResponse(responseCode = "201",
            description = "A new template has been created",
            content = { @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = TemplateModel.class)
            ) }
        ),
        @ApiResponse(responseCode = "400",
            description = "The template model is not valid",
            content = { @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ServiceErrorModel.class)
            ) }
        )
    })
    public TemplateModel create(
        @RequestBody
        @Parameter(description = "The template model to create")
        TemplateModel model)
    {
        try {
            ModelValidator.validate(model, TemplateModel.class, mappings -> mappings
                .field("id")
                .ignoreAnnotations(true)
                .constraint(new NullDef())
            );

            return templateLogic.create(model);
        }
        catch( ModelValidateException ex) {
            handleModelValidateException(ex);
            return null;
        }
    }

    @RequestMapping( value = "/templates", method = RequestMethod.GET )
    @ResponseStatus( HttpStatus.OK )
    @Operation(summary = "Find all templates")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
            description = "Returns all found templates in a paginating result",
            content = { @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = TemplateModel.class)
            ) }
        ),
        @ApiResponse(responseCode = "400",
            description = "The query parameters are not valid",
            content = { @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ServiceErrorModel.class)
            ) }
        )
    })
    public PaginationModel<TemplateModel> findAll(Pageable pageable) {
        try {
            return PaginationTransformations.toPaginationResult(templateLogic.findAll(pageable));
        } catch (PropertyReferenceException ex) {
            throw new ApiBadRequestException("sort", "Unknown sort property", ex);
        }
    }

    @RequestMapping( value = "/templates/{id}", method = RequestMethod.GET )
    @ResponseStatus( HttpStatus.OK )
    @Operation(summary = "Returns a template")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
            description = "Returns one template by its id",
            content = { @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = TemplateModel.class)
            ) }
        ),
        @ApiResponse(responseCode = "400",
            description = "The {id} parameters is not a number",
            content = { @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ServiceErrorModel.class)
            ) }
        ),
        @ApiResponse(responseCode = "404",
            description = "Unable to find a template with the given id",
            content = { @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ServiceErrorModel.class)
            ) }
        )
    })
    public TemplateModel get(@PathVariable("id") Long id) {
        try {
            return templateLogic.get(id);
        } catch (IllegalArgumentException ex) {
            throw new ApiBadRequestException("id", ex.getMessage(), ex);
        } catch (LogicException ex) {
            throw ex.mapApiException();
        }
    }

    @RequestMapping( value = "/templates/{id}", method = RequestMethod.PATCH )
    @ResponseStatus( HttpStatus.OK )
    @Operation(summary = "Update an existing template")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
            description = "The template has been updated",
            content = { @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = TemplateModel.class)
            ) }
        ),
        @ApiResponse(responseCode = "400",
            description = "The template model is not valid",
            content = { @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ServiceErrorModel.class)
            ) }
        ),
        @ApiResponse(responseCode = "404",
            description = "A template with the given id does not exist",
            content = { @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ServiceErrorModel.class)
            ) }
        )
    })
    public TemplateModel patch(
        @PathVariable("id")
            Long id,
        @RequestBody
        @Parameter(description = "The template model to create")
            TemplateModel model)
    {
        try {
            ModelValidator.validate(model, TemplateModel.class, mappings -> mappings
                .field("id")
                .ignoreAnnotations(true)
                .constraint(new NullDef())
                .field("name")
                .ignoreAnnotations(true)
            );

            return templateLogic.patch(id, model);
        } catch( ModelValidateException ex) {
            handleModelValidateException(ex);
            return null;
        } catch (IllegalArgumentException ex) {
            throw new ApiBadRequestException("id", ex.getMessage(), ex);
        } catch (LogicException ex) {
            throw ex.mapApiException();
        }
    }

    @RequestMapping( value = "/templates/{id}", method = RequestMethod.DELETE )
    @ResponseStatus( HttpStatus.NO_CONTENT )
    @Operation(summary = "Delete a template")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
            description = "Delete one template by its id"
        ),
        @ApiResponse(responseCode = "400",
            description = "The {id} parameters is not a number",
            content = { @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ServiceErrorModel.class)
            ) }
        ),
        @ApiResponse(responseCode = "404",
            description = "Unable to find a template with the given id",
            content = { @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ServiceErrorModel.class)
            ) }
        )
    })
    public void delete(@PathVariable("id") Long id) {
        try {
            templateLogic.delete(id);
        } catch (IllegalArgumentException ex) {
            throw new ApiBadRequestException("id", ex.getMessage(), ex);
        } catch (LogicException ex) {
            throw ex.mapApiException();
        }
    }

    private void handleModelValidateException(ModelValidateException ex) {
        ex.getViolations().stream().findFirst().ifPresent(validateViolationModel -> {
            throw new ApiBadRequestException(validateViolationModel.getParam(), validateViolationModel.getMessage(), ex);
        });

        throw new ApiBadRequestException("Unknown validation error", ex);
    }
}

# TES Spring Boot dk.sunepoulsen.tes.library.template.Template Backend

TODO: General description of this repository.

## Purpose

TODO: Describe the purpose of this backend

## REST Endpoints

The backend provide the following endpoints:

```
GET /actuator/health
```

Monitoring endpoint to see if the backend is running.

```
GET /swagger-ui.html
```

Online swagger documentation of all endpoints.

## Testing

The component and stress tests are excluded from the normal build cycle. As this is 
expected to be the normal cycle for daily development.

### Component tests

To run the component tests:

```
./gradlew -Pcomponent-tests :tes-spring-boot-template-component-tests:check
```

### Stress tests

To run the stress tests:

```
./gradlew -Pstress-tests :tes-spring-boot-template-stress-tests:check
```

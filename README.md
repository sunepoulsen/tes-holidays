# TES Holidays Backend

TES Holidays Backend is a microservice that stores and holds holidays in a database. 

## Purpose

The purpose is to provide a general microservice that can be used in solutions that require management of holidays. 

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

The component test is placed in the `tes-holidays-component-tests` subproject.

To run the component tests:

```
./gradlew -Pcomponent-tests :tes-features-component-tests:check
```

### Stress tests

The stress test is placed in the `tes-holidays-stress-tests` subproject.

#### JMeter

The stress test is generated with [JMeter](https://jmeter.apache.org/) version 5.4.3. Its required to have a version 
of jmeter installed to be able to run the stress tests.

#### Running

To run the stress tests:

```
./gradlew -Pstress-tests :tes-features-stress-tests:check
```

To run the stress tests with the local profile:

```
./gradlew -Pstress-tests :tes-features-stress-tests:check -Dstress.test.profile=local
```

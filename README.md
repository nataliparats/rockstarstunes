# Tech Screening Demo Kotlin

## How to run the RockTunes API
The application is using Maven to manage the projects and its dependencies. Using maven from the project's file
you can run the following command on your terminal:
`./mvnw spring-boot:run`

## Try out the API
This implementation includes Swagger UI, available at http://localhost:8080/swagger-ui/index.html. Swagger UI is included
in the OpenAPI3 integration of the project.

## Improvement Notes
- Make RockTunesRepository.findAllArtists() return only artists related to songs with genre "Metal"
- Update database from json file, using the API
- Should not update artist's or song's id, unprocessable content
- Refactor RockTunesRepository to convert if checks to validation methods
- Add pagination to API calls that return all artists and songs
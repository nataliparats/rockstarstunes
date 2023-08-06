# Tech Screening Demo Kotlin
This implementation is providing a music repository, aka rocktunesrepository, with 2 implementations for a repository. 
One completely in memory and one using Postgres database.
The application is using
- Java 17
- Docker
- Maven

## How to run the RockTunes API
The application is using Maven to manage the projects and its dependencies. Using maven from the project's file
you can run: 
- The following command on your terminal to run the in memory implementation `./mvnw spring-boot:run`
- The following commands on your terminal to run the postgresql implementation 
  - `docker run -itd -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=123 -p 5432:5432 -v /data:/var/lib/postgresql/data --name rocktunesdb postgres`
  - `./mvnw spring-boot:run -Dspring-boot.run.profiles=jpa`

## Try out the API
This implementation includes Swagger UI, available at http://localhost:8080/swagger-ui/index.html. Swagger UI is included
in the OpenAPI3 integration of the project.

## Improvement Notes
- Update database from json file, using the API
- Should not update artist's or song's id, unprocessable content
- Add pagination to API calls that return all artists and songs
- Spin up db using docker compose
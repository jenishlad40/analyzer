# Log analyzer application

## Application for analyzing the logs

This application is used to analyze the logs written in text file in below json object format.
```sh
{"id":"scsmbstgra","state":"STARTED","type":"APPLICATION_LOG","host":"12345","timestamp":"1491377495212"}
{"id":"scsmbstgrb","state":"STARTED","timestamp":"1491377495213"}
```

>It accepts txt file format file as a command line input and store result in HSQLDB(In memory database).

## How to run
```sh
mvn spring-boot:run -Dspring-boot.run.arguments=<Text File path>
```

> Note: `<Text File path>` valid text file.


## Tech

- [Spring Boot] - Spring Boot framework
- [HSQLDB] - In-Memory database
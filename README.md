# Run

The application is designed following [12 factor app](https://12factor.net/) design guidelines.  
When targetDb is not specified, the application will run using the default configuration in application.properties. When the gradle project property "env" is specified the app will look for   
* <user.home>/.michr-apps/yhr-track/app.properties
* file specified for java system property config (e.g.: ./gradlew bootrun -Dconfig=app.properties)
* file specified for environment variable YHR_TRACK_CONFIG_FILE (e.g.: HR_TRACK_CONFIG_FILE=app.properties ./gradlew bootrun)
Within the app.properties file, the properties prefixed by the value implied by -Penv argument will be used
see ExternalConfigEnvironmentPostProcessor for details (/src/main/resources/META-INF/spring.factories sets this up)

## Overriding Configuration Based on Database Target
When running the application locally, you can specify the target database configuration by passing a Gradle property. For example, to use the production settings, run:

./gradlew bootRun -Penv=prod
This will instruct the application to load properties from app.properties that are prefixed with dev. (e.g., prod.spring.datasource.url), and they will be added to the environment with the prefix removed.

## Locally Using Embedded DB

./gradlew bootrun

Debug App Locally Using Embedded DB

./gradlew bootrun --debug-jvm -Dspring.profiles.active=dev
You can use http://localhost:8082/h2-console to access embedded h2 db. Refer to application-dev.properties for the db connection details.

# To Debug Database Integration Tests

To debug the database integration tests, you need to set the breakpoint to suspend the executing thread only instead of all threads. 
Otherwise the h2 web console web server thread will be suspended and the web console will not be available.
http://localhost:8082/h2-console
See application-test.properties data source url and credentials to connect to the h2 database.

MICHR Track Application

## Overview
This is a Spring Boot web application designed to collect analytics data from web applications. 
It follows the [12-factor app](https://12factor.net/) design guidelines, supporting externalized configuration to run with different settings depending on the environment.

## CORS Configuration
Cross-Origin Resource Sharing is configurable through client configurations stored in the database. Each client entry specifies:

- Client ID
- Allowed origins (domains)

The following CORS settings are configured in the code. See the `config/cors` package for details:

- Allowed HTTP methods
- Allowed headers
- Additional CORS settings

## Configuration

You can configure the application in an external properties file specified by the environment or through command-line arguments, 
as supported by Spring Boot's externalized configuration. The external configuration file is found by checking

1. User Home Directory: The default location is 
```
<user.home>/.michr-apps/track/app.properties
```
2. Java System Property: Specify the file using the system property config 
```shell
./gradlew bootRun -Dconfig=/some/folder/app.properties)
```
3. Environment Variable: Specify the file using the environment variable TRACK_CONFIG_FILE 
```shell 
TRACK_CONFIG_FILE=/some/folder/app.properties ./gradlew bootRun
```
## Overriding Configuration Based on Target Environment When running Locally

If -Penv argument is used when running gradle then the property entries prefixed by the value of "env" project parameter will be used for configuration.
(See ExternalConfigEnvironmentPostProcessor for details; this is set up via /src/main/resources/META-INF/spring.factories.)
```shell 
./gradlew -Penv=prod bootRun
# It is expected that the configuration properties file will have properties prefixed with "prod" for the production environment e.g.: prod.DB_USERNAME=MICHR_TRACK
```
This command instructs the application to load properties from properties file (specified by the configuration) that are prefixed with prod. (for example, prod.spring.datasource.url). These properties are then added to the environment with the prefix removed.

## Running Locally Using Embedded DB

To run the application with an embedded H2 database, simply execute:

./gradlew bootrun

## Debugging the Application Locally (Embedded DB)

To debug the application locally with the embedded database, run:

./gradlew bootrun --debug-jvm
You can use http://localhost:8080/h2-console to access embedded h2 db. Refer to application-dev.properties for the db connection details.

Initial data is loaded from src/main/resources/data.sql which populates:

Client configurations with allowed origins and CORS settings

### Note on Connection Pool Parameters

When running the application, Hibernate logs may show some connection pool parameters as "undefined/unknown". 
This is a known issue with how Hibernate logs HikariCP configuration and doesn't indicate a problem with the actual connection pool settings. 
The configured HikariCP parameters are still applied correctly. To verify the actual settings, enable HikariCP debug logging by adding `logging.level.com.zaxxer.hikari=DEBUG` to your configuration.

## Debugging Database Integration Tests

When debugging the database integration tests, ensure that only the executing thread is suspended at breakpoints (and not all threads). 
Otherwise, the H2 web console thread may also be suspended, preventing access to http://localhost:8082/h2-console.

See application-test.properties for the datasource URL and credentials needed to connect to the H2 database.

# Tracking with Image Pixels (GET)

Let's say you want to track unique page views but not repeated views within a 30-minute window:

```java
// Track views but only count a new view every 30 minutes
HttpHeaders headers = new HttpHeaders();
headers.setContentType(MediaType.IMAGE_GIF);
headers.setCacheControl("private, max-age=1800"); // 30 minutes
headers.setETag("\"session-" + sessionId + "\"");
```

1. This is much harder to control precisely with a 204 response due to less consistent caching behavior across clients. For such cases 
sendBeacon may not be the ideal form of tracking depending on your needs.   
2. For email tracking, the image request is inevitable since js will not execute in email clients. We will need to return 200 with 1pixel 
image otherwise a broken image will be displayed in the email. Not only that, some clients may submit multiple retry requests if the image is not loaded properly.

# Submitting tracking requests

[Beacon API](https://developer.mozilla.org/en-US/docs/Web/API/Beacon_API) is preferable for analytics data. Below is a sample JavaScript 
snippet that can be used to submit tracking requests to the application. The snippet checks if the browser 
supports [sendBeacon](https://developer.mozilla.org/en-US/docs/Web/API/Navigator/sendBeacon):

* **If supported:** It sends a POST request using sendBeacon with a JSON payload.
* **If not supported:** It falls back to a GET request by building a URL with query parameters.

> **Note:**  
> To track individual users, the JS code should be modified to include a unique identifier for each user (stored in a persistent cookie). 
> This cookie should be set with the HttpOnly and Secure flags to prevent XSS attacks. (If the cookie is HTTP-only, it will be automatically 
> included in requests but cannot be read via JavaScript.)


To be able to track the individual users the js below should be tweaked to include a unique identifier for the user in a persistent cookie 
and send it with the tracking request. The cookie should be set with the HttpOnly and Secure flags to prevent XSS attacks even though 
the biggest risk would be nothing but the incorrect analytics data.

## Sample JS Tag  

```js
(function() {
  var clientId = "d2c1e4a7-63c5-4dfd-a392-35636f7ce5ac";
  // If you like to test the email tracking feature, uncomment the line below.
  //var emailId = "5902f6aa-8ca5-413b-8646-6079ed9265e7" ;
  var analyticsUrl = "https://localhost:8080/analytics/events";

  // Utility to generate a GUID
  function generateGUID() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
      var r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8);
      return v.toString(16);
    });
  }

  // Cookie helper functions
  function getCookie(name) {
    var match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'));
    return match ? match[2] : null;
  }

  function setCookie(name, value, days) {
    var expires = "";
    if (days) {
      var date = new Date();
      date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
      expires = "; expires=" + date.toUTCString();
    }
    document.cookie = name + "=" + value + expires + "; path=/";
  }

  // Get or create the tracking user id
  var trackingCookieName = "analytics-user-id";
  var userId = getCookie(trackingCookieName);
  if (!userId) {
    userId = generateGUID();
    // Set cookie for 1 year persistence
    setCookie(trackingCookieName, userId, 365);
  }

  // Utility to send analytics data
  function sendAnalytics(eventData) {
    var payloadString = serializePayload(eventData, navigator.sendBeacon?'POST':'GET');

    // Try sendBeacon if available
    if (navigator.sendBeacon) {
      var blob = new Blob([payloadString], { type: "application/x-www-form-urlencoded" });
      if (navigator.sendBeacon(analyticsUrl, blob)) {
        return;
      }
    }
    // Fallback: create an image pixel to trigger GET request.
    var img = new Image();
    img.src = analyticsUrl + "?" + payloadString;
  }

  // Determine event type. For demonstration, assume:
  // - First load: pageView
  // - Refresh: pageRefresh (using performance navigation info if available)
  // - SPA route change: will be triggered via a custom global function
  var eventType = "pageView";  // default

  if (window.performance && performance.navigation && performance.navigation.type === 1) {
    eventType = "pageRefresh";
  }

  // Capture the time when the page is loaded
  var pageLoadTime = Date.now();

  // Prepare common event data
  function buildPayload() {
    return {
      userId: userId,
      clientId: clientId,
      // if tracking emails uncomment the line below and the line where emailId is declared above
      // emailId: emailId,
      page: window.location.href,
      eventType: eventType,
      // Application-specific cookies (assuming they exist)
      appLang: getCookie("lang") || "",
      cookieConsent: getCookie("cookie_consent") || ""
    };
  }


  function serializePayload(json, method) {
    return Object.keys(json).map(key => {
      const encodedKey = method.toUpperCase() === 'GET'
        ? key.replace(/([A-Z])/g, '-$1').toLowerCase()
        : key;
      const encodedValue = encodeURIComponent(json[key]);
      return `${encodedKey}=${encodedValue}`;
    }).join('&');
  }

  // Send initial event (pageView or pageRefresh)
  sendAnalytics(buildPayload());

  // Expose a function for SPA route changes
  window.trackRouteChange = function() {
    var routeChangeData = buildPayload();
    routeChangeData.eventType = "routeChange";
    routeChangeData.eventTimestamp = new Date().toISOString();
    sendAnalytics(routeChangeData);
    // Reset the page load time for duration tracking
    pageLoadTime = Date.now();
  };

  // Track when user leaves the page, calculating duration.
  function trackLeftPage() {
    // Avoid sending duplicate leftPage events if already sent
    if (window.leftPageTracked) return;
    window.leftPageTracked = true;

    var leftPageData = buildPayload();
    leftPageData.eventType = "leftPage";
    leftPageData.durationMs = Date.now() - pageLoadTime;
    leftPageData.eventTimestamp = new Date().toISOString();
    sendAnalytics(leftPageData);
  }

  // Listen for various unload events
  window.addEventListener("beforeunload", trackLeftPage);
  window.addEventListener("pagehide", trackLeftPage);

  // Use the Page Visibility API to support mobile behavior:
  // When the document becomes hidden, it's a good moment to capture the leftPage event.
  document.addEventListener("visibilitychange", function() {
    if (document.visibilityState === "hidden") {
      trackLeftPage();
    }
  });

})();
```

## Analytics Request Authorization

For GET/POST requests using browsers fetch or sendBeacon, the Origin or Referrer header is automatically set by the browser. 
The value of the header is compared to the list of allowed origins for the given client id in CLIENT and AUTHORIZED_CLIENT_ORIGIN records. 
However, if 1 px image request is used for analytics tracking and if the request is originated from a mail client to track email opens, 
then those headers can not be used. In that case CLIENT should have a corresponding GUID record in AUTHORIZED_CLIENT_ORIGIN and the image pixed should submit email-id as a request parameter set to the allowed GUID which acts as allowed origin.  

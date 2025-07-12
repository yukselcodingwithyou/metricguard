# metricguard

MetricGuard is a small library that records business and system errors as metrics.
Metrics can be exported to Prometheus or OpenTelemetry.
The library targets **Java&nbsp;17** and is intended for use with Spring Boot&nbsp;3.x.

## Usage
1. Include the jar as a dependency in your Spring Boot application.
2. Configure a `MeterRegistry` bean (e.g. `PrometheusMeterRegistry`) only when exporting to Prometheus.
3. Annotate methods with `@MetricGuard` and set `backend` to `PROMETHEUS` or `OTEL`. The `tags` array is required while header names are optional.
4. Convenience annotations `@PrometheusMetricGuard` and `@OtelMetricGuard` are provided for the common cases.
   When only exporting to OpenTelemetry, a `MeterRegistry` bean is unnecessary.

```java
@MetricGuard(
    backend = MetricBackend.PROMETHEUS,
    tags = {"operation=create"},
    headers = {"X-Platform", "X-Agent"}
)
public void createUser(User user) {
    // ...
}
```

To export to OpenTelemetry:

```java
@MetricGuard(
    backend = MetricBackend.OTEL,
    tags = {"operation=update"},
    headers = {"X-Platform"})
public void updateUser(User user) {
    // ...
}
```

// or simply

```java
@OtelMetricGuard(tags = {"operation=profile"}, headers = {"X-Platform"})
public void updateProfile(User user) {
    // ...
}
```

Any `BusinessException` or other exception thrown from the method will increment the `metricguard_errors` counter with tags including a `type` of `business` or `system`.
When `headers` are specified, the current HTTP request is inspected and the header values are attached as tags. If no headers are configured, the request is not accessed at all.

## Building and Installing

After merging your changes to `main`, build and install the library to your
local Maven repository:

```bash
mvn clean install
```

You can then reference the snapshot version from other projects:

```xml
<dependency>
    <groupId>com.yukselcodingwithyou</groupId>
    <artifactId>metricguard</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

Publish the artifact to your preferred Maven repository when ready for a
release.


package com.yukselcodingwithyou.metricguard;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

/**
 * Convenience annotation that exports metrics to Prometheus.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@MetricGuard(backend = MetricBackend.PROMETHEUS, tags = {})
public @interface PrometheusMetricGuard {
    /**
     * Additional tags in key=value form. This attribute is required and is
     * passed through to {@link MetricGuard#tags()}.
     */
    @AliasFor(annotation = MetricGuard.class, attribute = "tags")
    String[] tags();

    /** Header names to capture as tags. */
    @AliasFor(annotation = MetricGuard.class, attribute = "headers")
    String[] headers() default {};
}

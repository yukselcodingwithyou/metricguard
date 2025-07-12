package com.yukselcodingwithyou.metricguard;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

import com.yukselcodingwithyou.metricguard.MetricBackend;
import com.yukselcodingwithyou.metricguard.MetricGuard;

/**
 * Convenience annotation that exports metrics to OpenTelemetry.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@MetricGuard(backend = MetricBackend.OTEL, tags = {})
public @interface OtelMetricGuard {
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

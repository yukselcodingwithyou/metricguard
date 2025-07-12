package com.yukselcodingwithyou.metricguard;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.yukselcodingwithyou.metricguard.MetricBackend;

/**
 * Annotation to mark methods whose exceptions should be recorded as metrics.
 * <p>
 * Tags to associate with the metric must be provided in the form
 * {@code key=value}. These will be attached to the meter when an
 * exception is recorded.
 * Header names may also be specified so their values are exported as tags.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface MetricGuard {
    /** Backend to export metrics to. */
    MetricBackend backend() default MetricBackend.PROMETHEUS;
    /**
     * Additional tags to associate with the metric in the form key=value.
     * This attribute is required.
     */
    String[] tags();

    /**
     * Names of HTTP headers to attach as tags using the header value.
     */
    String[] headers() default {};
}

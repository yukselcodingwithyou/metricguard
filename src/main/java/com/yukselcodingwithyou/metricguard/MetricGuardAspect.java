package com.yukselcodingwithyou.metricguard;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.yukselcodingwithyou.metricguard.MetricBackend;

/**
 * Aspect that records metrics for methods annotated with {@link MetricGuard}.
 */
@Aspect
@Component
public class MetricGuardAspect {
    private final MeterRegistry registry;
    private final LongCounter otelCounter;

    @Autowired(required = false)
    public MetricGuardAspect(@Nullable MeterRegistry registry) {
        this.registry = registry;
        Meter meter = GlobalOpenTelemetry.getMeter("metricguard");
        this.otelCounter = meter.counterBuilder("metricguard_errors").build();
    }

    @Pointcut("@annotation(com.yukselcodingwithyou.metricguard.MetricGuard) || " +
            "@annotation(com.yukselcodingwithyou.metricguard.OtelMetricGuard) || " +
            "@annotation(com.yukselcodingwithyou.metricguard.PrometheusMetricGuard)")
    public void callAt() {
    }

    @Around("callAt()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        MetricGuard metricGuard = AnnotationUtils.findMergedAnnotation(
                ((MethodSignature) pjp.getSignature()).getMethod(), MetricGuard.class);
        if (metricGuard == null) {
            return pjp.proceed();
        }
        try {
            return pjp.proceed();
        } catch (BusinessException ex) {
            recordError("business", metricGuard);
            throw ex;
        } catch (Exception ex) {
            recordError("system", metricGuard);
            throw ex;
        }
    }

    private void recordError(String type, MetricGuard metricGuard) {
        switch (metricGuard.backend()) {
            case PROMETHEUS:
                recordPrometheus(type, metricGuard.tags(), metricGuard.headers());
                break;
            case OTEL:
                recordOtel(type, metricGuard.tags(), metricGuard.headers());
                break;
        }
    }

    private void recordPrometheus(String type, String[] annotationTags, String[] headerNames) {
        if (registry == null) {
            return;
        }
        Tags tags = Tags.of("type", type);
        for (String t : annotationTags) {
            String[] kv = t.split("=", 2);
            if (kv.length == 2) {
                tags = tags.and(kv[0], kv[1]);
            }
        }
        if (headerNames.length > 0) {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                for (String h : headerNames) {
                    String val = request.getHeader(h);
                    if (val != null) {
                        tags = tags.and(h, val);
                    }
                }
            }
        }
        Counter.builder("metricguard_errors")
                .tags(tags)
                .register(registry)
                .increment();
    }

    private void recordOtel(String type, String[] annotationTags, String[] headerNames) {
        AttributesBuilder attrs = Attributes.builder().put("type", type);
        for (String t : annotationTags) {
            String[] kv = t.split("=", 2);
            if (kv.length == 2) {
                attrs.put(kv[0], kv[1]);
            }
        }
        if (headerNames.length > 0) {
            ServletRequestAttributes reqAttrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (reqAttrs != null) {
                HttpServletRequest request = reqAttrs.getRequest();
                for (String h : headerNames) {
                    String val = request.getHeader(h);
                    if (val != null) {
                        attrs.put(h, val);
                    }
                }
            }
        }
        otelCounter.add(1, attrs.build());
    }
}

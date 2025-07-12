package com.yukselcodingwithyou.metricguard;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.InMemoryMetricReader;
import io.opentelemetry.sdk.metrics.data.MetricData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.yukselcodingwithyou.metricguard.MetricGuardAspect;

import java.util.Collection;

class OtelMetricGuardAspectTest {

    static class Service {
        @OtelMetricGuard(tags = {})
        void fail() {
            throw new RuntimeException("fail");
        }
    }

    @Test
    void recordsToOpenTelemetry() {
        InMemoryMetricReader reader = InMemoryMetricReader.create();
        SdkMeterProvider provider = SdkMeterProvider.builder().registerMetricReader(reader).build();
        OpenTelemetrySdk sdk = OpenTelemetrySdk.builder().setMeterProvider(provider).build();
        GlobalOpenTelemetry.set(sdk);
        MetricGuardAspect aspect = new MetricGuardAspect(null);

        Service target = new Service();
        ProxyFactory factory = new ProxyFactory(target);
        factory.addAspect(aspect);
        Service proxy = (Service) factory.getProxy();
        MockHttpServletRequest request = new MockHttpServletRequest();
        ServletRequestAttributes attrs = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attrs);
        try {
            Assertions.assertThrows(RuntimeException.class, proxy::fail);
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }

        Collection<MetricData> data = reader.collectAllMetrics();
        boolean found = data.stream().anyMatch(md -> md.getName().equals("metricguard_errors"));
        Assertions.assertTrue(found);
    }
}

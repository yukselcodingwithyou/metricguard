package com.yukselcodingwithyou.metricguard;

import io.micrometer.core.instrument.SimpleMeterRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;

import com.yukselcodingwithyou.metricguard.PrometheusMetricGuard;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class MetricGuardAspectTest {

    static class Service {
        @PrometheusMetricGuard(tags = {"k=v"}, headers = {"X-Platform"})
        void businessError() {
            throw new BusinessException("boom");
        }

        @PrometheusMetricGuard(tags = {})
        void systemError() {
            throw new RuntimeException("fail");
        }
    }

    @Test
    void recordsBusinessAndSystemErrors() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        MetricGuardAspect aspect = new MetricGuardAspect(registry);

        Service target = new Service();
        ProxyFactory factory = new ProxyFactory(target);
        factory.addAspect(aspect);
        Service proxy = (Service) factory.getProxy();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Platform", "ios");
        ServletRequestAttributes attrs = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attrs);
        try {
            Assertions.assertThrows(BusinessException.class, proxy::businessError);
            Assertions.assertThrows(RuntimeException.class, proxy::systemError);
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }

        Assertions.assertEquals(2,
                registry.get("metricguard_errors").counter().count(),
                0.01);
        Assertions.assertEquals(1,
                registry.get("metricguard_errors")
                        .tags("X-Platform", "ios", "type", "business", "k", "v")
                        .counter().count(),
                0.01);
    }
}

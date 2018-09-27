/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.metrics;

import io.github.mweirauch.micrometer.jvm.extras.ProcessMemoryMetrics;
import io.github.mweirauch.micrometer.jvm.extras.ProcessThreadMetrics;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * TODO create an interface to work with this
 */
public class MetricsController implements ApplicationContextAware
{
    private PrometheusMeterRegistry prometheusRegistry;
    private Counter numberOfRestApiWebscriptCalls;
    private LongTaskTimer restApiResponseTime;

    private ApplicationContext context;

    private boolean enabled;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.context = applicationContext;
        if (enabled)
        {
            initMetricsRegistry();
        }
    }

    private void initMetricsRegistry()
    {
        prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

        new ProcessMemoryMetrics().bindTo(prometheusRegistry);
        new ProcessThreadMetrics().bindTo(prometheusRegistry);

        new ClassLoaderMetrics().bindTo(prometheusRegistry);
        new JvmMemoryMetrics().bindTo(prometheusRegistry);
        new JvmGcMetrics().bindTo(prometheusRegistry);
        new ProcessorMetrics().bindTo(prometheusRegistry);
        new JvmThreadMetrics().bindTo(prometheusRegistry);

        numberOfRestApiWebscriptCalls = prometheusRegistry.counter("number.restapi.webscript.calls");
        restApiResponseTime = LongTaskTimer.builder("event.process.time").register(prometheusRegistry);
    }

    public String scrape()
    {
        if (enabled)
        {
            return prometheusRegistry.scrape();
        }
        return "The prometheus metrics service is not enabled";
    }

    public void recordRestApiCall()
    {
        if (enabled)
        {
            numberOfRestApiWebscriptCalls.increment();
        }
    }

    public LongTaskTimer.Sample recordRestApiCallTime()
    {
        if (enabled)
        {
            return restApiResponseTime.start();
        }
        return new FakeSample();
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }
}

class FakeSample extends LongTaskTimer.Sample
{

    public FakeSample()
    {
        super(null, -1);
    }

    @Override
    public long stop()
    {
        //nothing
        return -1;
    }
}
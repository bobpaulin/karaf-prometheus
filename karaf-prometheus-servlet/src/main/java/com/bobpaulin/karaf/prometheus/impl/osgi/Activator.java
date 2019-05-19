/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.bobpaulin.karaf.prometheus.impl.osgi;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

public class Activator implements BundleActivator {
	
	public static final String METRICS_REGISTRY_NAME = "metricsRegistry";

    private ServiceTracker httpServiceTracker;
    
    ServiceRegistration<MeterRegistry> prometheusSr;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
    	PrometheusMeterRegistry prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
		Hashtable<String, Object> micrometerConfigServiceProps = new Hashtable<String, Object>();
		micrometerConfigServiceProps.put(Constants.SERVICE_PID, METRICS_REGISTRY_NAME);
		micrometerConfigServiceProps.put("name", METRICS_REGISTRY_NAME);
		this.prometheusSr = bundleContext.registerService(MeterRegistry.class, prometheusRegistry, micrometerConfigServiceProps);
		
		new ClassLoaderMetrics().bindTo(prometheusRegistry);
        new JvmMemoryMetrics().bindTo(prometheusRegistry);
        new JvmGcMetrics().bindTo(prometheusRegistry);
        new ProcessorMetrics().bindTo(prometheusRegistry);
        new JvmThreadMetrics().bindTo(prometheusRegistry);
        
        httpServiceTracker = new ServiceTracker(bundleContext, HttpService.class.getName(), null) {
            @Override
            public Object addingService(ServiceReference ref) {
                HttpService httpService = (HttpService) bundleContext.getService(ref);
                try {
                    httpService.registerServlet("/prometheus", new MicrometerServlet(prometheusRegistry), null, null);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return httpService;
            }

            public void removedService(ServiceReference ref, Object service) {
                try {
                    ((HttpService) service).unregister("/prometheus");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        httpServiceTracker.open();
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        httpServiceTracker.close();
        bundleContext.ungetService(this.prometheusSr.getReference());
        
}

}

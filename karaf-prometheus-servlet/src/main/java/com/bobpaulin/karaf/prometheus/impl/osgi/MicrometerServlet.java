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

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.exporter.common.TextFormat;

public class MicrometerServlet extends HttpServlet {

	private final PrometheusMeterRegistry prometheusRegistry;
	
	public MicrometerServlet(PrometheusMeterRegistry prometheusRegistry) {
		this.prometheusRegistry = prometheusRegistry;
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setStatus(HttpServletResponse.SC_OK);
	    resp.setContentType(TextFormat.CONTENT_TYPE_004);

	    Writer writer = resp.getWriter();
	    try {
	      writer.write(this.prometheusRegistry.scrape());
	      writer.flush();
	    } finally {
	      writer.close();
	    }
	}
}

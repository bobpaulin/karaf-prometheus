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

package org.springframework.samples.petclinic.system;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import io.opentelemetry.sdk.resources.Resource;

public class Telemetry {

	private static OpenTelemetry openTelemetry;

	public static void initTelemetry() {
		Resource resource = Resource.getDefault()
			.merge(Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, "pet-clinic")));

		SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
			.addSpanProcessor(BatchSpanProcessor.builder(OtlpGrpcSpanExporter.builder().build()).build())
			.setResource(resource)
			.build();

		SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
			.registerMetricReader(PeriodicMetricReader.builder(OtlpGrpcMetricExporter.builder().build()).build())
			.setResource(resource)
			.build();

		SdkLoggerProvider sdkLoggerProvider = SdkLoggerProvider.builder()
			.addLogRecordProcessor(BatchLogRecordProcessor.builder(OtlpGrpcLogRecordExporter.builder().build()).build())
			.setResource(resource)
			.build();

		openTelemetry = OpenTelemetrySdk.builder()
			.setTracerProvider(sdkTracerProvider)
			.setMeterProvider(sdkMeterProvider)
			.setLoggerProvider(sdkLoggerProvider)
			.setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
			.buildAndRegisterGlobal();

		System.out.println("Telemetry initialized");
	}

	public static OpenTelemetry getOpenTelemetry() {
		return openTelemetry;
	}

}
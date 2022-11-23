package utf8uri;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.util.Deque;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponentsBuilder;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

public class UriTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(UriTest.class);
	private static final String PARAM_A = "1";
	private static final String PARAM_B = "^";
	private static final String PARAM_C = "👻";
	private int port = 8088;
	private Undertow server;
	private URI uri;

	@Before
	public void before() {
		uri = UriComponentsBuilder.fromUriString("http://127.0.0.1:" + port).pathSegment("whatever")
				.queryParam("a", PARAM_A).queryParam("b", PARAM_B).queryParam("c", PARAM_C).encode().build()

				.toUri();
		LOGGER.info("URI is {}", uri);
		server = Undertow.builder().addHttpListener(port, "localhost").setHandler(new HttpHandler() {
			@Override
			public void handleRequest(final HttpServerExchange exchange) throws Exception {
				if (parametersMatch(exchange.getQueryParameters())) {
					exchange.setStatusCode(200);
					exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
					exchange.getResponseSender().send("OK");
				} else {
					exchange.setStatusCode(400);
					exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
					exchange.getResponseSender().send("ERROR");
				}
			}

			private static boolean parametersMatch(Map<String, Deque<String>> map) {
				return PARAM_A.equals(map.get("a").getFirst()) && PARAM_B.equals(map.get("b").getFirst())
						&& PARAM_C.equals(map.get("c").getFirst());
			}
		}).build();
		server.start();
	}

	@After
	public void after() {
		server.stop();
	}

	@Test
	public void testClient5() throws IOException {
		try (var client = org.apache.hc.client5.http.impl.classic.HttpClientBuilder.create().build()) {
			try (var response = client.execute(new org.apache.hc.client5.http.classic.methods.HttpGet(uri))) {
				assertEquals(200, response.getCode());
			}
		}
	}

	@Test
	public void testClient4() throws IOException {
		try (var client = org.apache.http.impl.client.HttpClientBuilder.create().build()) {
			try (var response = client.execute(new org.apache.http.client.methods.HttpGet(uri))) {
				assertEquals(200, response.getStatusLine().getStatusCode());
			}
		}
	}
}

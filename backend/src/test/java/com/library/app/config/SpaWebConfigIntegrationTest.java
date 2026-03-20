package com.library.app.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SpaWebConfigIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void getRoot_forwardsToIndexHtml() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(forwardedUrl("index.html"));
	}

	@Test
	void getIndexHtml_servesClasspathStatic() throws Exception {
		mockMvc.perform(get("/index.html"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("spa-test-index")));
	}

	@Test
	void getDeepClientRoute_fallsBackToIndex() throws Exception {
		mockMvc.perform(get("/books/any/deep-link"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("spa-test-index")));
	}
}

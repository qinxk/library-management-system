package com.library.app.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * Serves the Vue SPA from {@code classpath:/static/} and falls back to {@code index.html}
 * for non-file routes (history mode).
 */
@Configuration
public class SpaWebConfig implements WebMvcConfigurer {

	/**
	 * Permits anonymous GET for browser navigation and hashed asset files; excludes anything under {@code /api}.
	 */
	public static boolean isPublicSpaOrStaticGet(HttpServletRequest request) {
		if (!HttpMethod.GET.matches(request.getMethod())) {
			return false;
		}
		String uri = request.getRequestURI();
		String contextPath = request.getContextPath();
		if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
			uri = uri.substring(contextPath.length());
		}
		if (uri.isEmpty()) {
			uri = "/";
		}
		if (!uri.startsWith("/")) {
			uri = "/" + uri;
		}
		return !isApiPath(uri);
	}

	private static boolean isApiPath(String uri) {
		return uri.startsWith("/api/") || "/api".equals(uri);
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/**")
				.addResourceLocations("classpath:/static/")
				.resourceChain(false)
				.addResolver(new SpaFallbackResolver());
	}

	private static final class SpaFallbackResolver extends PathResourceResolver {

		@Override
		protected Resource getResource(String resourcePath, Resource location) throws IOException {
			Resource resource = super.getResource(resourcePath, location);
			if (resource != null) {
				return resource;
			}
			return super.getResource("index.html", location);
		}
	}
}

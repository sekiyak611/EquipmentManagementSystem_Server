package jp.co.highjunk.equipment.common.web;

import java.io.IOException;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * 組み立てた画面（Client の dist）を配る（API設計 2.1、詳細設計 common.md 3.9）。
 *
 * <ul>
 * <li>static フォルダにあるファイルは、そのまま返す。</li>
 * <li>画面のアドレス（/、/loans、/equipment/12 など）では、index.html を返す。画面の切り替えは React が行うため。</li>
 * <li>/api/ で始まるアドレスと、名前に「.」を含むのにファイルがないアドレスは、何も返さない（404 になる）。</li>
 * </ul>
 */
@Configuration(proxyBeanMethods = false)
public class SpaWebConfig implements WebMvcConfigurer {

	private static final String STATIC_LOCATION = "classpath:/static/";

	private static final String INDEX_HTML = "static/index.html";

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/**")
				.addResourceLocations(STATIC_LOCATION)
				.resourceChain(true)
				.addResolver(new IndexFallbackResolver());
	}

	/** ファイルがなければ、画面のアドレスのときだけ index.html を返す。 */
	static class IndexFallbackResolver extends PathResourceResolver {

		@Override
		protected Resource getResource(String resourcePath, Resource location) throws IOException {
			Resource found = super.getResource(resourcePath, location);
			if (found != null) {
				return found;
			}
			if (resourcePath.startsWith("api/") || resourcePath.equals("api") || lastSegment(resourcePath).contains(".")) {
				return null;
			}
			Resource index = new ClassPathResource(INDEX_HTML);
			return index.exists() ? index : null;
		}

		private static String lastSegment(String path) {
			int slash = path.lastIndexOf('/');
			return slash < 0 ? path : path.substring(slash + 1);
		}
	}
}

package jp.co.highjunk.equipment.testsupport;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import jakarta.servlet.http.Cookie;

/**
 * テストで POST・PUT・DELETE を送るときに、なりすまし対策の合言葉（CSRFトークン）を、画面と同じやり方で付ける部品。
 * GET /api/health で XSRF-TOKEN クッキーを受け取り、クッキーと X-XSRF-TOKEN 見出しの両方に入れて送る。
 *
 * <p>Spring Security のテスト用の csrf() は使わない。csrf() は、ほかのテストと共有している部品の中身を
 * 書き換えてしまい、あとで動くテストで合言葉のクッキーが出なくなるため（CLAUDE.md 6章）。
 */
public final class CsrfTestHelper {

	private CsrfTestHelper() {
	}

	/**
	 * 合言葉を付ける。使い方：mockMvc.perform(post("/api/...").with(CsrfTestHelper.csrfToken(mockMvc)))
	 */
	public static RequestPostProcessor csrfToken(MockMvc mockMvc) throws Exception {
		Cookie xsrf = mockMvc.perform(get("/api/health")).andReturn().getResponse().getCookie("XSRF-TOKEN");
		if (xsrf == null) {
			throw new IllegalStateException("XSRF-TOKEN クッキーを受け取れませんでした");
		}
		return request -> {
			Cookie[] existing = request.getCookies();
			Cookie[] cookies = existing == null ? new Cookie[1] : java.util.Arrays.copyOf(existing, existing.length + 1);
			cookies[cookies.length - 1] = xsrf;
			request.setCookies(cookies);
			request.addHeader("X-XSRF-TOKEN", xsrf.getValue());
			return request;
		};
	}

}

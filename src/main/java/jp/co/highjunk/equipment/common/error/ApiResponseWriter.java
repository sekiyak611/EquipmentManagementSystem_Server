package jp.co.highjunk.equipment.common.error;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

/**
 * controller の外（Spring Securityの中など）でエラーになったときに、
 * docs/API設計.md 2.4 の形で返すための部品。
 */
@Component
public class ApiResponseWriter {

	private final JsonMapper jsonMapper;

	public ApiResponseWriter(JsonMapper jsonMapper) {
		this.jsonMapper = jsonMapper;
	}

	/** エラーの形（data・errors は null）で書き出す。 */
	public void writeError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		jsonMapper.writeValue(response.getOutputStream(), ApiResponse.error(status, message, null));
	}
}

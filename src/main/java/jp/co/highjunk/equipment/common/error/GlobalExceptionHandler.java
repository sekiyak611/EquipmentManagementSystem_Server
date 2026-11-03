package jp.co.highjunk.equipment.common.error;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * APIで起きたエラーを、docs/API設計.md 2.4 の形（status・message・data・errors）に変えて返す。
 * 各機能の controller・service では、ここを通すために ApiException の仲間を投げる。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	private final AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();

	/** 各機能が投げたエラー（決まりに合わない操作、見つからない、ぶつかった、CSVの行のエラーなど）。 */
	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException ex) {
		return respond(ex.getStatus(), ex.getMessage(), ex.getErrors());
	}

	/** 送られてきた値（JSON）の入力チェックの誤り。入力欄ごとのメッセージを errors に入れる。 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleNotValid(MethodArgumentNotValidException ex) {
		List<ErrorItem> errors = new ArrayList<>();
		for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
			errors.add(ErrorItem.ofField(fieldError.getField(), fieldError.getDefaultMessage()));
		}
		ex.getBindingResult().getGlobalErrors()
				.forEach(e -> errors.add(ErrorItem.ofField(null, e.getDefaultMessage())));
		return respond(HttpStatus.BAD_REQUEST, ApiMessages.VALIDATION_ERROR, errors);
	}

	/** アドレスの後ろに付けた値（?page=1 など）の入力チェックの誤り。 */
	@ExceptionHandler(HandlerMethodValidationException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodValidation(HandlerMethodValidationException ex) {
		List<ErrorItem> errors = new ArrayList<>();
		ex.getParameterValidationResults().forEach(result -> {
			String field = result.getMethodParameter().getParameterName();
			for (MessageSourceResolvable error : result.getResolvableErrors()) {
				errors.add(ErrorItem.ofField(field, error.getDefaultMessage()));
			}
		});
		return respond(HttpStatus.BAD_REQUEST, ApiMessages.VALIDATION_ERROR, errors);
	}

	/** JSONの形が壊れている、必要な値がない、数字の欄に文字が入っている、など。 */
	@ExceptionHandler({ HttpMessageNotReadableException.class, MissingServletRequestParameterException.class,
			MethodArgumentTypeMismatchException.class })
	public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception ex) {
		return respond(HttpStatus.BAD_REQUEST, ApiMessages.VALIDATION_ERROR, null);
	}

	/**
	 * 権限の確認（@PreAuthorize）で止められた。
	 * ログインしていなければ 401、ログインしていて権限がなければ 403。
	 */
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || trustResolver.isAnonymous(authentication)) {
			return respond(HttpStatus.UNAUTHORIZED, ApiMessages.UNAUTHORIZED, null);
		}
		return respond(HttpStatus.FORBIDDEN, ApiMessages.FORBIDDEN, null);
	}

	/** ログインが必要なのに、ログインしていない。 */
	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException ex) {
		return respond(HttpStatus.UNAUTHORIZED, ApiMessages.UNAUTHORIZED, null);
	}

	/**
	 * 上のどれにも当たらないエラー。
	 * Springが用意しているエラー（存在しないアドレス、使えない操作の種類など。ErrorResponse の仲間）は、
	 * 番号はSpringが決めたものを使い、文は番号に合わせる。
	 * それ以外は思いがけない不具合として、詳しい中身はログにだけ残し、画面には決まった文だけ返す。
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleOther(Exception ex) {
		if (ex instanceof ErrorResponse errorResponse) {
			HttpStatus status = HttpStatus.resolve(errorResponse.getStatusCode().value());
			if (status != null && status.is4xxClientError()) {
				String message = switch (status) {
					case NOT_FOUND -> ApiMessages.NOT_FOUND;
					case BAD_REQUEST -> ApiMessages.VALIDATION_ERROR;
					default -> ApiMessages.BAD_REQUEST;
				};
				return respond(status, message, null);
			}
		}
		log.error("思いがけない不具合が起きました", ex);
		return respond(HttpStatus.INTERNAL_SERVER_ERROR, ApiMessages.INTERNAL_ERROR, null);
	}

	private ResponseEntity<ApiResponse<Void>> respond(HttpStatus status, String message, List<ErrorItem> errors) {
		return ResponseEntity.status(status).body(ApiResponse.error(status, message, errors));
	}
}

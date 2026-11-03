package jp.co.highjunk.equipment.common.error;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * エラーの返し方を確かめるための、テストの中だけにあるAPI（本番には入らない）。
 * 各エラーを、わざと起こす。
 */
@RestController
class ErrorTestController {

	record SampleRequest(@NotBlank(message = "ユーザー名は必須です。") String userName) {
	}

	@PostMapping("/api/test-errors/validation")
	ApiResponse<String> validation(@Valid @RequestBody SampleRequest request) {
		return ApiResponse.ok(request.userName());
	}

	@GetMapping("/api/test-errors/page")
	ApiResponse<Integer> page(@RequestParam int page) {
		return ApiResponse.ok(page);
	}

	@GetMapping("/api/test-errors/business-rule")
	ApiResponse<Void> businessRule() {
		throw new BusinessRuleException("貸出中の備品は削除できません");
	}

	@GetMapping("/api/test-errors/not-found")
	ApiResponse<Void> notFound() {
		throw new NotFoundException();
	}

	@GetMapping("/api/test-errors/conflict")
	ApiResponse<Void> conflict() {
		throw new ConflictException("この備品はすでに貸し出されています");
	}

	@GetMapping("/api/test-errors/csv")
	ApiResponse<Void> csv() {
		throw new CsvErrorException(List.of(ErrorItem.ofCsvRow(12, "D003", "不明の備品です。")));
	}

	@GetMapping("/api/test-errors/unexpected")
	ApiResponse<Void> unexpected() {
		throw new IllegalStateException("画面に出してはいけない詳しい中身");
	}

	@GetMapping("/api/test-errors/permission")
	@PreAuthorize("hasAuthority('EQUIPMENT_UPDATE')")
	ApiResponse<String> permission() {
		return ApiResponse.ok("ok");
	}
}

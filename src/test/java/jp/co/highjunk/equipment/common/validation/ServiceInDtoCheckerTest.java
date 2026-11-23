package jp.co.highjunk.equipment.common.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import jakarta.validation.Validation;
import jp.co.highjunk.equipment.common.error.ApiException;
import jp.co.highjunk.equipment.common.error.BusinessRuleException;
import jp.co.highjunk.equipment.common.error.ConflictException;
import jp.co.highjunk.equipment.common.error.FieldErrorException;
import jp.co.highjunk.equipment.common.error.ForbiddenException;
import jp.co.highjunk.equipment.common.error.NotFoundException;
import jp.co.highjunk.equipment.common.validation.ServiceCheckOrder.First;
import jp.co.highjunk.equipment.common.validation.ServiceCheckOrder.Second;

/**
 * ServiceInDtoChecker の、守れなかったときの返し方（詳細設計 common.md 3.20。Forbidden を足した）の単体テスト。
 * DB は使わない。いつも守れないテスト用のチェックを、種類（payload）と段（groups）を変えて付ける。
 */
class ServiceInDtoCheckerTest {

	private final ServiceInDtoChecker checker = new ServiceInDtoChecker(
			Validation.buildDefaultValidatorFactory().getValidator());

	@Test
	@DisplayName("Forbidden だけ：403 の ForbiddenException。文はアノテーションの message")
	void forbidden() {
		assertThatThrownBy(() -> checker.check(new ForbiddenOnly()))
				.isInstanceOfSatisfying(ForbiddenException.class, e -> {
					assertThat(e.getStatus().value()).isEqualTo(403);
					assertThat(e.getMessage()).isEqualTo("ほかの人のものです");
					assertThat(e.getErrors()).isNull();
				});
	}

	@Test
	@DisplayName("1つの段に Forbidden と Conflict と BusinessRule が出たら、Forbidden を返す")
	void forbiddenBeatsConflictAndBusinessRule() {
		assertThatThrownBy(() -> checker.check(new SecondAll())).isInstanceOf(ForbiddenException.class);
	}

	@Test
	@DisplayName("Conflict と BusinessRule だけなら、Conflict を返す（これまでどおり）")
	void conflictBeatsBusinessRule() {
		assertThatThrownBy(() -> checker.check(new ConflictAndRule())).isInstanceOf(ConflictException.class);
	}

	@Test
	@DisplayName("BusinessRule だけなら、BusinessRuleException（これまでどおり）")
	void businessRule() {
		assertThatThrownBy(() -> checker.check(new RuleOnly())).isInstanceOf(BusinessRuleException.class);
	}

	@Test
	@DisplayName("1段目が守れなければ、2段目は確かめない（1段目の Conflict を返す。2段目も確かめると Forbidden が勝ってしまう）")
	void firstStageStopsBeforeSecond() {
		assertThatThrownBy(() -> checker.check(new FirstConflictSecondForbidden()))
				.isInstanceOf(ConflictException.class);
	}

	@Test
	@DisplayName("同じ段に NotFound と Forbidden が出たら、NotFound（404）を先に返す")
	void notFoundBeatsForbidden() {
		assertThatThrownBy(() -> checker.check(new NotFoundAndForbidden())).isInstanceOf(NotFoundException.class)
				.isInstanceOf(ApiException.class);
	}

	@Test
	@DisplayName("payload のないチェック：入力欄のエラー（FieldErrorException。400。これまでどおり）")
	void fieldError() {
		assertThatThrownBy(() -> checker.check(new FieldOnly())).isInstanceOfSatisfying(FieldErrorException.class,
				e -> {
					assertThat(e.getStatus().value()).isEqualTo(400);
					assertThat(e.getErrors()).hasSize(1);
					assertThat(e.getErrors().get(0).message()).isEqualTo("ほかの人のものです");
				});
	}

	@Test
	@DisplayName("すべて守れていれば、何も起きない")
	void allPass() {
		assertThatCode(() -> checker.check(new Object())).doesNotThrowAnyException();
	}

	@Target({ ElementType.TYPE })
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@Repeatable(AlwaysFails.List.class)
	@Constraint(validatedBy = AlwaysFailsCheck.class)
	public @interface AlwaysFails {

		String message() default "ほかの人のものです";

		Class<?>[] groups() default {};

		Class<? extends Payload>[] payload() default {};

		@Target({ ElementType.TYPE })
		@Retention(RetentionPolicy.RUNTIME)
		@Documented
		public @interface List {

			AlwaysFails[] value();
		}
	}

	public static class AlwaysFailsCheck implements ConstraintValidator<AlwaysFails, Object> {

		@Override
		public boolean isValid(Object value, ConstraintValidatorContext context) {
			return false;
		}
	}

	@AlwaysFails(groups = Second.class, payload = ServiceErrorType.Forbidden.class)
	static class ForbiddenOnly {
	}

	@AlwaysFails(groups = Second.class, payload = ServiceErrorType.Forbidden.class)
	@AlwaysFails(groups = Second.class, payload = ServiceErrorType.Conflict.class, message = "ぶつかった")
	@AlwaysFails(groups = Second.class, payload = ServiceErrorType.BusinessRule.class, message = "決まりに合わない")
	static class SecondAll {
	}

	@AlwaysFails(groups = Second.class, payload = ServiceErrorType.Conflict.class, message = "ぶつかった")
	@AlwaysFails(groups = Second.class, payload = ServiceErrorType.BusinessRule.class, message = "決まりに合わない")
	static class ConflictAndRule {
	}

	@AlwaysFails(groups = Second.class, payload = ServiceErrorType.BusinessRule.class, message = "決まりに合わない")
	static class RuleOnly {
	}

	@AlwaysFails(groups = First.class, payload = ServiceErrorType.Conflict.class, message = "ぶつかった")
	@AlwaysFails(groups = Second.class, payload = ServiceErrorType.Forbidden.class)
	static class FirstConflictSecondForbidden {
	}

	@AlwaysFails(groups = First.class, payload = ServiceErrorType.NotFound.class)
	@AlwaysFails(groups = First.class, payload = ServiceErrorType.Forbidden.class)
	static class NotFoundAndForbidden {
	}

	@AlwaysFails(groups = Second.class)
	static class FieldOnly {
	}
}

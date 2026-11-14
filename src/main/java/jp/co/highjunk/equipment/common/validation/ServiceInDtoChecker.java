package jp.co.highjunk.equipment.common.validation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Payload;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;
import jp.co.highjunk.equipment.common.error.BusinessRuleException;
import jp.co.highjunk.equipment.common.error.ConflictException;
import jp.co.highjunk.equipment.common.error.ErrorItem;
import jp.co.highjunk.equipment.common.error.FieldErrorException;
import jp.co.highjunk.equipment.common.error.NotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * ServiceInDto を Bean Validation で確かめ、守れなければ common.error の例外に変えて投げる部品（詳細設計 common.md。CLAUDE.md 5章）。
 * ServiceImpl が、@Transactional のメソッドの中で、書き換えの前に呼ぶ（確かめてから書き換えるまでを1つのまとまりにするため）。
 *
 * <p>確かめる順番は ServiceCheckOrder の First → Second → Default。ある段で守れないものがあれば、そこで止める。
 * 返し方は、チェック用アノテーションの payload（ServiceErrorType）で決める。1つの段で2つ以上の種類が出たら、
 * NotFound → Conflict → BusinessRule → 入力欄のエラーの順に、先のものを返す。
 */
@Component
@RequiredArgsConstructor
public class ServiceInDtoChecker {

	private static final List<Class<?>> ORDER = List.of(ServiceCheckOrder.First.class,
			ServiceCheckOrder.Second.class, Default.class);

	private final Validator validator;

	/**
	 * ServiceInDto を確かめる。すべて守れていれば何もしない。
	 *
	 * @throws NotFoundException     payload が NotFound のチェックを守れないとき（404）
	 * @throws ConflictException     payload が Conflict のチェックを守れないとき（409）
	 * @throws BusinessRuleException payload が BusinessRule のチェックを守れないとき（400。errors なし）
	 * @throws FieldErrorException   payload のないチェックを守れないとき（400。errors に入力欄ごとの文）
	 */
	public void check(Object serviceInDto) {
		for (Class<?> group : ORDER) {
			Set<ConstraintViolation<Object>> violations = validator.validate(serviceInDto, group);
			if (!violations.isEmpty()) {
				throw toException(serviceInDto.getClass(), violations);
			}
		}
	}

	private static RuntimeException toException(Class<?> dtoClass, Set<ConstraintViolation<Object>> violations) {
		if (has(violations, ServiceErrorType.NotFound.class)) {
			return new NotFoundException();
		}
		ConstraintViolation<Object> conflict = first(violations, ServiceErrorType.Conflict.class);
		if (conflict != null) {
			return new ConflictException(conflict.getMessage());
		}
		ConstraintViolation<Object> rule = first(violations, ServiceErrorType.BusinessRule.class);
		if (rule != null) {
			return new BusinessRuleException(rule.getMessage());
		}
		List<String> fieldOrder = fieldNames(dtoClass);
		List<ErrorItem> errors = new ArrayList<>();
		violations.stream()
				.sorted(Comparator.comparingInt((ConstraintViolation<Object> v) -> order(fieldOrder, field(v)))
						.thenComparing(ConstraintViolation::getMessage))
				.forEach(v -> errors.add(ErrorItem.ofField(field(v), v.getMessage())));
		return new FieldErrorException(errors);
	}

	private static boolean has(Set<ConstraintViolation<Object>> violations, Class<? extends Payload> type) {
		return first(violations, type) != null;
	}

	/** その印の付いた、守れなかったチェックのうち1つ（入力欄の名前・文の順で先のもの）。なければ null。 */
	private static ConstraintViolation<Object> first(Set<ConstraintViolation<Object>> violations,
			Class<? extends Payload> type) {
		return violations.stream()
				.filter(v -> v.getConstraintDescriptor().getPayload().contains(type))
				.min(Comparator.comparing((ConstraintViolation<Object> v) -> String.valueOf(field(v)))
						.thenComparing(ConstraintViolation::getMessage))
				.orElse(null);
	}

	/** 入力欄の名前（項目のいちばん上の名前）。クラスに付けたチェックで、入力欄を指していなければ null。 */
	private static String field(ConstraintViolation<Object> violation) {
		for (Path.Node node : violation.getPropertyPath()) {
			if (node.getName() != null) {
				return node.getName();
			}
		}
		return null;
	}

	/** errors の並び順は、ServiceInDto の項目を書いた順（入力欄を指さないものは後ろ）。 */
	private static int order(List<String> fieldOrder, String field) {
		int index = fieldOrder.indexOf(field);
		return index < 0 ? Integer.MAX_VALUE : index;
	}

	private static List<String> fieldNames(Class<?> dtoClass) {
		List<String> names = new ArrayList<>();
		for (Field field : dtoClass.getDeclaredFields()) {
			names.add(field.getName());
		}
		return names;
	}
}

package jp.co.highjunk.equipment;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

/**
 * 構造のテスト（ArchUnit）。パッケージの依存と、書き方の決まりのうち、機械で確かめられるものを確かめる。
 * 決まりの正本は CLAUDE.md 4.1・5章（Server の .claude/rules/java.md）。決まりに合わないコードが入ると、このテストが失敗する。
 * 例外は、理由を書いて、この中に置く（勝手に増やさない。増やすときは、人に確かめる）。
 * 本体のコードだけを調べる（テストのコードは調べない）。
 */
@DisplayName("構造のテスト（ArchUnit）")
class ArchitectureTest {

	private static final String BASE = "jp.co.highjunk.equipment";

	/** API のパッケージの中の、API ごとのまとまり（…item など）を取り出す。controller・service・model・validator の手前まで */
	private static final Pattern API_ROOT = Pattern.compile("^(" + Pattern.quote(BASE) + "\\.api\\..+?)\\.(controller|service|model|validator)(\\..*)?$");

	private static JavaClasses classes;

	@BeforeAll
	static void importClasses() {
		classes = new ClassFileImporter().withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS).importPackages(BASE);
	}

	/** API のパッケージのクラスなら、その API のまとまりの名前。そうでなければ null */
	private static String apiRoot(JavaClass c) {
		Matcher m = API_ROOT.matcher(c.getPackageName());
		return m.matches() ? m.group(1) : null;
	}

	@Nested
	@DisplayName("パッケージの依存（CLAUDE.md 4.1・5章）")
	class Dependencies {

		@Test
		@DisplayName("API のクラスは、ほかの API のパッケージ（同じまとまりの API も）を使わない。使ってよいのは common・dao と、自分の API")
		void apisDoNotUseEachOther() {
			classes().that().resideInAPackage(BASE + ".api..").should(new ArchCondition<JavaClass>("ほかの API のパッケージを使わない") {
				@Override
				public void check(JavaClass origin, ConditionEvents events) {
					String own = apiRoot(origin);
					for (Dependency d : origin.getDirectDependenciesFromSelf()) {
						JavaClass target = d.getTargetClass();
						String other = apiRoot(target);
						if (other != null && !other.equals(own)) {
							events.add(SimpleConditionEvent.violated(d, d.getDescription()));
						}
					}
				}
			}).check(classes);
		}

		@Test
		@DisplayName("common・dao は、api・batch を使わない")
		void commonAndDaoDoNotUseApi() {
			noClasses().that().resideInAnyPackage(BASE + ".common..", BASE + ".dao..")
					.should().dependOnClassesThat().resideInAnyPackage(BASE + ".api..", BASE + ".batch..").check(classes);
		}

		@Test
		@DisplayName("api と batch は、互いに使わない")
		void apiAndBatchDoNotUseEachOther() {
			// batch はまだ作っていない（Slack催促の定期実行で作る。ZPOP-127）ので、空でもよいことにする
			noClasses().that().resideInAPackage(BASE + ".api..").should().dependOnClassesThat().resideInAPackage(BASE + ".batch..")
					.allowEmptyShould(true).check(classes);
			noClasses().that().resideInAPackage(BASE + ".batch..").should().dependOnClassesThat().resideInAPackage(BASE + ".api..")
					.allowEmptyShould(true).check(classes);
		}

		@Test
		@DisplayName("Controller は、ServiceImpl と dao の Mapper を直接使わない（Service の interface を通す）")
		void controllersUseServiceInterface() {
			noClasses().that().resideInAPackage("..controller..").should().dependOnClassesThat().haveSimpleNameEndingWith("ServiceImpl").check(classes);
			noClasses().that().resideInAPackage("..controller..").should().dependOnClassesThat().resideInAPackage(BASE + ".dao..").check(classes);
		}
	}

	@Nested
	@DisplayName("クラスの置き場所と名前（CLAUDE.md 4.1・5章の命名規則）")
	class Naming {

		@Test
		@DisplayName("Controller は controller、ServiceImpl・ServiceInDto は service、RestOutDto は model、Validator は validator のパッケージに置く")
		void placement() {
			classes().that().resideInAPackage(BASE + ".api..").and().haveSimpleNameEndingWith("Controller").should().resideInAPackage("..controller..").check(classes);
			classes().that().resideInAPackage(BASE + ".api..").and().haveSimpleNameEndingWith("RestInDto").should().resideInAPackage("..controller..").check(classes);
			classes().that().resideInAPackage(BASE + ".api..").and().haveSimpleNameEndingWith("ServiceImpl").should().resideInAPackage("..service..").check(classes);
			classes().that().resideInAPackage(BASE + ".api..").and().haveSimpleNameEndingWith("ServiceInDto").should().resideInAPackage("..service..").check(classes);
			classes().that().resideInAPackage(BASE + ".api..").and().haveSimpleNameEndingWith("RestOutDto").should().resideInAPackage("..model..").check(classes);
			classes().that().resideInAPackage(BASE + ".api..").and().haveSimpleNameEndingWith("Validator").should().resideInAPackage("..validator..").check(classes);
			classes().that().resideInAPackage(BASE + ".api..").and().haveSimpleNameEndingWith("ValidatorDto").should().resideInAPackage("..validator..").check(classes);
		}

		@Test
		@DisplayName("名前が Controller で終わる API のクラスには @RestController が付いている")
		void controllersAreRestControllers() {
			classes().that().resideInAPackage(BASE + ".api..").and().haveSimpleNameEndingWith("Controller").should().beAnnotatedWith(RestController.class).check(classes);
		}
	}

	@Nested
	@DisplayName("書き方（CLAUDE.md 3章・5章）")
	class Style {

		/**
		 * record は使わない（CLAUDE.md 3章）。
		 * 例外：決まりに合わないまま入っているもの（common.error.ApiResponse・ErrorItem、common.security.LoginUser、
		 * common.config.AppProperties とその中のクラス）。普通のクラスに直すかは、人が決める。増やさない。
		 */
		private static final java.util.Set<String> RECORD_EXCEPTIONS = java.util.Set.of(
				BASE + ".common.error.ApiResponse", BASE + ".common.error.ErrorItem",
				BASE + ".common.security.LoginUser", BASE + ".common.config.AppProperties");

		@Test
		@DisplayName("record は使わない（例外は、RECORD_EXCEPTIONS の4つだけ）")
		void noRecords() {
			noClasses().that(DescribedPredicate.describe("例外の record でない",
							(JavaClass c) -> !RECORD_EXCEPTIONS.contains(c.getName().split(Pattern.quote("$"))[0])))
					.should().beAssignableTo(Record.class).check(classes);
		}

		@Test
		@DisplayName("今の日時を、直接取らない（LocalDate.now()・LocalDateTime.now()・Instant.now()・new Date() など。java.time.Clock から取る）")
		void useClock() {
			noClasses().should().callMethod(LocalDate.class, "now")
					.orShould().callMethod(LocalDateTime.class, "now")
					.orShould().callMethod(LocalTime.class, "now")
					.orShould().callMethod(ZonedDateTime.class, "now")
					.orShould().callMethod(Instant.class, "now")
					.orShould().callConstructor(Date.class)
					.orShould().callMethod(System.class, "currentTimeMillis").check(classes);
		}

		@Test
		@DisplayName("@Transactional は、Controller・Mapper・Service の interface に付けない（ServiceImpl などに付ける）")
		void transactionalPlacement() {
			noClasses().that().resideInAPackage("..controller..").should().beAnnotatedWith(Transactional.class).check(classes);
			noMethods().that().areDeclaredInClassesThat().resideInAPackage("..controller..").should().beAnnotatedWith(Transactional.class).check(classes);
			noClasses().that().resideInAPackage(BASE + ".dao.mapper..").should().beAnnotatedWith(Transactional.class).check(classes);
			noMethods().that().areDeclaredInClassesThat().resideInAPackage(BASE + ".dao.mapper..").should().beAnnotatedWith(Transactional.class).check(classes);
			noClasses().that().areInterfaces().and().resideInAPackage(BASE + ".api..").should().beAnnotatedWith(Transactional.class).check(classes);
			noMethods().that().areDeclaredInClassesThat().areInterfaces().and().areDeclaredInClassesThat().resideInAPackage(BASE + ".api..")
					.should().beAnnotatedWith(Transactional.class).check(classes);
		}
	}

	@Nested
	@DisplayName("権限の確認（CLAUDE.md 5章）")
	class Authorization {

		/**
		 * ログインしなくても呼べる API（SecurityConfig で permitAll にしているもの。ログイン・パスワードの再設定・死活の確認）。
		 * これ以外の API の Controller の、URL を持つメソッドには、すべて @PreAuthorize が要る。増やすときは、人に確かめる。
		 */
		private static final java.util.Set<String> PUBLIC_CONTROLLERS = java.util.Set.of(
				BASE + ".api.auth.login.controller.AuthLoginController",
				BASE + ".api.auth.passwordresetrequests.controller.AuthPasswordResetRequestsController",
				BASE + ".api.auth.passwordtokens.use.controller.AuthPasswordTokensUseController",
				BASE + ".api.auth.passwordtokens.verify.controller.AuthPasswordTokensVerifyController",
				BASE + ".api.health.controller.HealthController");

		@Test
		@DisplayName("API の Controller の、URL を持つメソッドには @PreAuthorize が付いている（ログイン不要の5つの Controller を除く）")
		void everyEndpointHasPreAuthorize() {
			classes().that().resideInAPackage(BASE + ".api..controller..").and().areAnnotatedWith(RestController.class)
					.and(DescribedPredicate.describe("ログイン不要の Controller でない", (JavaClass c) -> !PUBLIC_CONTROLLERS.contains(c.getName())))
					.should(new ArchCondition<JavaClass>("URL を持つメソッドに @PreAuthorize が付いている") {
						@Override
						public void check(JavaClass controller, ConditionEvents events) {
							controller.getMethods().stream()
									.filter(m -> m.isAnnotatedWith(GetMapping.class) || m.isAnnotatedWith(PostMapping.class)
											|| m.isAnnotatedWith(PutMapping.class) || m.isAnnotatedWith(DeleteMapping.class))
									.filter(m -> !m.isAnnotatedWith(PreAuthorize.class))
									.forEach(m -> events.add(SimpleConditionEvent.violated(m, m.getFullName() + " に @PreAuthorize がない")));
						}
					}).check(classes);
		}

		@Test
		@DisplayName("ログイン不要の Controller の一覧が、実在するクラスだけ（消した・名前を変えたものが残らない）")
		void publicControllersExist() {
			for (String name : PUBLIC_CONTROLLERS) {
				if (!classes.contain(name)) {
					throw new AssertionError(name + " がない。PUBLIC_CONTROLLERS を直す");
				}
			}
		}
	}
}

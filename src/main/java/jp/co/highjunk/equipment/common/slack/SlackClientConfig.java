package jp.co.highjunk.equipment.common.slack;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;

import jp.co.highjunk.equipment.common.config.AppProperties;

/**
 * Slack にボットトークンでつなぐ部品を用意する（詳細設計 common.md 3.13）。
 * 本物に送るとき（app.test-support.enabled が false）だけ動く。
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "app.test-support", name = "enabled", havingValue = "false", matchIfMissing = true)
public class SlackClientConfig {

	@Bean
	public MethodsClient slackMethodsClient(AppProperties appProperties) {
		return Slack.getInstance().methods(appProperties.slack().botToken());
	}
}

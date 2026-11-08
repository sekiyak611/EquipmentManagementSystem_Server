package jp.co.highjunk.equipment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

/**
 * 備品管理システムのサーバーを起動する入口。
 *
 * <p>UserDetailsServiceAutoConfiguration は使わない。使うと、Spring Boot が仮のユーザを作り、
 * そのパスワードをログに出してしまうため（ログにパスワードを出さない決まり。CLAUDE.md 5章）。
 * ログイン（auth 機能。ZPOP-55）は UserDetailsService を使わずに作ったので、外したままでよい。
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@ConfigurationPropertiesScan
public class EquipmentApplication {

	public static void main(String[] args) {
		SpringApplication.run(EquipmentApplication.class, args);
	}
}

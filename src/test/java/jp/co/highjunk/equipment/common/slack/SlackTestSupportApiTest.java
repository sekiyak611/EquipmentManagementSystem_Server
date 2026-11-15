package jp.co.highjunk.equipment.common.slack;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 送った内容を読み出すAPI（詳細設計 common.md 3.13）のテスト。
 * テストの設定（app.test-support.enabled が true）と、本番の設定（false）の両方を確かめる。
 */
class SlackTestSupportApiTest {

	@Nested
	@SpringBootTest
	@AutoConfigureMockMvc
	@DisplayName("テストの設定（app.test-support.enabled が true）")
	class Enabled {

		@Autowired
		MockMvc mockMvc;

		@Autowired
		SlackSender slackSender;

		@Autowired
		ApplicationContext context;

		@BeforeEach
		void clearRecords() throws Exception {
			mockMvc.perform(delete("/api/test-support/slack-messages")).andExpect(status().isOk());
		}

		@Test
		@DisplayName("送り手は偽物（FakeSlackSender）で、本物（SlackApiSender）は動かない")
		void fakeIsUsed() {
			assertThat(slackSender).isInstanceOf(FakeSlackSender.class);
			assertThat(context.getBeansOfType(SlackApiSender.class)).isEmpty();
		}

		@Test
		@DisplayName("1通も送っていなければ、messages は空の配列")
		void emptyList() throws Exception {
			mockMvc.perform(get("/api/test-support/slack-messages"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.status").value(200))
					.andExpect(jsonPath("$.data.messages").isArray())
					.andExpect(jsonPath("$.data.messages.length()").value(0));
		}

		@Test
		@DisplayName("送った内容が古い順に出る。ログインなしで呼べる")
		void listsSentMessagesOldestFirst() throws Exception {
			slackSender.sendDirectMessage("a@example.com", new SlackDirectMessage("見出し1", "本文1"));
			slackSender.sendDirectMessage("b@example.com", new SlackDirectMessage("見出し2", "本文2"));

			mockMvc.perform(get("/api/test-support/slack-messages"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.data.messages.length()").value(2))
					.andExpect(jsonPath("$.data.messages[0].email").value("a@example.com"))
					.andExpect(jsonPath("$.data.messages[0].title").value("見出し1"))
					.andExpect(jsonPath("$.data.messages[0].body").value("本文1"))
					.andExpect(jsonPath("$.data.messages[0].sentAt").value(org.hamcrest.Matchers.endsWith("+09:00")))
					.andExpect(jsonPath("$.data.messages[1].email").value("b@example.com"));
		}

		@Test
		@DisplayName("DELETE で空になる。CSRFの合言葉なしで呼べる")
		void deleteClears() throws Exception {
			slackSender.sendDirectMessage("a@example.com", new SlackDirectMessage("見出し", "本文"));

			mockMvc.perform(delete("/api/test-support/slack-messages"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.data").doesNotExist());

			mockMvc.perform(get("/api/test-support/slack-messages"))
					.andExpect(jsonPath("$.data.messages.length()").value(0));
		}
	}

	@Nested
	@SpringBootTest
	@AutoConfigureMockMvc
	@TestPropertySource(properties = "app.test-support.enabled=false")
	@DisplayName("本番の設定（app.test-support.enabled が false）")
	class Disabled {

		@Autowired
		MockMvc mockMvc;

		@Autowired
		SlackSender slackSender;

		@Autowired
		ApplicationContext context;

		@Test
		@DisplayName("送り手は本物（SlackApiSender）で、偽物と読み出しの controller はない")
		void realSenderIsUsed() {
			assertThat(slackSender).isInstanceOf(SlackApiSender.class);
			assertThat(context.getBeansOfType(FakeSlackSender.class)).isEmpty();
			assertThat(context.getBeansOfType(SlackTestSupportController.class)).isEmpty();
		}

		@Test
		@DisplayName("GET・DELETE とも 404（ログインなしでも、401 ではなく 404）")
		void notFound() throws Exception {
			mockMvc.perform(get("/api/test-support/slack-messages")).andExpect(status().isNotFound());
			mockMvc.perform(delete("/api/test-support/slack-messages")).andExpect(status().isNotFound());
		}
	}
}

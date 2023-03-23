/*
 * Copyright 2014-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.session.data.redis.config.annotation.web.http;

import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.session.data.redis.config.ConfigureNotifyKeyspaceEventsAction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EnableRedisKeyspaceNotificationsInitializerTests {

	private static final String CONFIG_NOTIFY_KEYSPACE_EVENTS = "notify-keyspace-events";

	@Mock
	RedisConnectionFactory connectionFactory;

	@Mock
	RedisConnection connection;

	@Mock
	RedisServerCommands commands;

	@Captor
	ArgumentCaptor<String> options;

	private RedisIndexedHttpSessionConfiguration.EnableRedisKeyspaceNotificationsInitializer initializer;

	@BeforeEach
	void setup() {
		given(this.connectionFactory.getConnection()).willReturn(this.connection);
		given(this.connection.serverCommands()).willReturn(this.commands);

		this.initializer = new RedisIndexedHttpSessionConfiguration.EnableRedisKeyspaceNotificationsInitializer(
				this.connectionFactory, new ConfigureNotifyKeyspaceEventsAction());
	}

	@Test
	void afterPropertiesSetUnset() {
		setConfigNotification("");

		this.initializer.afterPropertiesSet();

		assertOptionsContains("E", "g", "x");
	}

	@Test
	void afterPropertiesSetA() {
		setConfigNotification("A");

		this.initializer.afterPropertiesSet();

		assertOptionsContains("A", "E");
	}

	@Test
	void afterPropertiesSetE() {
		setConfigNotification("E");

		this.initializer.afterPropertiesSet();

		assertOptionsContains("E", "g", "x");
	}

	@Test
	void afterPropertiesSetK() {
		setConfigNotification("K");

		this.initializer.afterPropertiesSet();

		assertOptionsContains("K", "E", "g", "x");
	}

	@Test
	void afterPropertiesSetAE() {
		setConfigNotification("AE");

		this.initializer.afterPropertiesSet();

		verify(this.commands, never()).setConfig(anyString(), anyString());
	}

	@Test
	void afterPropertiesSetAK() {
		setConfigNotification("AK");

		this.initializer.afterPropertiesSet();

		assertOptionsContains("A", "K", "E");
	}

	@Test
	void afterPropertiesSetEK() {
		setConfigNotification("EK");

		this.initializer.afterPropertiesSet();

		assertOptionsContains("E", "K", "g", "x");
	}

	@Test
	void afterPropertiesSetEg() {
		setConfigNotification("Eg");

		this.initializer.afterPropertiesSet();

		assertOptionsContains("E", "g", "x");
	}

	@Test
	void afterPropertiesSetE$() {
		setConfigNotification("E$");

		this.initializer.afterPropertiesSet();

		assertOptionsContains("E", "$", "g", "x");
	}

	@Test
	void afterPropertiesSetKg() {
		setConfigNotification("Kg");

		this.initializer.afterPropertiesSet();

		assertOptionsContains("K", "g", "E", "x");
	}

	@Test
	void afterPropertiesSetAEK() {
		setConfigNotification("AEK");

		this.initializer.afterPropertiesSet();

		verify(this.commands, never()).setConfig(anyString(), anyString());
	}

	private void assertOptionsContains(String... expectedValues) {
		verify(this.commands).setConfig(eq(CONFIG_NOTIFY_KEYSPACE_EVENTS), this.options.capture());
		for (String expectedValue : expectedValues) {
			assertThat(this.options.getValue()).contains(expectedValue);
		}
		assertThat(this.options.getValue().length()).isEqualTo(expectedValues.length);
	}

	private void setConfigNotification(String value) {
		Properties properties = new Properties();
		properties.setProperty(CONFIG_NOTIFY_KEYSPACE_EVENTS, value);
		given(this.commands.getConfig(CONFIG_NOTIFY_KEYSPACE_EVENTS)).willReturn(properties);
	}

}

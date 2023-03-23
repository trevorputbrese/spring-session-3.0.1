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

package docs;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;

/**
 * @author Rob Winch
 *
 */
@ExtendWith(MockitoExtension.class)
class FindByIndexNameSessionRepositoryTests {

	@Mock
	FindByIndexNameSessionRepository<Session> sessionRepository;

	@Mock
	Session session;

	@Test
	void setUsername() {
		// tag::set-username[]
		String username = "username";
		this.session.setAttribute(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME, username);
		// end::set-username[]
	}

	@Test
	void findByUsername() {
		// tag::findby-username[]
		String username = "username";
		Map<String, Session> sessionIdToSession = this.sessionRepository.findByPrincipalName(username);
		// end::findby-username[]
	}

}

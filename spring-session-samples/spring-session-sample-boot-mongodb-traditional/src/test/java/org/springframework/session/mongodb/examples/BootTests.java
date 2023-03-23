/*
 * Copyright 2014-2017 the original author or authors.
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

package org.springframework.session.mongodb.examples;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.session.mongodb.examples.pages.HomePage;
import org.springframework.session.mongodb.examples.pages.LoginPage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.htmlunit.webdriver.MockMvcHtmlUnitDriverBuilder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Pool Dolorier
 */
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@ContextConfiguration(initializers = SpringSessionMongoTraditionalBoot.Initializer.class)
public class BootTests {

	@Autowired
	private MockMvc mockMvc;

	private WebDriver driver;

	@BeforeEach
	void setUp() {
		this.driver = MockMvcHtmlUnitDriverBuilder.mockMvcSetup(this.mockMvc).build();
	}

	@AfterEach
	void tearDown() {
		this.driver.quit();
	}

	@Test
	void unauthenticatedUserSentToLogInPage() {

		HomePage homePage = HomePage.go(this.driver);
		LoginPage loginPage = homePage.unauthenticated();
		loginPage.assertAt();
	}

	@Test
	void logInViewsHomePage() {

		LoginPage loginPage = LoginPage.go(this.driver);
		loginPage.assertAt();

		HomePage homePage = loginPage.login("user", "password");
		homePage.assertAt();

		WebElement username = homePage.getDriver().findElement(By.id("un"));
		assertThat(username.getText()).isEqualTo("user");
		Set<Cookie> cookies = homePage.getDriver().manage().getCookies();
		assertThat(cookies).extracting("name").contains("SESSION");
		assertThat(cookies).extracting("name").doesNotContain("JSESSIONID");
	}

	@Test
	void logoutSuccess() {

		LoginPage loginPage = LoginPage.go(this.driver);
		HomePage homePage = loginPage.login("user", "password");
		LoginPage successLogoutPage = homePage.logout();

		successLogoutPage.assertAt();
	}

	@Test
	void loggedOutUserSentToLoginPage() {

		LoginPage loginPage = LoginPage.go(this.driver);
		HomePage homePage = loginPage.login("user", "password");
		homePage.logout();

		HomePage backHomePage = HomePage.go(this.driver);
		LoginPage backLoginPage = backHomePage.unauthenticated();

		backLoginPage.assertAt();
	}

}

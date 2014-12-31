/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2014 Wisdom Framework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.wisdom.mailer;

import com.google.common.base.Charsets;
import org.apache.felix.ipojo.Pojo;
import org.junit.After;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.ow2.chameleon.mail.Mail;
import org.ow2.chameleon.mail.MailSenderService;
import org.ow2.chameleon.testing.helpers.OSGiHelper;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.test.parents.WisdomTest;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * Check the Smtp service.
 */
public class SmtpIT extends WisdomTest {
    public static final String USERNAME = "ow2.chameleon.test@googlemail.com";
    public static final String PASSWORD = "chameleon";

    @Inject
    MailSenderService mailer;

    @Inject
    ApplicationConfiguration applicationConfiguration;

    @After
    public void tearDown() {
        // Clear all related system properties
        System.clearProperty("mail.smtp.connection");
        System.clearProperty("mail.smtp.host");
        System.clearProperty("mail.smtp.port");
        System.clearProperty("mail.smtp.from");
        System.clearProperty("mail.smtp.username");
        System.clearProperty("mail.smtp.password");
        System.clearProperty("mail.smtp.debug");

        // Restart the application configuration service to use the new system properties
        restartApplicationConfiguration();
    }

    @Test
    @Category(Mock.class)
    public void mock() throws Exception {
        mailer.send(new Mail()
                .to("clement@wisdom.org")
                .subject("Hello from wisdom")
                .body("Hi !"));
    }

    @Test
    @Category(Real.class)
    public void gmail() throws Exception {
        // We set the system properties to use gmail and force the instance to be reconfigured.
        setGmailProperties();
        ((Pojo) mailer).getComponentInstance().reconfigure(new Properties());

        mailer.send(new Mail()
                .from(USERNAME)
                .to("clement.escoffier@gmail.com")
                .subject("[IT-TEST] Hello from wisdom")
                .body("This is a test. Wisdom is sending this mail using its own mailer service. \n Wisdom"));
    }

    @Test
    @Category(Real.class)
    public void gmailWithCharsetAndMime() throws Exception {
        // We set the system properties to use gmail and force the instance to be reconfigured.
        setGmailProperties();
        ((Pojo) mailer).getComponentInstance().reconfigure(new Properties());

        mailer.send(new Mail()
                .from(USERNAME)
                .to("clement.escoffier@gmail.com")
                .subject("[IT-TEST] Data")
                .body("<html><body><h1>Hello</h1><p>Here is data using accent and symbols - å é è ß ∑ Ω " +
                        "</p></body></html>")
                .charset(Charsets.UTF_8.toString())
                .subType("html"));
    }

    private void setGmailProperties() {
        System.setProperty("mail.smtp.connection", "SSL");
        System.setProperty("mail.smtp.host", "smtp.gmail.com");
        System.setProperty("mail.smtp.port", "465");
        System.setProperty("mail.smtp.from", USERNAME);
        System.setProperty("mail.smtp.username", USERNAME);
        System.setProperty("mail.smtp.password", PASSWORD);
        System.setProperty("mail.smtp.debug", "true");

        // Restart the application configuration service to use the new system properties
        restartApplicationConfiguration();
    }

    private void restartApplicationConfiguration() {
        try {
            Method reload = applicationConfiguration.getClass().getDeclaredMethod("reloadConfiguration");
            reload.setAccessible(true);
            reload.invoke(applicationConfiguration);
            new OSGiHelper(context).waitForService(ApplicationConfiguration.class, null, 1000);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

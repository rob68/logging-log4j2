/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package org.apache.logging.log4j.jdk;

import java.util.List;
import java.util.logging.Logger;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.test.appender.ListAppender;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;

public class ApiLoggerTest {

    public static final String LOGGER_NAME = "Test";
    private Logger logger;
    private ListAppender eventAppender;
    private ListAppender stringAppender;

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("java.util.logging.manager", LogManager.class.getName());
        System.setProperty(LogManager.LOGGER_ADAPTOR_PROPERTY, ApiLoggerAdapter.class.getName());
    }

    @Before
    public void setUp() throws Exception {
        logger = Logger.getLogger(LOGGER_NAME);
        assertThat(logger.getLevel(), equalTo(java.util.logging.Level.FINE));
        eventAppender = ListAppender.getListAppender("TestAppender");
        stringAppender = ListAppender.getListAppender("StringAppender");
    }

    @After
    public void tearDown() throws Exception {
        eventAppender.clear();
    }

    @Test
    public void testLog() throws Exception {
        logger.info("Informative message here.");
        final List<LogEvent> events = eventAppender.getEvents();
        assertThat(events, hasSize(1));
        final LogEvent event = events.get(0);
        assertThat(event, instanceOf(Log4jLogEvent.class));
        assertEquals(Level.INFO, event.getLevel());
        assertEquals(LOGGER_NAME, event.getLoggerName());
        assertEquals("Informative message here.", event.getMessage().getFormattedMessage());
        assertEquals(Logger.class.getName(), event.getLoggerFqcn());
    }

    @Test
    public void testLogWithCallingClass() throws Exception {
        final Logger log = Logger.getLogger("Test.CallerClass");
        log.config("Calling from LoggerTest");
        final List<String> messages = stringAppender.getMessages();
        assertThat(messages, hasSize(1));
        final String message = messages.get(0);
        assertEquals(getClass().getName(), message);
    }

    @Test
    public void testLogUsingCustomLevel() throws Exception {
        logger.log(CustomJdkLevel.TEST, "Test level");
        final List<LogEvent> events = eventAppender.getEvents();
        assertThat(events, hasSize(1));
        final LogEvent event = events.get(0);
        assertThat(event.getLevel(), equalTo(Level.INFO));
        final String levelName = event.getContextMap().get(ApiLogger.LEVEL);
        assertThat(levelName, equalTo(CustomJdkLevel.TEST.getName()));
    }

    @Test
    public void testIsLoggable() throws Exception {
        assertThat(logger.isLoggable(java.util.logging.Level.SEVERE), equalTo(true));
        assertThat(logger.isLoggable(CustomJdkLevel.DEFCON_1), equalTo(true));
    }

    @Test
    public void testGetName() throws Exception {
        assertThat(logger.getName(), equalTo(LOGGER_NAME));
    }

    @Test
    public void testGlobalLoggerName() throws Exception {
        final Logger root = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        assertThat(root.getName(), equalTo(Logger.GLOBAL_LOGGER_NAME));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetParentFails() throws Exception {
        logger.getParent();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetParentFails() throws Exception {
        logger.setParent(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetLevelFails() throws Exception {
        logger.setLevel(null);
    }
}
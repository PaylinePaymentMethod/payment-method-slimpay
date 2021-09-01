package com.payline.payment.slimpay.bean.common;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillingAddressTest {

    @Mock
    private Appender appender;

    @Captor
    private ArgumentCaptor<LogEvent> captor;

    private BillingAddress address;

    private LoggerConfig loggerConfig;

    @BeforeEach
    void setUp() {
        when(appender.getName()).thenReturn("MockAppender");
        lenient().when(appender.isStarted()).thenReturn(true);

        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        loggerConfig = config.getLoggerConfig("com.payline.payment.slimpay.bean.common.BillingAddress");
        loggerConfig.addAppender(appender, Level.INFO, null);
    }

    @AfterEach
    void tearDown() {
        loggerConfig.removeAppender("MockAppender");
    }

    @Test
    void billingAddressTestOK(){
        address = BillingAddress.Builder.aBillingAddressBuilder()
                .withStreet1("10 rue de la paix")
                .withStreet2("residence peace")
                .withCity("Versailles")
                .withCountry("FR")
                .withPostalCode("78000")
                .build();

        String jsonAddress= address.toString();
        Assertions.assertTrue(jsonAddress.contains("10 rue de la paix"));
        Assertions.assertTrue(jsonAddress.contains("residence peace"));
        Assertions.assertTrue(jsonAddress.contains("Versailles"));
        Assertions.assertTrue(jsonAddress.contains("FR"));
        Assertions.assertTrue(jsonAddress.contains("78000"));
    }

    @Test
    void billingAddressTestKO(){
        address = BillingAddress.Builder.aBillingAddressBuilder()
                .build();

        Mockito.verify(appender, Mockito.atLeastOnce()).append(captor.capture());
        final List<LogEvent> logs = captor.getAllValues();
        assertEquals(4, logs.size());

        assertEquals(Level.WARN, logs.get(0).getLevel());
        assertEquals(BillingAddress.STREET_WARN, logs.get(0).getMessage().getFormattedMessage());
        assertEquals(Level.WARN, logs.get(1).getLevel());
        assertEquals(BillingAddress.CITY_WARN, logs.get(1).getMessage().getFormattedMessage());
        assertEquals(Level.WARN, logs.get(2).getLevel());
        assertEquals(BillingAddress.POSTAL_CODE_WARN, logs.get(2).getMessage().getFormattedMessage());
        assertEquals(Level.WARN, logs.get(3).getLevel());
        assertEquals(BillingAddress.COUNTRY_WARN, logs.get(3).getMessage().getFormattedMessage());
    }
}

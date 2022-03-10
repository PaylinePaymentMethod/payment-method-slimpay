package com.payline.payment.slimpay.bean.common;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

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
        doReturn("MockAppender").when(appender).getName();
        lenient().doReturn(true).when(appender).isStarted();
        lenient().doReturn(false).when(appender).isStopped();

        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        loggerConfig = config.getLoggerConfig("com.payline.payment.slimpay.bean.common.BillingAddress");
        loggerConfig.addAppender(appender, Level.WARN, null);
        Configurator.setRootLevel(Level.WARN);
        ctx.updateLoggers();
    }

    @AfterEach
    void tearDown() {
        loggerConfig.removeAppender("MockAppender");
    }

    @Test
    void billingAddressTestOK(){
        address = getValidBillingAdress().build();

        String jsonAddress= address.toString();
        Assertions.assertTrue(jsonAddress.contains("10 rue de la paix"));
        Assertions.assertTrue(jsonAddress.contains("residence peace"));
        Assertions.assertTrue(jsonAddress.contains("Versailles"));
        Assertions.assertTrue(jsonAddress.contains("FR"));
        Assertions.assertTrue(jsonAddress.contains("78000"));
    }

    private BillingAddress.Builder getValidBillingAdress() {
        return BillingAddress.Builder.aBillingAddressBuilder()
                .withStreet1("10 rue de la paix")
                .withStreet2("residence peace")
                .withCity("Versailles")
                .withCountry("FR")
                .withPostalCode("78000");
    }

    @Nested
    class billingaddresstestko {
        @Test
        void billingAddressTestKOStreet(){
            address = getValidBillingAdress()
                    .withStreet1(null)
                    .build();

            verify(appender, times(1)).append(captor.capture());
            assertEquals(BillingAddress.STREET_WARN, captor.getValue().getMessage().getFormattedMessage());
        }

        @Test
        void billingAddressTestKOCity(){
            address = getValidBillingAdress()
                    .withCity(null)
                    .build();

            verify(appender, times(1)).append(captor.capture());
            assertEquals(BillingAddress.CITY_WARN, captor.getValue().getMessage().getFormattedMessage());
        }

        @Test
        void billingAddressTestKOPostal(){
            address = getValidBillingAdress()
                    .withPostalCode(null)
                    .build();

            verify(appender, times(1)).append(captor.capture());
            assertEquals(BillingAddress.POSTAL_CODE_WARN, captor.getValue().getMessage().getFormattedMessage());
        }

        @Test
        void billingAddressTestKOCountry(){
            address = getValidBillingAdress()
                    .withCountry(null)
                    .build();

            verify(appender, times(1)).append(captor.capture());
            assertEquals(BillingAddress.COUNTRY_WARN, captor.getValue().getMessage().getFormattedMessage());
        }
    }
}

package com.payline.payment.slimpay.bean.common;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentTest {

    @Mock
    private Appender appender;

    @Captor
    private ArgumentCaptor<LogEvent> captor;

    private Payment payment;

    private LoggerConfig loggerConfig;

    @BeforeEach
    void setUp() {
        when(appender.getName()).thenReturn("MockAppender");
        lenient().when(appender.isStarted()).thenReturn(true);

        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        loggerConfig = config.getLoggerConfig("com.payline.payment.slimpay.bean.common.PaymentTest");
        loggerConfig.addAppender(appender, Level.INFO, null);
    }

    @AfterEach
    void tearDown() {
        loggerConfig.removeAppender("MockAppender");
    }

    @Test
    void testPaymentOK(){
        payment = getValidPayment()
                .build();
        String jsonPayment = payment.toString();
        Assertions.assertTrue(jsonPayment.contains("reference"));
        Assertions.assertTrue(jsonPayment.contains("scheme"));
        Assertions.assertTrue(jsonPayment.contains("direction"));
        Assertions.assertTrue(jsonPayment.contains("amount"));
        Assertions.assertTrue(jsonPayment.contains("currency"));
        Assertions.assertTrue(jsonPayment.contains("label"));
    }

    private Payment.Builder getValidPayment() {
        return Payment.Builder.aPaymentBuilder()
                .withReference("PAYMENT-REF-1")
                .withScheme("SEPA.DIRECT_DEBIT.CORE")
                .withDirection("IN")
                .withAction("create")
                .withAmount("100")
                .withCurrency("EUR")
                .withLabel("the label");
    }


    @Nested
    class testInvalidPayment {
        @Test
        void testPaymentKOReference(){
            payment = getValidPayment()
                    .withReference(null)
                    .build();
            //test on logs
            verify(appender, Mockito.atLeastOnce()).append(captor.capture());
            assertEquals(Payment.REFERENCE_WARN, captor.getValue().getMessage().getFormattedMessage());
        }
        @Test
        void testPaymentKOScheme(){
            payment = getValidPayment()
                    .withScheme(null)
                    .build();
            //test on logs
            verify(appender, times(1)).append(captor.capture());
            assertEquals(Payment.SCHEME_WARN, captor.getValue().getMessage().getFormattedMessage());
        }
        @Test
        void testPaymentKOAmount(){
            payment = getValidPayment()
                    .withAmount(null)
                    .build();
            //test on logs
            verify(appender, times(1)).append(captor.capture());
            assertEquals(Payment.AMOUNT_WARN, captor.getValue().getMessage().getFormattedMessage());
        }
        @Test
        void testPaymentKOCurrency(){
            payment = getValidPayment()
                    .withCurrency(null)
                    .build();
            //test on logs
            verify(appender, times(1)).append(captor.capture());
            assertEquals(Payment.CURRENCY_WARN, captor.getValue().getMessage().getFormattedMessage());
        }
        @Test
        void testPaymentKODirection(){
            payment = getValidPayment()
                    .withDirection(null)
                    .build();
            //test on logs
            verify(appender, times(1)).append(captor.capture());
            assertEquals(Payment.DIRECTION_WARN, captor.getValue().getMessage().getFormattedMessage());
        }
        @Test
        void testPaymentWithWrongDirection(){
            payment = getValidPayment()
                    .withDirection("ouest")
                    .build();

            String jsonPayment = payment.toString();
            Assertions.assertTrue(jsonPayment.contains("direction"));

            verify(appender, times(1)).append(captor.capture());
            assertEquals(Payment.WRONG_DIRECTION_WARN, captor.getValue().getMessage().getFormattedMessage());
        }
    }

}

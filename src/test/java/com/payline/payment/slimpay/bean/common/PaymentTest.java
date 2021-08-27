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
        payment = Payment.Builder.aPaymentBuilder()
                .withReference("PAYMENT-REF-1")
                .withScheme("SEPA.DIRECT_DEBIT.CORE")
                .withDirection("IN")
                .withAction("create")
                .withAmount("100")
                .withCurrency("EUR")
                .withLabel("the label")
                .build();
        String jsonPayment = payment.toString();
        Assertions.assertTrue(jsonPayment.contains("reference"));
        Assertions.assertTrue(jsonPayment.contains("scheme"));
        Assertions.assertTrue(jsonPayment.contains("direction"));
        Assertions.assertTrue(jsonPayment.contains("amount"));
        Assertions.assertTrue(jsonPayment.contains("currency"));
        Assertions.assertTrue(jsonPayment.contains("label"));
    }


    @Test
    void testPaymentKO(){
        payment = Payment.Builder.aPaymentBuilder()
                .withAction("payin")
                .withLabel("the label")
                .build();
        //test on logs
        Mockito.verify(appender, Mockito.atLeastOnce()).append(captor.capture());
        final List<LogEvent> logs = captor.getAllValues();
        assertEquals(5, logs.size());

        assertEquals(Level.WARN, logs.get(0).getLevel());
        assertEquals(Payment.REFERENCE_WARN, logs.get(0).getMessage().getFormattedMessage());
        assertEquals(Level.WARN, logs.get(1).getLevel());
        assertEquals(Payment.SCHEME_WARN, logs.get(1).getMessage().getFormattedMessage());
        assertEquals(Level.WARN, logs.get(2).getLevel());
        assertEquals(Payment.AMOUNT_WARN, logs.get(2).getMessage().getFormattedMessage());
        assertEquals(Level.WARN, logs.get(3).getLevel());
        assertEquals(Payment.CURRENCY_WARN, logs.get(3).getMessage().getFormattedMessage());
        assertEquals(Level.WARN, logs.get(4).getLevel());
        assertEquals(Payment.DIRECTION_WARN, logs.get(4).getMessage().getFormattedMessage());
    }



    @Test
    void testPaymentWithWrongDirection(){
        payment = Payment.Builder.aPaymentBuilder()
                .withDirection("ouest")
                .build();
        System.out.println(payment);
        String jsonPayment = payment.toString();
        Assertions.assertTrue(jsonPayment.contains("direction"));

        Mockito.verify(appender, Mockito.atLeastOnce()).append(captor.capture());
        final List<LogEvent> logs = captor.getAllValues();
        assertEquals(6, logs.size());

        assertEquals(Level.WARN, logs.get(0).getLevel());
        assertEquals(Payment.REFERENCE_WARN, logs.get(0).getMessage().getFormattedMessage());
        assertEquals(Level.WARN, logs.get(1).getLevel());
        assertEquals(Payment.SCHEME_WARN, logs.get(1).getMessage().getFormattedMessage());
        assertEquals(Level.WARN, logs.get(2).getLevel());
        assertEquals(Payment.AMOUNT_WARN, logs.get(2).getMessage().getFormattedMessage());
        assertEquals(Level.WARN, logs.get(3).getLevel());
        assertEquals(Payment.CURRENCY_WARN, logs.get(3).getMessage().getFormattedMessage());
        assertEquals(Level.WARN, logs.get(4).getLevel());
        assertEquals(Payment.ACTION_WARN, logs.get(4).getMessage().getFormattedMessage());
        assertEquals(Level.WARN, logs.get(5).getLevel());
        assertEquals(Payment.WRONG_DIRECTION_WARN, logs.get(5).getMessage().getFormattedMessage());
    }
}

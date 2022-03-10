package com.payline.payment.slimpay.bean.common;


import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

import static com.payline.payment.slimpay.utils.BeansUtils.createDefaultSignatory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class MandateTest {

    private static final Logger LOGGER = LogManager.getLogger(MandateTest.class);

    public static final String CREATE_SEQUENCE_TYPE = "createSequenceType";
    public static final String SEPA_DIRECT_DEBIT_CORE = "SEPA.DIRECT_DEBIT.CORE";
    public static final String SEPA = "SEPA";
    public static final String REFERENCE = "reference";
    public static final String PAYMENT_SCHEME = "paymentScheme";
    public static final String SIGNATORY = "signatory";
    public static final String STANDARD = "standard";
    public static final String PAYMENT_REF_1 = "PAYMENT-REF-1";
    private Mandate mandate;

    @Mock
    private Appender appender;

    @Captor
    private ArgumentCaptor<LogEvent> captor;

    private LoggerConfig loggerConfig;

    @BeforeEach
    void setUp() {
        when(appender.getName()).thenReturn("MockAppender");
        lenient().when(appender.isStarted()).thenReturn(true);

        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        loggerConfig = config.getLoggerConfig("com.payline.payment.slimpay.bean.common.MandateTest");
        loggerConfig.addAppender(appender, Level.INFO, null);
    }

    @AfterEach
    void tearDown() {
        loggerConfig.removeAppender("MockAppender");
    }

    @Test
    void testMandateOK() {

        mandate = Mandate.Builder.aMandateBuilder()
                .withReference(PAYMENT_REF_1)
                .withSignatory(createDefaultSignatory())
                .withStandard(SEPA)
                .withPaymentScheme(SEPA_DIRECT_DEBIT_CORE)
                .withCreateSequenceType(CREATE_SEQUENCE_TYPE)
                .build();
        String jsonMandate = mandate.toString();

        Mockito.verify(appender, Mockito.never()).append(any());

        LOGGER.info(jsonMandate);
        Assertions.assertTrue(jsonMandate.contains(REFERENCE));
        Assertions.assertTrue(jsonMandate.contains(PAYMENT_SCHEME));
        Assertions.assertTrue(jsonMandate.contains(SIGNATORY));
        Assertions.assertTrue(jsonMandate.contains(STANDARD));
        Assertions.assertTrue(jsonMandate.contains(CREATE_SEQUENCE_TYPE));

    }


    @Test
    void testMandateWoReference() {

        mandate = Mandate.Builder.aMandateBuilder()
                .withSignatory(createDefaultSignatory())
                .withStandard(SEPA)
                .withPaymentScheme(SEPA_DIRECT_DEBIT_CORE)
                .withCreateSequenceType(CREATE_SEQUENCE_TYPE)
                .build();

        Mockito.verify(appender, Mockito.atLeastOnce()).append(captor.capture());
        final List<LogEvent> logs = captor.getAllValues();
        assertEquals(1, logs.size());
        assertEquals(Mandate.REFERENCE_WARN, logs.get(0).getMessage().getFormattedMessage());

        String jsonMandate = mandate.toString();
        LOGGER.info(jsonMandate);
        Assertions.assertFalse(jsonMandate.contains(REFERENCE));
        Assertions.assertTrue(jsonMandate.contains(PAYMENT_SCHEME));
        Assertions.assertTrue(jsonMandate.contains(SIGNATORY));
        Assertions.assertTrue(jsonMandate.contains(STANDARD));
        Assertions.assertTrue(jsonMandate.contains(CREATE_SEQUENCE_TYPE));

    }


    @Test
    void testMandateWoSignatory() {

        mandate = Mandate.Builder.aMandateBuilder()
                .withReference(PAYMENT_REF_1)
                .withStandard(SEPA)
                .withPaymentScheme(SEPA_DIRECT_DEBIT_CORE)
                .withCreateSequenceType(CREATE_SEQUENCE_TYPE)
                .build();
        String jsonMandate = mandate.toString();

        Mockito.verify(appender, Mockito.atLeastOnce()).append(captor.capture());
        final List<LogEvent> logs = captor.getAllValues();
        assertEquals(1, logs.size());
        assertEquals(Mandate.SIGNATORY_WARN, logs.get(0).getMessage().getFormattedMessage());

        LOGGER.info(jsonMandate);
        Assertions.assertTrue(jsonMandate.contains(REFERENCE));
        Assertions.assertTrue(jsonMandate.contains(PAYMENT_SCHEME));
        Assertions.assertFalse(jsonMandate.contains(SIGNATORY));
        Assertions.assertTrue(jsonMandate.contains(STANDARD));
        Assertions.assertTrue(jsonMandate.contains(CREATE_SEQUENCE_TYPE));

    }


    @Test
    void testMandateWoPaymentScheme() {

        mandate = Mandate.Builder.aMandateBuilder()
                .withReference(PAYMENT_REF_1)
                .withSignatory(createDefaultSignatory())
                .withStandard(SEPA)
                .withCreateSequenceType(CREATE_SEQUENCE_TYPE)
                .build();
        String jsonMandate = mandate.toString();

        Mockito.verify(appender, Mockito.atLeastOnce()).append(captor.capture());
        final List<LogEvent> logs = captor.getAllValues();
        assertEquals(1, logs.size());
        assertEquals(Mandate.PAYMENT_SCHEME_WARN, logs.get(0).getMessage().getFormattedMessage());

        LOGGER.info(jsonMandate);
        Assertions.assertTrue(jsonMandate.contains(REFERENCE));
        Assertions.assertFalse(jsonMandate.contains(PAYMENT_SCHEME));
        Assertions.assertTrue(jsonMandate.contains(SIGNATORY));
        Assertions.assertTrue(jsonMandate.contains(STANDARD));
        Assertions.assertTrue(jsonMandate.contains(CREATE_SEQUENCE_TYPE));

    }


    @Test
    void testMandateWoCreateSequenceType() {

        mandate = Mandate.Builder.aMandateBuilder()
                .withReference(PAYMENT_REF_1)
                .withSignatory(createDefaultSignatory())
                .withStandard(SEPA)
                .withPaymentScheme(SEPA_DIRECT_DEBIT_CORE)
                .build();
        String jsonMandate = mandate.toString();

        Mockito.verify(appender, Mockito.atLeastOnce()).append(captor.capture());
        final List<LogEvent> logs = captor.getAllValues();
        assertEquals(1, logs.size());
        assertEquals(Mandate.CREATE_SEQUENCE_TYPE_WARN, logs.get(0).getMessage().getFormattedMessage());

        LOGGER.info(jsonMandate);
        Assertions.assertTrue(jsonMandate.contains(REFERENCE));
        Assertions.assertTrue(jsonMandate.contains(PAYMENT_SCHEME));
        Assertions.assertTrue(jsonMandate.contains(SIGNATORY));
        Assertions.assertTrue(jsonMandate.contains(STANDARD));
        Assertions.assertFalse(jsonMandate.contains(CREATE_SEQUENCE_TYPE));

    }
}

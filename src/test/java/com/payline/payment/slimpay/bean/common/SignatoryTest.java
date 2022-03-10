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

import static com.payline.payment.slimpay.utils.BeansUtils.createDefaultBillingAddress;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SignatoryTest {

    private Signatory signatory;

    @Mock
    private Appender appender;

    @Captor
    private ArgumentCaptor<LogEvent> captor;

    private LoggerConfig loggerConfig;

    @BeforeEach
    void setUp() {

        doReturn("MockAppender").when(appender).getName();
        lenient().doReturn(true).when(appender).isStarted();

        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        loggerConfig = config.getLoggerConfig(BillingAddress.class.getSimpleName());
        loggerConfig.addAppender(appender, Level.INFO, null);
    }

    @AfterEach
    void tearDown() {
        loggerConfig.removeAppender("MockAppender");
    }


    @Test
    public void signatoryOk(){
        signatory = Signatory.Builder.aSignatoryBuilder()
                .withfamilyName("Doe")
                .withGivenName("John")
                .withHonorificPrefix("Mr")
                .withBilingAddress(createDefaultBillingAddress())
                .withEmail("toto@emailcom")
                .withTelephone("+33725262729")
                .build();

        String jsonSignatory = signatory.toString();
        Assertions.assertTrue(jsonSignatory.contains("Doe"));
        Assertions.assertTrue(jsonSignatory.contains("John"));
        Assertions.assertTrue(jsonSignatory.contains("toto@emailcom"));
        Assertions.assertTrue(jsonSignatory.contains("+33725262729"));
        Assertions.assertTrue(jsonSignatory.contains("Mr"));
        Assertions.assertTrue(jsonSignatory.contains("billingAddress"));
    }

    @Test
    public void signatoryKO(){
        signatory = Signatory.Builder.aSignatoryBuilder()
                .withHonorificPrefix("Mr")
                .withBilingAddress(createDefaultBillingAddress())
                .withEmail("toto@emailcom")
                .withTelephone("+33725262729")
                .build();

        //test on logs
        verify(appender, times(2)).append(captor.capture());
    }

    @Test
    public void signatoryWrongTelephoneFormat(){
        signatory = Signatory.Builder.aSignatoryBuilder()
                .withHonorificPrefix("Mr")
                .withfamilyName("Doe")
                .withGivenName("John")
                .withBilingAddress(createDefaultBillingAddress())
                .withEmail("toto@emailcom")
                .withTelephone("000")
                .build();

        //test on logs
        verify(appender, Mockito.atLeastOnce()).append(captor.capture());
        assertEquals(Signatory.TELEPHONE_WARN, captor.getValue().getMessage().getFormattedMessage());
    }
}

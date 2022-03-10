package com.payline.payment.slimpay.bean.common;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.payline.payment.slimpay.utils.BeansUtils.createDefaultMandate;
import static com.payline.payment.slimpay.utils.BeansUtils.createDefaultPayin;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class SlimPayOrderItemTest {

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
    public void SlimpayOrderItemOK(){
        SlimPayOrderItem.Builder.aSlimPayOrderItemBuilder()
                .withType("payment")
                .withPayin(createDefaultPayin("reference payment"))
                .build();

        verify(appender, never()).append(any());
    }

    @Test
    public void SlimpayOrderItemMandateOK(){
        SlimPayOrderItem.Builder.aSlimPayOrderItemBuilder()
                .withType("signMandate")
                .withMandate(createDefaultMandate("reference mandate"))
                .build();
        verify(appender, never()).append(captor.capture());
    }

    @Test
    public void SlimpayOrderItemMandateWithoutReference(){
        SlimPayOrderItem.Builder.aSlimPayOrderItemBuilder()
                .withType("signMandate")
                .build();

        verify(appender, Mockito.atLeastOnce()).append(captor.capture());
        assertEquals(SlimPayOrderItem.MANDATE_WARN, captor.getValue().getMessage().getFormattedMessage());
    }

    @Test
    public void SlimpayOrderItemMandateWithoutType(){
        SlimPayOrderItem.Builder.aSlimPayOrderItemBuilder()
                .withMandate(createDefaultMandate("reference mandate"))
                .build();

        verify(appender, Mockito.atLeastOnce()).append(captor.capture());
        assertEquals(SlimPayOrderItem.TYPE_WARN, captor.getValue().getMessage().getFormattedMessage());
    }
}

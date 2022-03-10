package com.payline.payment.slimpay.bean.request;

import com.payline.payment.slimpay.bean.common.BillingAddress;
import com.payline.payment.slimpay.bean.common.Creditor;
import com.payline.payment.slimpay.bean.common.SlimPayOrderItem;
import com.payline.payment.slimpay.bean.common.Subscriber;
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

import static com.payline.payment.slimpay.utils.BeansUtils.createDefaultOrderItemMandate;
import static com.payline.payment.slimpay.utils.BeansUtils.createDefaultOrderItemPayment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SlimpayOrderRequestTest {

    private SlimpayOrderRequest orderRequest;

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
    public void slimpayOrderRequestOK() {

        orderRequest = SlimpayOrderRequest.Builder.aSlimPayOrderRequestBuilder()
                .withReference("ORDER-123")
                .withCreditor(new Creditor("creditor1"))
                .withSubscriber(new Subscriber("Client2"))
                .withFailureUrl("failure.url.com")
                .withSuccessUrl("success.url.com")
                .withItems(new SlimPayOrderItem[]{
                        createDefaultOrderItemMandate(),
                        createDefaultOrderItemPayment()
                })
                .withLocale("FR")
                .withPaymentScheme("SEPA.DIRECT_DEBIT.CORE")
                .build();
        String requestJson = orderRequest.toString();
        //Assert Json is well formed
        Assertions.assertTrue(requestJson.contains("reference"));
        Assertions.assertTrue(requestJson.contains("creditor"));
        Assertions.assertTrue(requestJson.contains("subscriber"));
        Assertions.assertTrue(requestJson.contains("items"));
        Assertions.assertTrue(requestJson.contains("paymentScheme"));
    }


    @Test
    public void slimpayOrderRequestEmpty() {

        orderRequest = SlimpayOrderRequest.Builder.aSlimPayOrderRequestBuilder()
                .build();

        verify(appender, Mockito.atLeastOnce()).append(captor.capture());
        assertEquals(5, captor.getAllValues().size());
    }

}

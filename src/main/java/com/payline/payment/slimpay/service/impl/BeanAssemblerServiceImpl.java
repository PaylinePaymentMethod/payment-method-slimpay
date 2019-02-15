package com.payline.payment.slimpay.service.impl;

import com.payline.payment.slimpay.bean.common.*;
import com.payline.payment.slimpay.bean.request.SlimpayOrderRequest;
import com.payline.payment.slimpay.bean.response.PaymentResponseSuccessAdditionalData;
import com.payline.payment.slimpay.exception.InvalidDataException;
import com.payline.payment.slimpay.service.BeanAssemblerService;
import com.payline.pmapi.bean.common.Amount;
import com.payline.pmapi.bean.common.Buyer;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import com.payline.pmapi.bean.payment.Environment;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.refund.request.RefundRequest;

import java.util.Locale;

import static com.payline.payment.slimpay.utils.PluginUtils.createStringAmount;
import static com.payline.payment.slimpay.utils.PluginUtils.getHonorificCode;
import static com.payline.payment.slimpay.utils.SlimpayConstants.*;

public class BeanAssemblerServiceImpl implements BeanAssemblerService {

    private enum Direction {
        IN, OUT;
    }

    private enum Type {
        PAYMENT("payment"), SIGN_MANDATE("signMandate");

        protected String key;

        Type(String key) {
            this.key = key;
        }
    }

    private static final String CREATE = "create";
    private static final String PAYOUT_SCHEME = "SEPA.CREDIT_TRANSFER";
    final String foo = "foo";
    //Type de prélèvement
    private static final String PONCTUEL = "OOFF";


    /**
     * Singleton Holder
     */
    private static class SingletonHolder {
        private static final BeanAssemblerServiceImpl INSTANCE = new BeanAssemblerServiceImpl();
    }

    /**
     * @return the singleton instance
     */
    public static BeanAssemblerServiceImpl getInstance() {
        return BeanAssemblerServiceImpl.SingletonHolder.INSTANCE;
    }

    /**
     * Create a Slimplay Payment with direction IN from a Payline PaymentRequest
     *
     * @param paymentRequest
     * @return a a new  Payment
     */

    @Override
    public Payment assemblePayin(PaymentRequest paymentRequest) throws InvalidDataException {
        if (paymentRequest == null) {
            throw new InvalidDataException("PaymentRequest is null or empty", "PaymentRequest is null");
        } else {
        return Payment.Builder.aPaymentBuilder()
                .withReference(paymentRequest.getOrder().getReference())
                .withScheme(RequestConfigServiceImpl.INSTANCE.getParameterValue(paymentRequest, FIRST_PAYMENT_SCHEME))
                .withDirection(Direction.IN.name())
                .withAction(CREATE)
                .withAmount(createStringAmount(paymentRequest.getAmount()))
                .withCurrency(getCurrencyAsString(paymentRequest.getAmount()))
                .withLabel(paymentRequest.getSoftDescriptor())
                .build();
    }
    }

    /**
     * Create a Slimplay Payment with direction OUT from a Payline PaymentRequest
     *
     * @param refundRequest
     * @return a a new  Payment
     */
    @Override
    public Payment assemblePayout(RefundRequest refundRequest) throws InvalidDataException {
        if (refundRequest == null) {
            throw new InvalidDataException("RefundRequest is null or empty", "RefundRequest is null");
        } else {
        //use mandate or Subscriber reference for payout
        PaymentResponseSuccessAdditionalData additionalData = PaymentResponseSuccessAdditionalData.fromJson(refundRequest.getTransactionAdditionalData());
        String mandateReference = additionalData.getMandateReference();
        String reference = refundRequest.getOrder() == null ? null : refundRequest.getOrder().getReference();

        return Payment.Builder.aPaymentBuilder()
                .withReference(reference)
                .withScheme(PAYOUT_SCHEME)
                .withDirection(Direction.OUT.name())
                .withAmount(createStringAmount(refundRequest.getAmount()))
                .withCurrency(getCurrencyAsString(refundRequest.getAmount()))
                .withLabel(refundRequest.getSoftDescriptor())
                .withCorrelationId(refundRequest.getPartnerTransactionId())
//                .withSubscriber(new Subscriber(refundRequest.getBuyer().getCustomerIdentifier()))
                .withCreditor(new Creditor(RequestConfigServiceImpl.INSTANCE.getParameterValue(refundRequest, CREDITOR_REFERENCE_KEY)))
                .withMandate(Mandate.Builder.aMandateBuilder()
                        .withReference(mandateReference)
                        .build())
                .build();

    }
    }

    @Override
    public SlimPayOrderItem assembleOrderItem(PaymentRequest paymentRequest) {
        return null;
    }

    /**
     * Create a SlimPayOrderItem with type signMandate and  a Mandate from a Payline PaymentRequest
     *
     * @param paymentRequest
     * @return a a new  SlimPayOrderItem
     */
    @Override
    public SlimPayOrderItem assembleOrderItemMandate(PaymentRequest paymentRequest) throws InvalidDataException {
        if (paymentRequest == null) {
            throw new InvalidDataException("PaymentRequest is null or empty", "PaymentRequest is null");
        } else {
        return SlimPayOrderItem.Builder.aSlimPayOrderItemBuilder()
                .withType(Type.SIGN_MANDATE.key)
                .withMandate(assembleMandate(paymentRequest))
                .build();

    }
    }


    /**
     * Create a SlimPayOrderItem with type payment and  a Payment (direction IN) from a Payline PaymentRequest
     *
     * @param paymentRequest
     * @return a a new  SlimPayOrderItem
     */
    @Override
    public SlimPayOrderItem assembleOrderItemPayment(PaymentRequest paymentRequest) throws InvalidDataException {
        if (paymentRequest == null) {
            throw new InvalidDataException("PaymentRequest is null or empty", "PaymentRequest is null");
        } else {
        return SlimPayOrderItem.Builder.aSlimPayOrderItemBuilder()
                .withType(Type.PAYMENT.key)
                .withPayin(assemblePayin(paymentRequest))
                .build();
    }
    }

    /**
     * Create a Mandate from a Payline PaymentRequest
     *
     * @param paymentRequest
     * @return a new  Mandate
     */
    @Override
    public Mandate assembleMandate(PaymentRequest paymentRequest) throws InvalidDataException {
        if (paymentRequest == null) {
            throw new InvalidDataException("PaymentRequest is null or empty", "PaymentRequest is null");
        } else {
        return Mandate.Builder.aMandateBuilder()
                .withReference(paymentRequest.getTransactionId())
                .withStandard(RequestConfigServiceImpl.INSTANCE.getParameterValue(paymentRequest, MANDATE_STANDARD_KEY))
                .withAction(CREATE)
                .withPaymentScheme(RequestConfigServiceImpl.INSTANCE.getParameterValue(paymentRequest, MANDATE_PAYIN_SCHEME))
                .withCreateSequenceType(PONCTUEL)
                .withSequenceType(PONCTUEL)
                .withSignatory(assembleSignatory(paymentRequest))
                .build();
    }
    }

    /**
     * Create a Signatory from a Payline PaymentRequest
     *
     * @param paymentRequest
     * @return a new  Signatory
     */
    @Override
    public Signatory assembleSignatory(PaymentRequest paymentRequest) {

        Buyer buyer = paymentRequest.getBuyer();

        if (buyer == null) {
            return null;
        }

        final Buyer.FullName fullName = buyer.getFullName();
        return Signatory.Builder.aSignatoryBuilder()
                .withfamilyName(fullName == null ? null : fullName.getFirstName())
                .withGivenName(fullName == null ? null : fullName.getLastName())
                .withHonorificPrefix(getHonorificCode(fullName == null ? null : fullName.getCivility()))
                .withBilingAddress(assembleBillingAddress(paymentRequest))
                .withEmail(buyer.getEmail())
                .withTelephone(buyer.getPhoneNumbers().get(Buyer.PhoneNumberType.CELLULAR))
                .build();
    }

    /**
     * Create a BillingAddress from a Payline PaymentRequest
     *
     * @param paymentRequest
     * @return a new  BillingAddress
     */
    public BillingAddress assembleBillingAddress(PaymentRequest paymentRequest) {

        Buyer.Address address = paymentRequest.getBuyer().getAddressForType(Buyer.AddressType.BILLING);

        if (address == null) {
            return null;
        }

        return BillingAddress.Builder.aBillingAddressBuilder()
                .withStreet1(address.getStreet1())
                .withStreet2(address.getStreet2())
                .withCity(address.getCity())
                .withCountry(address.getCountry())
                .withPostalCode(address.getZipCode())
                .build();
    }

    /**
     * Create a SlimpayOrderRequest from a Payline PaymentRequest
     *
     * @param paymentRequest
     * @return a new  SlimpayOrderRequest
     */
    public SlimpayOrderRequest assembleSlimPayOrderRequest(PaymentRequest paymentRequest) throws InvalidDataException {
        if (paymentRequest == null) {
            throw new InvalidDataException("PaymentRequest is null or empty", "PaymentRequest is null");
        } else {
            Environment environment = paymentRequest.getEnvironment();
            Locale locale = paymentRequest.getLocale();
            Buyer buyer = paymentRequest.getBuyer();
        return SlimpayOrderRequest.Builder.aSlimPayOrderRequestBuilder()
                .withReference(paymentRequest.getTransactionId())
                .withSubscriber(new Subscriber(buyer == null ? null : buyer.getCustomerIdentifier()))
                .withCreditor(new Creditor(RequestConfigServiceImpl.INSTANCE.getParameterValue(paymentRequest, CREDITOR_REFERENCE_KEY)))
                .withSuccessUrl(environment == null ? null : environment.getRedirectionReturnURL())
                .withFailureUrl(environment == null ? null : environment.getRedirectionReturnURL())
                .withCancelUrl(environment == null ? null : environment.getRedirectionCancelURL())
                .withLocale(locale == null ? null : locale.getCountry())
                .withStarted(true)
                //send by mail user approval link
                .withSendUserApproval(true)
                .withItems(new SlimPayOrderItem[]{
                        assembleOrderItemMandate(paymentRequest),
                        assembleOrderItemPayment(paymentRequest)
                })
                .build();
    }
    }

    /**
     * Request used to test Simplay HTTP call
     *
     * @param request ContractParametersCheckRequest
     * @return SlimpayOrderRequest
     * @throws InvalidDataException
     */
    public SlimpayOrderRequest assembleSlimPayOrderRequest(ContractParametersCheckRequest request) throws InvalidDataException {


        Environment environment = request.getEnvironment();
        Locale locale = request.getLocale();

        Mandate mandate = createTestMandate(request);

        return SlimpayOrderRequest.Builder.aSlimPayOrderRequestBuilder()
                .withSubscriber(new Subscriber(foo))
                .withCreditor(new Creditor(request.getContractConfiguration().getProperty(CREDITOR_REFERENCE_KEY).getValue()))
                .withSuccessUrl(environment == null ? null : environment.getRedirectionReturnURL())
                .withFailureUrl(environment == null ? null : environment.getRedirectionReturnURL())
                .withCancelUrl(environment == null ? null : environment.getRedirectionCancelURL())
                .withLocale(locale == null ? null : locale.getCountry())
                .withStarted(true)
                .withItems(new SlimPayOrderItem[]{
                        SlimPayOrderItem.Builder.aSlimPayOrderItemBuilder()
                                .withType(Type.SIGN_MANDATE.key)
                                .withMandate(mandate)
                                .build()
                })
                .build();
    }

    /**
     * Fake mandate creation for Slimpay call test
     *
     * @param request ContractParametersCheckRequest
     * @return mandate
     * @throws InvalidDataException
     */
    private Mandate createTestMandate(ContractParametersCheckRequest request) throws InvalidDataException {

        final String street = "street";
        final String prefix = "Mr";
        final String country = "US";
        final String phone = "+33601020304";
        final String mail = "foo@bar.com";
        final String reference = "123456789";

        BillingAddress address = BillingAddress.Builder.aBillingAddressBuilder()
                .withStreet1(street)
                .withStreet2(street)
                .withCity(foo)
                .withCountry(country)
                .withPostalCode(foo)
                .build();

        Signatory signatory = Signatory.Builder.aSignatoryBuilder()
                .withfamilyName(foo)
                .withGivenName(foo)
                .withHonorificPrefix(prefix)
                .withBilingAddress(address)
                .withEmail(mail)
                .withTelephone(phone)
                .build();

        return Mandate.Builder.aMandateBuilder()
                .withReference(reference)
                .withStandard(RequestConfigServiceImpl.INSTANCE.getParameterValue(request, MANDATE_STANDARD_KEY))
                .withAction(CREATE)
                .withPaymentScheme(RequestConfigServiceImpl.INSTANCE.getParameterValue(request, MANDATE_PAYIN_SCHEME))
                .withSignatory(signatory)
                .build();
    }

    private String getCurrencyAsString(Amount amount) {
        if (amount == null || amount.getCurrency() == null) {
            return null;
        }

        return amount.getCurrency().toString();
    }

}


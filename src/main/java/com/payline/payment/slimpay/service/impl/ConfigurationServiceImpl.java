package com.payline.payment.slimpay.service.impl;

import com.payline.payment.slimpay.utils.PluginUtils;
import com.payline.payment.slimpay.utils.i18n.I18nService;
import com.payline.payment.slimpay.utils.properties.constants.ConfigurationConstants;
import com.payline.payment.slimpay.utils.properties.service.ReleasePropertiesEnum;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.configuration.ReleaseInformation;
import com.payline.pmapi.bean.configuration.parameter.AbstractParameter;
import com.payline.pmapi.bean.configuration.parameter.impl.InputParameter;
import com.payline.pmapi.bean.configuration.parameter.impl.ListBoxParameter;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.service.ConfigurationService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.payline.payment.slimpay.utils.SlimpayConstants.*;

public class ConfigurationServiceImpl implements ConfigurationService {


    private I18nService i18n = I18nService.getInstance();

    @Override
    public List<AbstractParameter> getParameters(Locale locale) {
        List<AbstractParameter> parameters = new ArrayList<>();

        // creditorReference
        final InputParameter creditorReference = new InputParameter();
        creditorReference.setKey(CREDITOR_REFERENCE_KEY);
        creditorReference.setLabel(this.i18n.getMessage(CREDITOR_REFERENCE_KEY_LABEL, locale));
        creditorReference.setDescription(this.i18n.getMessage(CREDITOR_REFERENCE_KEY_LABEL, locale));
        creditorReference.setRequired(true);
        parameters.add(creditorReference);

        // paymentProcessor
        final ListBoxParameter paymentProcessor = new ListBoxParameter();
        paymentProcessor.setKey(PAYMENT_PROCESSOR);
        paymentProcessor.setLabel(this.i18n.getMessage(PAYMENT_PROCESSOR_LABEL, locale));
        paymentProcessor.setDescription(this.i18n.getMessage(PAYMENT_PROCESSOR_DESCRIPTION, locale));
        paymentProcessor.setRequired(true);
        final LinkedHashMap<String, String> processors = new LinkedHashMap<>();
        processors.put("Slimpay", "Slimpay");
        paymentProcessor.setList(processors);
        parameters.add(paymentProcessor);

        //paymentScheme
        final ListBoxParameter paymentScheme = new ListBoxParameter();
        paymentScheme.setKey(FIRST_PAYMENT_SCHEME);
        paymentScheme.setLabel(this.i18n.getMessage(FIRST_PAYMENT_SCHEME_LABEL, locale));
        paymentScheme.setDescription(this.i18n.getMessage(FIRST_PAYMENT_SCHEME_DESCRIPTION, locale));
        paymentScheme.setRequired(true);
        final LinkedHashMap<String, String> paymentSchemes = new LinkedHashMap<>();
        paymentSchemes.put("SEPA.DIRECT_DEBIT.CORE", "SEPA.DIRECT_DEBIT.CORE");
        paymentScheme.setList(paymentSchemes);
        parameters.add(paymentScheme);

        // mandateScheme
        final ListBoxParameter mandateScheme = new ListBoxParameter();
        mandateScheme.setKey(MANDATE_PAYIN_SCHEME);
        mandateScheme.setLabel(this.i18n.getMessage(MANDATE_PAYIN_SCHEME_LABEL, locale));
        mandateScheme.setDescription(this.i18n.getMessage(MANDATE_PAYIN_SCHEME_DESCRIPTION, locale));
        mandateScheme.setRequired(true);
        final LinkedHashMap<String, String> mandateSchemes = new LinkedHashMap<>();
        mandateSchemes.put("SEPA.DIRECT_DEBIT.CORE", "SEPA.DIRECT_DEBIT.CORE");
        mandateScheme.setList(mandateSchemes);
        parameters.add(mandateScheme);

        // signatureApproval method
        final ListBoxParameter signatureApproval = new ListBoxParameter();
        signatureApproval.setKey(SIGNATURE_APPROVAL_METHOD);
        signatureApproval.setLabel(this.i18n.getMessage(SIGNATURE_APPROVAL_METHOD_LABEL, locale));
        signatureApproval.setRequired(true);
        final LinkedHashMap<String, String> signatures = new LinkedHashMap<>();
        signatures.put("otp", "otp");
        signatureApproval.setList(signatures);
        parameters.add(signatureApproval);

        return parameters;

    }

    @Override
    public Map<String, String> check(ContractParametersCheckRequest contractParametersCheckRequest) {

        Locale locale = contractParametersCheckRequest.getLocale();
        final Map<String, String> errors = new HashMap<>();
        PartnerConfiguration partnerConfiguration = contractParametersCheckRequest.getPartnerConfiguration();
        ContractConfiguration contractConfiguration = contractParametersCheckRequest.getContractConfiguration();
        //todo recuperer dans accountInfo ou contractProperties
        final Map<String, String> accountInfo = contractParametersCheckRequest.getAccountInfo();


        //
        RequestConfigServiceImpl.INSTANCE.getParameterValue(contractParametersCheckRequest, "KEY");

        // CreditoReference
        final String creditoReference = accountInfo.get(CREDITOR_REFERENCE_KEY);
        if (PluginUtils.isEmpty(creditoReference)) {
            errors.put(CREDITOR_REFERENCE_KEY, this.i18n.getMessage(CREDITOR_REFERENCE_KEY_MESSAGE_ERROR, locale));
        }

        // paymentScheme
        final String paymentScheme = accountInfo.get(FIRST_PAYMENT_SCHEME);
        if (PluginUtils.isEmpty(paymentScheme)) {
            errors.put(FIRST_PAYMENT_SCHEME, this.i18n.getMessage(FIRST_PAYMENT_MESSAGE_ERROR, locale));
        }

        // mandateScheme
        final String mandateScheme = accountInfo.get(MANDATE_PAYIN_SCHEME);
        if (PluginUtils.isEmpty(mandateScheme)) {
            errors.put(MANDATE_PAYIN_SCHEME, this.i18n.getMessage(MANDATE_PAYIN_MESSAGE_ERROR, locale));
        }
        // signatureApproval
        final String signatureApproval = accountInfo.get(SIGNATURE_APPROVAL_METHOD);
        if (PluginUtils.isEmpty(signatureApproval)) {
            errors.put(SIGNATURE_APPROVAL_METHOD, this.i18n.getMessage(SIGNATURE_APPROVAL_METHOD_MESSAGE_ERROR, locale));
        }

        // paymentProcessor
        final String paymentProcessor = accountInfo.get(PAYMENT_PROCESSOR);
        if (PluginUtils.isEmpty(paymentProcessor)) {
            errors.put(PAYMENT_PROCESSOR, this.i18n.getMessage(PAYMENT_PROCESSOR_MESSAGE_ERROR, locale));
        }


        //partnerConfig

        // appKey
        String appName = contractParametersCheckRequest.getPartnerConfiguration().getProperty(APP_KEY);
        if (PluginUtils.isEmpty(appName)) {
            errors.put(APP_KEY, this.i18n.getMessage(APP_KEY_MESSAGE_ERROR, locale));
        }

        // appSecret
        String appSecret = contractParametersCheckRequest.getPartnerConfiguration().getProperty(APP_SECRET);
        if (PluginUtils.isEmpty(appSecret)) {
            errors.put(APP_SECRET, this.i18n.getMessage(APP_SECRET_MESSAGE_ERROR, locale));
        }

        return errors;
    }

    @Override
    public ReleaseInformation getReleaseInformation() {

        LocalDate date = LocalDate.parse(ReleasePropertiesEnum.INSTANCE.get(ConfigurationConstants.RELEASE_DATE),
                DateTimeFormatter.ofPattern(ConfigurationConstants.RELEASE_DATE_FORMAT));
        return ReleaseInformation.ReleaseBuilder.aRelease()
                .withDate(date)
                .withVersion(ReleasePropertiesEnum.INSTANCE.get(ConfigurationConstants.RELEASE_VERSION))
                .build();
    }

    @Override
    public String getName(Locale locale) {

        return this.i18n.getMessage(ConfigurationConstants.PAYMENT_METHOD_NAME, locale);
    }
}
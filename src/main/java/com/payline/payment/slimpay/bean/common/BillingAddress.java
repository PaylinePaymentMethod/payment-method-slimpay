package com.payline.payment.slimpay.bean.common;

import com.payline.payment.slimpay.utils.Required;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BillingAddress extends SlimpayBean {

    private static final Logger LOGGER = LogManager.getLogger(Signatory.class);

    @Required
    private String street1;
    private String street2;
    @Required
    private String city;
    @Required
    private String postalCode;
    @Required
    private String country;

    public String getStreet1() {
        return street1;
    }

    public String getStreet2() {
        return street2;
    }

    public String getCity() {
        return city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCountry() {
        return country;
    }

    public BillingAddress(BillingAddress.Builder builder) {
        this.street1 = builder.street1;
        this.street2 = builder.street2;
        this.city = builder.city;
        this.postalCode = builder.postalCode;
        this.country = builder.country;
    }

    public static class Builder {
        private String street1;
        private String street2;
        private String city;
        private String postalCode;
        private String country;

        public static BillingAddress.Builder aBillingAddressBuilder() {
            return new BillingAddress.Builder();
        }

        public BillingAddress.Builder withStreet1(String street1) {
            this.street1 = street1;
            return this;
        }

        public BillingAddress.Builder withStreet2(String street2) {
            this.street2 = street2;
            return this;
        }

        public BillingAddress.Builder withCity(String city) {
            this.city = city;
            return this;
        }

        public BillingAddress.Builder withPostalCode(String postalCode) {
            this.postalCode = postalCode;
            return this;
        }

        public BillingAddress.Builder withCountry(String country) {
            this.country = country;
            return this;
        }

        private BillingAddress.Builder verifyIntegrity() {

            //to do logger les champs manquants obligatoire ??
            if (this.street1 == null) {
                LOGGER.warn("BillingAddress must have a familyName when built");
            }
            if (this.city == null) {
                LOGGER.warn("BillingAddress must have a city when built");
            }
            if (this.postalCode == null) {
                LOGGER.warn("BillingAddress must have a postalCode when built");
            }
            if (this.country == null) {
                LOGGER.warn("BillingAddress must have a country when built");
            }
            return this;
        }

        public BillingAddress build() {
            return new BillingAddress(this.verifyIntegrity());
        }

    }
}

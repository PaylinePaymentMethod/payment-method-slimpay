package com.payline.payment.slimpay.utils;


import com.payline.pmapi.bean.common.Amount;

import java.math.BigInteger;
import java.util.Currency;

import static com.payline.payment.slimpay.utils.SlimpayConstants.ERROR_MAX_LENGTH;

public class PluginUtils {


    private PluginUtils() {
        // ras.
    }

    public static boolean isEmpty(String s) {

        return s == null || s.isEmpty();
    }

    /**
     * Return a string which was converted from cents to euro
     *
     * @param amount
     * @return
     */
    public static String createStringAmount(Amount amount) {
        if (amount == null || amount.getAmountInSmallestUnit() == null || amount.getCurrency() == null) {
            return null;
        }
        Currency currency = amount.getCurrency();
        BigInteger amountInSmallestUnit = amount.getAmountInSmallestUnit();

        //récupérer le nombre de digits dans currency
        int nbDigits = currency.getDefaultFractionDigits();

        StringBuilder sb = new StringBuilder();
        sb.append(amountInSmallestUnit);

        for (int i = sb.length(); i < 3; i++) {
            sb.insert(0, "0");
        }

        sb.insert(sb.length() - nbDigits, ".");
        return sb.toString();
    }

    /**
     * Return a Float which was converted from cents to euro
     *
     * @param amount
     * @return
     */
    public static Float createFloatAmount(Amount amount) {
        if (amount == null) return null;
        return Float.parseFloat(createStringAmount(amount));
    }

    public static String truncate(String value, int length) {
        if (value != null && value.length() > length) {
            value = value.substring(0, length);
        }
        return value;
    }

    public static String getHonorificCode(String civility) {

        if (civility == null) {
            return null;
        }

        switch (civility.toLowerCase()) {
            //MR
            case "4":
            case "5":
                return "Mr";
            //MME
            case "1":
            case "2":
            case "6":
                return "Mrs";
            //MLLE
            case "3":
                return "Miss";
            default:
                return "Mr";
        }
    }

    /**
     * Truncate a String to max length define in SlimpayConstants class
     * @param error
     * @return
     */
    public static String truncateError(String error) {
        return PluginUtils.truncate(error, ERROR_MAX_LENGTH);
    }


}
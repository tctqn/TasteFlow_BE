package com.startup.tasteflowbe.utils;

import org.apache.commons.codec.digest.HmacUtils;
import org.json.JSONObject;

import java.util.*;

public class PayOSWebhookUtil {

    public static boolean isValidData(JSONObject dataObject, String transactionSignature, String checksumKey) {
        try {
            Iterator<String> sortedIt = sortedIterator(dataObject.keys(), String::compareTo);

            StringBuilder transactionStr = new StringBuilder();
            while (sortedIt.hasNext()) {
                String key = sortedIt.next();
                String value = dataObject.optString(key, "");
                transactionStr.append(key).append("=").append(value);
                if (sortedIt.hasNext()) {
                    transactionStr.append("&");
                }
            }

            String signature = new HmacUtils("HmacSHA256", checksumKey).hmacHex(transactionStr.toString());
            return signature.equalsIgnoreCase(transactionSignature);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static Iterator<String> sortedIterator(Iterator<?> it, Comparator<String> comparator) {
        List<String> list = new ArrayList<String>();
        while (it.hasNext()) {
            list.add((String) it.next());
        }

        Collections.sort(list, comparator);
        return list.iterator();
    }
}

package io.keepcoding.pickandgol.manager.net;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;


/**
 * This class represents a set of parameters passed to a Network request
 */
public class RequestParams {

    private Map<String,String> params;

    public RequestParams() {
        this.params = new HashMap<>();
    }

    public RequestParams(Map<String,String> params) {
        this.params = params;
    }

    public int size() {
        return params.size();
    }

    public Map<String,String> getParams() {
        return params;
    }

    public RequestParams addParam(String key, String value) {

        String existingValue = params.get(key);

        if (existingValue == null)
            params.put(key,value);

        // If this is a multi value parameter, it will be sent as: param = 'value1,value2,value3...'
        else
            params.put(key, existingValue +","+ value);


        return this;
    }

    public RequestParams addParams(Map<String,String> newParams) {
        params.putAll(newParams);
        return this;
    }

    public RequestParams urlEncodeParams() {

        Map<String,String> encodedParams = new HashMap<>();

        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {

                String key = URLEncoder.encode(entry.getKey(), "UTF-8");
                String value = URLEncoder.encode(entry.getValue(), "UTF-8");
                encodedParams.put(key,value);
            }

            params = encodedParams;
        }
        catch (Exception e) {
        }

        return this;
    }

    public String addParamsToUrl(String url) {

        StringBuilder strBuilder;

        if (params.size() == 0)
            return url;

        strBuilder = new StringBuilder(url);
        strBuilder.append("?");

        for (Map.Entry<String,String> entry : urlEncodeParams().getParams().entrySet()) {

            String key = entry.getKey();
            String value = entry.getValue();
            strBuilder.append(key +"="+ value +"&");
        }

        strBuilder.setLength(strBuilder.length() - 1);    // to remove the last '&'
        return strBuilder.toString();
    }

    public String debugString() {

        if (params.size() == 0)
            return "< NO PARAMS >\n";

        StringBuilder strBuilder = new StringBuilder("\n");

        for (Map.Entry<String, String> entry : params.entrySet()) {

            String key = entry.getKey();
            String value = entry.getValue();
            strBuilder.append(key +": "+ value +"\n");
        }

        return strBuilder.toString();
    }
}

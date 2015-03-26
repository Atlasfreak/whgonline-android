package com.gbhrdt.whgonline;

import android.app.Application;

import com.loopj.android.http.*;

import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.protocol.HttpContext;

/**
 * Created by Marius on 24.09.13.
 */
public class VplanApiClient extends Application {
    private static final String BASE_URL = "http://vplan.whgonline.de/";
    private static String group;

    public static String getGroup() {
        return group;
    }

    public static void setGroup(String group) {
        VplanApiClient.group = group;
    }

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

    public static CookieStore getCookieStore() {


        HttpContext httpContext = client.getHttpContext();
        CookieStore cookieStore = (CookieStore) httpContext.getAttribute(ClientContext.COOKIE_STORE);
        return cookieStore;
    }

}
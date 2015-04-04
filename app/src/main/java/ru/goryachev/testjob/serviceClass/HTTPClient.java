package ru.goryachev.testjob.serviceClass;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import java.util.HashMap;
import java.util.Map;

/**
 * Серовис будет выполнять HTTP GET, POST запросы
 */
public class HTTPClient {
    private static HTTPClient httpClient;
    private final static int PORT = 8181;
    private HTTPClient() {

    }

    synchronized public static HTTPClient getInstance() {
        if (httpClient == null) {
            httpClient = new HTTPClient();
        }
        return httpClient;
    }

    //POST запрос c параметрами
    public void post(String url, Map<String, String> params, AsyncHttpResponseHandler handler) {
        new AsyncHttpClient(PORT).post(url, new RequestParams(params), handler);
    }

    //POST запрос без параметров
    public void post(String url, AsyncHttpResponseHandler handler) {
        new AsyncHttpClient(PORT).post(url, handler);
    }
    //GET запрос с параметрами
    public void get(String url, Map<String, String> params, AsyncHttpResponseHandler handler){
        new AsyncHttpClient(PORT).get(url, new RequestParams(params), handler);
    }

    //Перегон Header[] в Map<String, String>
    public static Map<String, String> convertHeaders(Header[] headers) {
        Map<String, String> headersMap = new HashMap<>();
        if (headers != null) {
            for (Header h : headers) {
                headersMap.put(h.getName(), h.getValue());
            }
        }

        return headersMap;
    }
}

package br.com.domain.ports;

import java.net.http.HttpRequest;

public interface HttpclientService {

    void createClient();
    HttpRequest createRequest(String uri, String method);
    HttpRequest buildGetRequest(String uri);
}

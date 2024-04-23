package br.com.controllers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpclientController {

    private final String uri;
    private HttpClient client;
    private HttpRequest request;

    public HttpclientController(String uri){
        this.uri = uri.formatted(System.getenv("API_KEY"));
        createClient();
    }

    public void createClient(){
        this.client = HttpClient.newHttpClient();
    }

    public void createExchangeRequest(){
        try {
            this.request = HttpRequest.newBuilder(new URI(uri))
                    .GET()
                    .build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public HttpResponse<String> requestExchange(){
        try {
            HttpResponse<String>  response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

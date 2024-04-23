package br.com.controllers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpclientController {

    private static final Logger logger = Logger.getLogger(HttpclientController.class.getName());
    private final String uriExchangeList;
    private String uriPaixExchange;
    private HttpClient client;

    public HttpclientController(String uriExchangeList, String uriPaixExchange){
        String api_key = System.getenv("API_KEY");

        this.uriExchangeList = uriExchangeList.formatted(api_key);
        this.uriPaixExchange= uriPaixExchange.formatted(api_key, "%s", "%s");

        createClient();
    }

    public void createClient(){
        this.client = HttpClient.newHttpClient();
    }

    private  HttpRequest createRequest(String uri){
        HttpRequest request = null;
        try {
            request = HttpRequest.newBuilder(new URI(uri))
                    .GET()
                    .build();

        } catch (URISyntaxException e) {
            logger.log(Level.WARNING, "Falha na criação do corpo de requisição");
            throw new RuntimeException(e);
        }

        return request;
    }

    public HttpResponse<String> requestExchangeList(){

        HttpResponse<String>  response = null;

        try {
            HttpRequest request = createRequest(uriExchangeList);

            if (request != null) {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
            }

        } catch (IOException | InterruptedException e) {
            logger.log(Level.WARNING, "Falha no processo de envio da requisição");
            throw new RuntimeException(e);
        }

        return response;
    }

    public HttpResponse<String> requestPairConversion(String currency1, String currency2)
    {
        HttpResponse<String> response = null;
        String uriPairConversion = uriPaixExchange.formatted(currency1, currency2);
        HttpRequest request = createRequest(uriPairConversion);
        try {
            if (request != null){
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return response;
    }
}

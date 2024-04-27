package br.com.adapters.out;

import br.com.domain.ports.HttpclientService;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpclientController implements HttpclientService {

    private final String uriExchangeList;
    private final String uriPairExchange;
    private HttpClient client;

    public HttpclientController(String uriExchangeList, String uriPairExchange){
        String api_key = System.getenv("API_KEY");

        this.uriExchangeList = uriExchangeList.formatted(api_key);
        this.uriPairExchange = uriPairExchange.formatted(api_key, "%s", "%s");

        createClient();
    }

    @Override
    public void createClient(){
        this.client = HttpClient.newHttpClient();
    }

    @Override
    public HttpRequest createRequest(String uri, String method) {
        HttpRequest request = null;

        switch (method) {
            case "GET":
                request = buildGetRequest(uri);
                break;
            case "POST":
                // Não há implementação pois conectamos com API de terceiros.
                break;
            case "PUT":
                // Não há implementação pois conectamos com API de terceiros.
                break;
            case "DELETE":
                // Não há implementação pois conectamos com API de terceiros.
                break;
        }

        return request;
    }

    public HttpResponse<String> requestExchangeList(){

        HttpResponse<String>  response = null;

        try {
            HttpRequest request = createRequest(uriExchangeList, "GET");

            if (request != null) {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return response;
    }

    public HttpResponse<String> requestPairConversion(String currency1, String currency2)
    {
        HttpResponse<String> response = null;
        String uriPairConversion = uriPairExchange.formatted(currency1, currency2);
        HttpRequest request = createRequest(uriPairConversion, "GET");
        try {
            if (request != null){
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return response;
    }

    @Override
    public HttpRequest buildGetRequest(String uri) throws RuntimeException {
        try {
            return HttpRequest.newBuilder(new URI(uri)).GET() .build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isValidResponse(HttpResponse<String> response){
        return (response != null && response.statusCode() == 200);
    }
}

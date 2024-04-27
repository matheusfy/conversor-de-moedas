package br.com.usecase;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class JsonHandler {
    Gson gson;
    public JsonHandler()
    {
        this.gson = new Gson();
    }

    public JsonElement parseResponseBody(HttpResponse<String> response){


        String bodyAsString  = gson.toJson(response.body());

        String parsedBodyString = JsonParser.parseString(bodyAsString).getAsString();
        JsonElement parsedBody = JsonParser.parseString(parsedBodyString);
        return parsedBody;
    }

    public JsonObject getConversionRateListAsJson(JsonElement parsedBody){
        JsonObject jsonBody = parsedBody.getAsJsonObject().getAsJsonObject("conversion_rates");
        return jsonBody;
    }


    public Double getConvertionRateAsDouble(JsonElement parsedBody, String param){
        JsonElement  conversionRateJson = parsedBody.getAsJsonObject().get(param);
        Double conversionRate = conversionRateJson.getAsDouble();
        System.out.println("taxa de conversão obtida: " + conversionRate.toString());
        return conversionRate;
    }

    public Map<String, Double> responseToHash(HttpResponse<String> response)
    {
        JsonObject currencyListJson =  getConversionRateListAsJson(parseResponseBody(response));
        return mapCurrencyToHash(currencyListJson);
    }

    private Map<String, Double> mapCurrencyToHash(JsonObject currencyJsonObject){

        Map<String, Double> hshCurrency = new HashMap<>();
        Set<Map.Entry<String, JsonElement>> entries = currencyJsonObject.entrySet();

        for (Map.Entry<String, JsonElement> entry: entries)
        {
            String currency                = entry.getKey();
            double conversionRate = entry.getValue().getAsDouble();

            hshCurrency.put(currency, conversionRate);
        }
        return  hshCurrency;
    }
}

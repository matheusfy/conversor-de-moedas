package br.com.usecase;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.http.HttpResponse;

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

    public JsonObject getConversionRateAsJson(JsonElement parsedBody){
        JsonObject jsonBody = parsedBody.getAsJsonObject().getAsJsonObject("conversion_rates");
        return jsonBody;
    }


    public Double getConvertionRateAsDouble(JsonElement parsedBody, String param){


        JsonElement  conversionRateJson = parsedBody.getAsJsonObject().get(param);
        Double conversionRate = conversionRateJson.getAsDouble();
        System.out.println("taxa de conversão obtida: " + conversionRate.toString());
        return conversionRate;
    }
}

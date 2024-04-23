package br.com.application;

import br.com.controllers.HttpclientController;
import br.com.models.ConvertionRec;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.*;

public class Conversor {

    private HttpclientController __exchangeController;
    private Map<String, Double> __hshCurrency;
    public List<ConvertionRec> lstConvertionHistory;

    public Conversor() {
        this.__exchangeController = new HttpclientController(System.getenv("ExchangeUri"));
        this.__hshCurrency             = null;
        this.lstConvertionHistory = new ArrayList<>();
    }

    public void init() {
        try {

            this.__hshCurrency = getExchange();
            if (this.__hshCurrency == null) {
                // Logar que houve problema em obter a resposta da API
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Falha na inicialização");
            throw new RuntimeException(e);
        }
    }

    private boolean isValidCurrency(String key) {
        return __hshCurrency.containsKey(key);
    }

    private  Map<String, Double> getExchange() throws IOException, InterruptedException {

        Map<String, Double> result = null;

        __exchangeController.createExchangeRequest();
        HttpResponse<String> response = __exchangeController.requestExchange();

        if (response != null) {
            JsonObject currencyListJson = responseToJson(response);
            result = mapCurrencyToHash(currencyListJson);
        }

        return result;
    }

    private Map<String, Double> mapCurrencyToHash(JsonObject currencyJsonObject){

        Map<String, Double> hshCurrency = new HashMap<>();
        Set<Map.Entry<String, JsonElement>> entries = currencyJsonObject.entrySet();

        for (Map.Entry<String, JsonElement> entry: entries)
        {
            String currency = entry.getKey();
            double conversionRate = entry.getValue().getAsDouble();
            hshCurrency.put(currency, conversionRate);
        }
        return  hshCurrency;
    }

    private Double calculateConversion(String originCurrency, String destinyCurrency, Double value){

        Double value1 = __hshCurrency.get(originCurrency);
        Double value2 = __hshCurrency.get(destinyCurrency);
        Double calculatedValue = (value2/value1) * value;

        return calculatedValue;
    }

    private JsonObject responseToJson(HttpResponse<String> response){

//        TODO: Criar uma classe para manipular json
        Gson gson = new Gson();
        String bodyAsString  = gson.toJson(response.body()); // Body sem formatação de Json

        String parsedBodyString = JsonParser.parseString(bodyAsString).getAsString();
        JsonElement parsedBody = JsonParser.parseString(parsedBodyString);
        JsonObject jsonBody = parsedBody.getAsJsonObject().getAsJsonObject("conversion_rates"); // Transforming String que foi formatted no format Json para um object Json de fact

        return jsonBody;
    }

    public String convertCurrency(String originCurrency, String destinyCurrency, Double value){

        try {
            if (isValidCurrency(originCurrency) && isValidCurrency(destinyCurrency)) {
                if (value >= 0) {
                    System.out.println("\nConvertendo de %s para %s.".formatted(originCurrency, destinyCurrency));

                    var calculatedValue = calculateConversion(originCurrency, destinyCurrency, value);

                    System.out.println("Valor calculado de %s/%s = %f".formatted(originCurrency, destinyCurrency, calculatedValue));

                    lstConvertionHistory.add(new ConvertionRec(originCurrency, destinyCurrency, value, calculatedValue));
                }
                return "Não é possivel realizar conversão com valor negativa";
            }
        } catch (Exception e ){
            System.out.println("houve um erro");
        }

        return "Moeda inválida";
    }

    public String getConvertionHistory() {
        String convertionHistory = "Historico de conversão: \n";
        if(lstConvertionHistory.isEmpty())
        {
            return "Não há conversão no histórico";
        } else {
            for (ConvertionRec item: lstConvertionHistory )
            {
                convertionHistory = convertionHistory + item.toString() + "\n";
            }
            return convertionHistory;
        }

    }

    public void  showCurrencyList(){

        for(Map.Entry<String, Double> currency: __hshCurrency.entrySet()){
            System.out.println("currency: "+ currency.getKey() + " rate: "+ currency.getValue());
        }
    }
}

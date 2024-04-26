package br.com.usecase;

import br.com.adapters.out.Connectiondb;
import br.com.adapters.out.HttpclientController;
import br.com.domain.model.ConvertionRec;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;


public class ConversorUseCase {

    private final HttpclientController exchangeController;
    private final JsonHandler jsonHandler;
    private final Connectiondb dbManager;

    private          Map<String, Double> hshCurrency;

    public            List<ConvertionRec> lstConvertionHistory;




    public ConversorUseCase() {
        this.exchangeController   = new HttpclientController(System.getenv("ExchangeListUri"), System.getenv("PairConversionUri"));
        this.hshCurrency                = null;
        this.lstConvertionHistory = new ArrayList<>();
        this.jsonHandler                 = new JsonHandler();
        this.dbManager                   = new Connectiondb();
    }

    public void init() {
        try {
            this.hshCurrency = getExchange();
            dbManager.initConnection();
            dbManager.loadConversionHistory(lstConvertionHistory);

        } catch (IOException | InterruptedException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isValidCurrency(String key) {
        return hshCurrency.containsKey(key);
    }

    private  Map<String, Double> getExchange() throws IOException, InterruptedException {

        Map<String, Double> result = null;

        HttpResponse<String> response = exchangeController.requestExchangeList();

        if (response != null) {

            JsonObject currencyListJson =   jsonHandler.getConversionRateAsJson(jsonHandler.parseResponseBody(response));
            result = mapCurrencyToHash(currencyListJson);
        }

        return result;
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

    private Double calculateExchangeRate(String c1, String c2){
        return hshCurrency.get(c2)/ hshCurrency.get(c1);
    }

    private Double calculateConversion(Double value, Double conversionRate){
        return value * conversionRate;
    }

    public void convertCurrency(String sourceCurrency, String targetCurrency, Double value){

        try {
            if (isValidCurrency(sourceCurrency) && isValidCurrency(targetCurrency)) {

                if (value >= 0) {

                    System.out.printf("Convertendo de %s para %s.%n", sourceCurrency, targetCurrency);


                    Double exchangeRate = getExchangeRate(sourceCurrency, targetCurrency);
                    Double calculatedValue = calculateConversion(value, exchangeRate);

                    ConvertionRec convertionRec = new ConvertionRec(
                                                                                            sourceCurrency,
                                                                                            targetCurrency,
                                                                                            value,
                                                                                            calculatedValue,
                                                                                            exchangeRate,
                                                                                            LocalDateTime.now());

                    lstConvertionHistory.add(convertionRec);
                    dbManager.saveConversion(convertionRec);
                    System.out.println("Conversão feita com sucesso: " + convertionRec);

                } else {
                    System.out.println("Não é possivel realizar conversão com valor negativa");
                }
            } else {
                System.out.println("Moeda inválida");
            }
        } catch (Exception error ){
        }

    }

    private Double getExchangeRate(String currency1, String currency2){
        Double conversionRate = 0.0;
        try {

            HttpResponse<String> response = exchangeController.requestPairConversion(currency1, currency2);
            conversionRate = jsonHandler.getConvertionRateAsDouble(jsonHandler.parseResponseBody(response), "conversion_rate") ;

        } catch (Exception error) {
            System.out.println("Exception: Houve algum problema na solicitação da taxa de conversão. Realizando o calculo localmente com valor histórico. " + error.getMessage());
            conversionRate = calculateExchangeRate(currency1, currency2);
        }

        return conversionRate;
    }

    public String getConversionHistory() {
        StringBuilder convertionHistory = new StringBuilder("Historico de conversão: \n");

        if (lstConvertionHistory.isEmpty())
        {
            return "Não há conversão no histórico";
        } else {
            for (ConvertionRec item: lstConvertionHistory )
            {
                convertionHistory.append(item.toString()).append("\n");
            }
            return convertionHistory.toString();
        }

    }

    public void  showCurrencyList(){

        for(Map.Entry<String, Double> currency: hshCurrency.entrySet()){
            System.out.println("currency: "+ currency.getKey() + " rate: "+ currency.getValue());
        }
    }

}

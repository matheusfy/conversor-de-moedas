package br.com.application;

import br.com.connection.Connectiondb;
import br.com.controllers.HttpclientController;
import br.com.models.ConvertionRec;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Conversor {

    private static final Logger logger = Logger.getLogger(Conversor.class.getName());

    private final HttpclientController __exchangeController;
    private final JsonHandler jsonHandler;

    private          Map<String, Double> __hshCurrency;
    private          Connectiondb dbManager;

    public            List<ConvertionRec> lstConvertionHistory;




    public Conversor() {
        this.__exchangeController = new HttpclientController(System.getenv("ExchangeListUri"), System.getenv("PairConversionUri"));
        this.__hshCurrency             = null;
        this.lstConvertionHistory = new ArrayList<>();
        this.jsonHandler                 = new JsonHandler();
        this.dbManager = new Connectiondb();
    }

    public void init() {
        try {
            this.__hshCurrency = getExchange();
            dbManager.testConnection();
            dbManager.loadConversionHistory(lstConvertionHistory);

            if (this.__hshCurrency == null) {
                logger.log(Level.WARNING, "Falha na comunicação com a API");
            }
        } catch (IOException | InterruptedException | SQLException e) {
            logger.log(Level.SEVERE, "Exception: Ocorreu um problema inesperado. " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private boolean isValidCurrency(String key) {
        return __hshCurrency.containsKey(key);
    }

    private  Map<String, Double> getExchange() throws IOException, InterruptedException {

        Map<String, Double> result = null;

        HttpResponse<String> response = __exchangeController.requestExchangeList();

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
            String currency = entry.getKey();
            double conversionRate = entry.getValue().getAsDouble();
            hshCurrency.put(currency, conversionRate);
        }
        return  hshCurrency;
    }

    /**
     * @param c1 : Source currency
     * @param c2: Target currency
     * @return : ExchangeRate to convert currency value to another currency value
     */
    private Double calculateExchangeRate(String c1, String c2){
        return __hshCurrency.get(c2)/__hshCurrency.get(c1);
    }

    private Double calculateConversion(Double value, Double conversionRate){
        return value * conversionRate;
    }

    public void convertCurrency(String sourceCurrency, String targetCurrency, Double value){

        try {
            if (isValidCurrency(sourceCurrency) && isValidCurrency(targetCurrency)) {

                if (value >= 0) {

                    System.out.println("Convertendo de %s para %s.".formatted(sourceCurrency, targetCurrency));


                    Double exchangeRate = getExchangeRate(sourceCurrency, targetCurrency);
                    Double calculatedValue = calculateConversion(value, exchangeRate);

                    logger.log(Level.INFO, "Valor calculado de %s/%s = %f".formatted(sourceCurrency, targetCurrency, calculatedValue));

                    ConvertionRec convertionRec = new ConvertionRec(sourceCurrency, targetCurrency, value, calculatedValue, exchangeRate, LocalDateTime.now());
                    lstConvertionHistory.add(convertionRec);

                    dbManager.saveConversion(convertionRec);
                    System.out.println("Conversão feita com sucesso: " + convertionRec.toString());

                } else {
                    System.out.println("Não é possivel realizar conversão com valor negativa");
                }
            } else {
                System.out.println("Moeda inválida");
            }
        } catch (Exception error ){
            logger.log(Level.SEVERE, "Exception: ocorreu um erro inesperado no calculo da conversão. " + error.getMessage());
        }

    }

    private Double getExchangeRate(String currency1, String currency2){
        Double conversionRate;
        try {

            HttpResponse response = __exchangeController.requestPairConversion(currency1, currency2);
            conversionRate = jsonHandler.getConvertionRateAsDouble(jsonHandler.parseResponseBody(response), "conversion_rate") ;

        } catch (Exception error) {
            System.out.println("Exception: Houve algum problema na solicitação da taxa de conversão. Realizando o calculo localmente com valor histórico. " + error.getMessage());
            conversionRate = calculateExchangeRate(currency1, currency2);
        }

        return conversionRate;
    }

    public String getConversionHistory() {
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

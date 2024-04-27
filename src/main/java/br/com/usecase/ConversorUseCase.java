package br.com.usecase;

import br.com.adapters.out.Connectiondb;
import br.com.adapters.out.HttpclientController;
import br.com.domain.exceptions.conversorexception.InvalidConversionValue;
import br.com.domain.exceptions.conversorexception.InvalidCurrencyException;
import br.com.domain.exceptions.conversorexception.InvalidCurrencyHashException;
import br.com.domain.exceptions.jsonhandlerexception.InvalidJsonConversion;
import br.com.domain.exceptions.requestexception.InvalidRequestException;
import br.com.domain.model.ConvertionRec;

import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ConversorUseCase {

    private final HttpclientController exchangeController;
    private final JsonHandler jsonHandler;
    private final Connectiondb dbManager;

    private          Map<String, Double> hshCurrency;
    private          boolean                        currencyListLoaded ;

    public            List<ConvertionRec> lstConvertionHistory;




    public ConversorUseCase() {
        this.exchangeController   = new HttpclientController(System.getenv("ExchangeListUri"), System.getenv("PairConversionUri"));
        this.lstConvertionHistory = new ArrayList<>();
        this.jsonHandler                 = new JsonHandler();
        this.dbManager                   = new Connectiondb();

        this.hshCurrency                = null;
        this.currencyListLoaded = false;
    }

    public void init() {
        try {
            this.hshCurrency = getExchangeAsHash();
            dbManager.initConnection();
            dbManager.loadConversionHistory(lstConvertionHistory);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isValidCurrency(String key) {
        return hshCurrency.containsKey(key);
    }

    private  Map<String, Double> getExchangeAsHash(){

        Map<String, Double> exchangeHash = null;
        HttpResponse<String> response = exchangeController.requestExchangeList();
        try {

            if (exchangeController.isValidResponse(response)) {
                System.out.println(response.statusCode());
                exchangeHash = jsonHandler.responseToHash(response);

                if (!exchangeHash.isEmpty()){
                    currencyListLoaded = true;
                }
            } else {
                System.out.println("Response msg:" + response.toString());
                throw new InvalidRequestException("Requisição invalida. Status code:" + response.statusCode());
            }
        } catch (InvalidJsonConversion | InvalidRequestException error) {
            System.out.println(error.getMessage());
        } finally {
            return exchangeHash;
        }
    }

    private Double calculateExchangeRate(String c1, String c2){
        return hshCurrency.get(c2)/ hshCurrency.get(c1);
    }

    private Double calculateConversion(Double value, Double conversionRate){
        return value * conversionRate;
    }

    public void convertCurrency(String sourceCurrency, String targetCurrency, Double value) throws InvalidConversionValue, InvalidCurrencyException{
        String exceptionMsg = "";

            if (canConvert(sourceCurrency,targetCurrency)) {

                if (value >= 0) {

                    System.out.printf("Convertendo de %s para %s.", sourceCurrency, targetCurrency);

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
                    exceptionMsg = "Não é possivel realizar conversão com valor negativa %f".formatted(value);
                    System.out.println(new InvalidConversionValue(exceptionMsg).getMessage());
                }
            } else {

                //TODO: Converter esta mensagem de conversão invalida para mensagem de Error
                exceptionMsg = "Conversão inválida. Verifique se as moedas existem: %s, %s".formatted(sourceCurrency, targetCurrency);
                tryGetCurrencyHash();

                System.out.println(new InvalidCurrencyException(exceptionMsg).getMessage());
            }
    }

    private Double getExchangeRate(String currency1, String currency2){
        Double conversionRate = 0.0;
        try {

            HttpResponse<String> response = exchangeController.requestPairConversion(currency1, currency2);
            conversionRate = jsonHandler.getConvertionRateAsDouble(jsonHandler.parseResponseBody(response), "conversion_rate") ;

        } catch (Exception error) {
            System.out.println("Houve algum problema na solicitação da taxa de conversão. Realizando o calculo localmente com valor histórico. " + error.getMessage());
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

    public void  showCurrencyList() throws InvalidCurrencyHashException {

        if (isValidHash()){
            for(Map.Entry<String, Double> currency: hshCurrency.entrySet()){
                System.out.println("currency: "+ currency.getKey() + " rate: "+ currency.getValue());
            }
        } else {
            tryGetCurrencyHash();
            if(!isValidHash()){
                throw new InvalidCurrencyHashException("Não possuimos a lista de ativos.");
            }
        }
    }


    private void tryGetCurrencyHash() {
        int MAX_ATEMPT = 3;

        if(!isValidHash()) {
            System.out.println("Buscando lista de moedas.");
            int tentativas = 0;
            while(tentativas<MAX_ATEMPT && !isValidHash()){
                System.out.println("Tentativa %i de buscar lista de moedas: ".formatted((tentativas + 1)));
                hshCurrency = getExchangeAsHash();
                tentativas++;
            }

            if (tentativas == MAX_ATEMPT && !isValidHash()){
                currencyListLoaded = false;
            }
        }
    }

    private boolean isValidHash(){
        return hshCurrency != null && !hshCurrency.isEmpty();
    }

    private boolean canConvert(String sourceCurrency, String targetCurrency)
    {
        return isValidHash() && isValidCurrency(sourceCurrency) && isValidCurrency(targetCurrency);
    }

}

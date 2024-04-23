package br.com.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record ConvertionRec(
        String from,
        String to,
        Double convertionValue,
        Double resultValue,
        Double conversionRate,
        LocalDateTime dateTime
) {

    public String toString(){
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String  formatedDateTime = myFormatObj.format(dateTime);
        return "[%s] De: %s - Para: %s - Valor Entrada: %.3f %s - Resultado: %.2f %s - Taxa de conversão: %.2f".formatted(formatedDateTime, from, to, convertionValue, from, resultValue, to, conversionRate);
    }
}

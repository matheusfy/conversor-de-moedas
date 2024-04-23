package br.com.models;

public record ConvertionRec(
        String from,
        String to,
        Double convertionValue,
        Double resultValue
) {

    public String toString(){
        return "De: %s - Para: %s - Valor Entrada: %.3f - Resultado: %.2f %s".formatted(from, to, convertionValue, resultValue, to);
    }
}

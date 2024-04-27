package br.com.domain.exceptions.conversorexception;

public class InvalidCurrencyHashException extends Exception{

    public InvalidCurrencyHashException(String msg){
        super("Exception: " + msg);
    }
}

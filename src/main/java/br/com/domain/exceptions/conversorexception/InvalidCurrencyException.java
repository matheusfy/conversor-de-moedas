package br.com.domain.exceptions.conversorexception;

public class InvalidCurrencyException extends Exception{

    public InvalidCurrencyException(String msg){
        super("Exception: " + msg);
    }
}

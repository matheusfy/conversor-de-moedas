package br.com.domain.exceptions.conversorexception;

public class InvalidConversionValue extends Exception{

    public InvalidConversionValue(String msg){
        super("Exception: " + msg);
    }
}

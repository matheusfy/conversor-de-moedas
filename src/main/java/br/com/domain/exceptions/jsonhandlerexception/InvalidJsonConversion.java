package br.com.domain.exceptions.jsonhandlerexception;

import com.google.gson.JsonParseException;

public class InvalidJsonConversion extends JsonParseException {
    public InvalidJsonConversion(String msg){
        super("Exception: " + msg);
    }
}

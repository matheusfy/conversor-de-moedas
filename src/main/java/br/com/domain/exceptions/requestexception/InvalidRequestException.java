package br.com.domain.exceptions.requestexception;

import java.io.IOException;

public class InvalidRequestException extends IOException {
    public InvalidRequestException(String msg)
    { super("Exception: " + msg);}
}

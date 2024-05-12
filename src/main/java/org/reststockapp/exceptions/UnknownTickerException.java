package org.reststockapp.exceptions;

public class UnknownTickerException extends RuntimeException{
    public UnknownTickerException(String message){
        super(message);
    }
}
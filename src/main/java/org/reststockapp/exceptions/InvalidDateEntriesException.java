package org.reststockapp.exceptions;

public class InvalidDateEntriesException extends RuntimeException{
    public InvalidDateEntriesException(String message){
        super(message);
    }
}
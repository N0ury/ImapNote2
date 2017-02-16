package com.Pau.ImapNotes2.Miscs;

/**
 * Created by kj on 2017-02-15 10:12.
 * Simple class to allow functions to return a value and a status as a single object.
 * @param <T> the type parameter
 */
public class Result<T> {
    /**
     * The result can be any type, usually a String or number.
     */
    public final T result;
    /**
     * True if the result is valid, else false.
     */
    public final boolean succeeded;


    public Result(T result,
                  boolean succeeded){
        this.result = result;
        this.succeeded = succeeded;
    }
}

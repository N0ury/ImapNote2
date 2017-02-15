package com.Pau.ImapNotes2.Miscs;

/**
 * Created by kj on 2017-02-15 10:12.
 */


public class Result<T> {
    public final T result;
    public final boolean succeeded;

    public Result(T result,
                  boolean succeeded){
        this.result = result;
        this.succeeded = succeeded;
    }
}

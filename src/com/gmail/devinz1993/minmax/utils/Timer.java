package com.gmail.devinz1993.minmax.utils;


public class Timer {
	
    private volatile long start, end;
    private int state = 0;

    public final synchronized void start() {
        if (0 != state) {
        	throw new IllegalStateException();
        } else {
        	start = System.currentTimeMillis();
        	state = 1;
        }
    }
    
    public final synchronized void stop() {
    	if (1 != state) {
    		throw new IllegalStateException();
    	} else {
    		end = System.currentTimeMillis();
    		state = 0;
    	}
    }
    
    public final long get() {
    	if (1 == state) {
    		return System.currentTimeMillis() - start;
    	} else {
    		return end - start;
    	}
    }
    
}

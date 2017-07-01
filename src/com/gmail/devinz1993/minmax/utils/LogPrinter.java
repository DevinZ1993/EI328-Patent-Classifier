package com.gmail.devinz1993.minmax.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.sun.xml.internal.ws.Closeable;


public class LogPrinter implements Closeable {
	
    private final PrintWriter out;

    public LogPrinter(String path) {
        try {
            out = new PrintWriter(new FileWriter(path, true));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
    
    public final synchronized void close() {
    	out.close();
    }
    
    public final synchronized void print(String str) {
        System.out.print(str);
        out.print(str);
    }
    
    public final synchronized void println(String str) {
        System.out.println(str);
        out.println(str);
    }
    
    public final synchronized void println() {
        System.out.println();
        out.println();
    }
    
}

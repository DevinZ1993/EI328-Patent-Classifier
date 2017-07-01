package com.gmail.devinz1993.minmax.impls;


public class Impls {
	
	private Impls() {}
	
	public static Impl newImplementor(String arg) {
		if (arg.equals("liblinear")) {
			return new LibImpl();
		} else {
			return new MyImpl();
		}
	}
}


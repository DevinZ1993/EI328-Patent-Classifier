package com.gmail.devinz1993.minmax.impls;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.Problem;


public interface Impl extends Cloneable {
	
	Impl clone();
	
	void train(Problem problem);
	
	int predict(Feature[] features, double threshold);
	
}


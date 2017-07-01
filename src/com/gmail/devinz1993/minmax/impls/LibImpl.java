package com.gmail.devinz1993.minmax.impls;

import java.lang.reflect.Field;

import de.bwaldvogel.liblinear.*;


class LibImpl implements Impl {
	
	private volatile Model model = null;
	
	public synchronized void train(Problem problem) {
		model = Linear.train(problem, new Parameter
                (SolverType.L2R_L2LOSS_SVC_DUAL, 1.0, 0.01));
	}
	
	@Override public String toString() {
		return "LibImplementor";
	}
	
	@Override public Impl clone() {
		return new LibImpl();
	}
	
	public int predict(Feature[] features, double threshold) {
		if (null == model) {
			throw new IllegalStateException();
		} else {
			try {
				Field field = model.getClass().getDeclaredField("w");
				double[] w = model.getFeatureWeights();
				double cst = w[w.length-1];
				
				field.setAccessible(true);
				((double[])(field.get(model)))[w.length-1] = cst-threshold;
				int val = (Linear.predict(model, features) >= .5)? 1:0;
				((double[])(field.get(model)))[w.length-1] = cst;
				return val;
			} catch (NoSuchFieldException | SecurityException | 
					IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			
		}
	}
    
}


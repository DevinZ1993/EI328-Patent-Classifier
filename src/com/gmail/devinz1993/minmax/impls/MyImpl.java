package com.gmail.devinz1993.minmax.impls;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.Problem;


class MyImpl implements Impl {
	
    public static final double LAMBDA = 0, EPSILON = 1;
    private volatile double[] weights;
    
    @Override public String toString() {
    	return "MyImplementor";
    }
    
    @Override public Impl clone() {
    	return new MyImpl();
    }
    
	public synchronized void train(Problem problem) {
		weights = new double[problem.n];
		for (int iteration=0; iteration<2000; iteration++) {
            double[] grad = getGrad(problem);
            double norm = getNorm(grad);
            
            if (norm < EPSILON) {
            	break;
            } else {
            	if (0 == iteration % 20) {
	                System.out.println("Iteration "+iteration+":  norm(grad) = "+norm);
	            }
	            for (int i=0; i<weights.length; i++) {
	                weights[i] -= norm*grad[i];
	            }
            }
        } 
	}

	public int predict(Feature[] features, double threshold) {
		return (sigmoid(features, threshold) >= .5)? 1 : 0;
	}
	
	private double[] getGrad(Problem problem) {
        double[] grad = new double[weights.length];
        
        for (int i=0;i<weights.length;i++) {
            grad[i] = LAMBDA*weights[i];
        }
        for (int idx=0; idx<problem.l; idx++) {
        	double delt = sigmoid(problem.x[idx], 0)-problem.y[idx];
	        
	        for (Feature feature : problem.x[idx]) {
	        	int i = feature.getIndex()-1;
	        	
	        	if (i >= 0 && i < grad.length-1) {
	        		grad[i] += delt * feature.getValue();
	        	}
	        }
	        grad[grad.length-1] += delt;
        }
        return grad;
    }
    
    private double getNorm(double[] vect) {
        double val = 0;
        
        for (int i=1;i<vect.length;i++) {
            val += vect[i]*vect[i];
        }
        return Math.sqrt(val/vect.length);
    }

	private double sigmoid(Feature[] features, double threshold) {
		if (null == weights) {
			throw new IllegalStateException();
		} else {
			double result = weights[weights.length-1];
			
			for (Feature feature : features) {
				int i = feature.getIndex()-1;
				
				if (i >= 0 && i < weights.length-1) {
					result += weights[i] * feature.getValue();
				}
			}
			return 1./(1+Math.exp(threshold-result));
		}
	}
    
}


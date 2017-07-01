package com.gmail.devinz1993.minmax.jobs;

import java.io.IOException;

import com.gmail.devinz1993.minmax.impls.Impl;
import com.gmail.devinz1993.minmax.utils.LogPrinter;
import com.gmail.devinz1993.minmax.utils.Timer;

import de.bwaldvogel.liblinear.InvalidInputDataException;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.Train;


class BasicJob implements Job {

	private final Impl implementor;
	private final LogPrinter printer = new LogPrinter("log/basic.log");
	private final AbstractJob helper = new AbstractJob(printer);
	private final Timer timer = new Timer();
	
	public BasicJob(Impl implementor) {
		this.implementor = implementor;
    }
	
	public synchronized void terminate() {
		helper.terminate();
		printer.close();
	}
    
	@Override public String toString() {
		return "BasicJob+"+implementor;
	}
	
	public synchronized void work(double threshold)
			throws IOException, InvalidInputDataException {
		printer.println(this+" t="+threshold);
		train();
		System.gc();
		test(threshold);
		System.gc();
	}
	
	private void train() throws IOException, InvalidInputDataException {
		Problem problem = Train.readProblem(Jobs.TRAIN, 1);
		
		timer.start();
		implementor.train(problem);
		timer.stop();
		printer.println("Training time: "+timer.get()+" ms.");
	}
	
	private void test(double threshold) throws IOException, InvalidInputDataException {
		Problem problem = Train.readProblem(Jobs.TEST, 1);
		int tp = 0, fp = 0, tn = 0, fn = 0;
		
		timer.start();
		for (int i=0; i<problem.l; i++) {
			double y = implementor.predict(problem.x[i], threshold);
			
			if (problem.y[i] >= .5) {
				if (y >= .5) {
					tp ++;
				} else {
					fn ++;
				}
			} else {
				if (y >= .5) {
					fp ++;
				} else {
					tn ++;
				}
			}
		}
		timer.stop();
		printer.println("Testing time: "+timer.get()+" ms.");
		helper.logResult(tp, fp, tn, fn);
	}

}



package com.gmail.devinz1993.minmax.jobs;

import com.gmail.devinz1993.minmax.utils.LogPrinter;


class AbstractJob implements Job {
	
	public final LogPrinter printer;
	
	public AbstractJob(LogPrinter printer) {
		this.printer = printer;
	}
	
	public void terminate() {}
	
	public void work(double threshold) {}
	
	public void logResult(int tp, int fp, int tn, int fn) {
		printer.println("acc = "+(0.+tp+tn)/(tp+fp+tn+fn));
		printer.println("F1 = "+2.*tp/(2*tp+fp+fn));
		printer.println("TPR = "+tp/(0.+tp+fn));
		printer.println("FPR = "+fp/(0.+fp+tn));
		printer.println();
	}
	
}


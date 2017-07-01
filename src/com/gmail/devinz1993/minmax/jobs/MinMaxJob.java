package com.gmail.devinz1993.minmax.jobs;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.*;

import com.gmail.devinz1993.minmax.impls.Impl;
import com.gmail.devinz1993.minmax.utils.LogPrinter;
import com.gmail.devinz1993.minmax.utils.Timer;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.InvalidInputDataException;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.Train;


class MinMaxJob implements Job {
	
	private final Impl implementor;
	private final LogPrinter printer = new LogPrinter("log/minmax.log");
	private final AbstractJob helper = new AbstractJob(printer);
	private final List<List<Future<Impl>>> futures = new ArrayList<>();
	private final int grain, nthreads;
	
    public MinMaxJob(Impl implementor, int grain, int nthreads) {
    	this.implementor = implementor;
    	this.grain = grain;
    	this.nthreads = nthreads;
    	
    }
    
    public synchronized void terminate() {
    	helper.terminate();
    	printer.close();
    }
    
    @Override public String toString() {
    	return "MinMaxJob+"+implementor+" m="+grain+" n="+nthreads;
    }
    
    public synchronized void work(double threshold) 
    		throws IOException, InvalidInputDataException {
    	printer.println(this+" t="+threshold);
    	try {
    		train();
	    	System.gc();
			test(threshold);
			System.gc();
    	} catch (InterruptedException e) {
    		throw new RuntimeException(e);
    	} finally {
    		futures.clear();
    	}
    }
    
    private void train() 
    		throws IOException, InvalidInputDataException, InterruptedException {
    	final Problem problem = Train.readProblem(Jobs.TRAIN, 1);
    	final List<Integer> positives = new ArrayList<Integer>();
    	final List<Integer> negatives = new ArrayList<Integer>();
    	final Timer timer = new Timer();
    	
    	for (int i=0; i<problem.l; i++) {
    		if (problem.y[i] >= .5) {
    			positives.add(i);
    		} else {
    			negatives.add(i);
    		}
    	}
    	final int posGroups = Math.max(1, (int)((0.+positives.size())/grain+.5));
    	final int negGroups = Math.max(1, (int)((0.+negatives.size())/grain+.5));
    	final ExecutorService service = Executors.newFixedThreadPool(nthreads);
    	final CountDownLatch latch = new CountDownLatch(posGroups*negGroups);
    	
    	timer.start();
    	for (int i=0, ii=positives.size(); i<positives.size(); i+=ii++/posGroups) {
    		futures.add(new ArrayList<Future<Impl>>());
    		for (int j=0, jj=negatives.size(); j<negatives.size(); j+=jj++/negGroups) {
    			final Problem sub = subproblem(problem, positives, i, ii/posGroups, 
    					negatives, j, jj/negGroups);
    			
    			futures.get(ii-positives.size()).add(service.submit(new Callable<Impl>() {
	    			public Impl call() {
		    			Impl worker = implementor.clone();
		    			
		    			worker.train(sub);
		    			latch.countDown();
		    			return worker;
		    		}
	    		}));
    		}
    	}
    	latch.await();
    	service.shutdown();
		timer.stop();
    	printer.println("Training time: "+timer.get()+" ms.");
    }
    
    private void test(final double threshold) 
    		throws IOException, InvalidInputDataException, InterruptedException {
    	final Problem problem = Train.readProblem(Jobs.TEST, 1);
    	final ExecutorService service = Executors.newFixedThreadPool(nthreads);
    	final BlockingQueue<List<Integer>> q = new LinkedBlockingQueue<>();
    	final Timer timer = new Timer();
    	
    	timer.start();
		for (int i=0; i<futures.size(); i++) {
			final List<Future<Impl>> minModule = futures.get(i);
			
			service.execute(new Runnable() {
				public void run() {
					for (int j=0; j<problem.l; j++) {
						int result = 1;
						
						for (Future<Impl> future : minModule) {
							try {
								result = Math.min(result, 
	    								future.get().predict(problem.x[j], threshold));
							} catch (ExecutionException | InterruptedException e) {
								throw new RuntimeException(e);
							}
						}
						q.add(Arrays.asList(j, result));
					}
				}
			});
		}
    	int[] results = new int[problem.l];
    	
    	for (int i=0, j=0; i<problem.l*futures.size(); i++) {
    		if (i >= j*problem.l*futures.size()/10) {
    			System.out.println("Tests finished "+10*j+"%.");
    			j++;
    		}
    		List<Integer> pair = q.take();
    		
    		results[pair.get(0)] = Math.max(results[pair.get(0)], pair.get(1));
    	}
    	service.shutdown();
    	timer.stop();
    	printer.println("Testing time: "+timer.get()+" ms.");
    	logResult(problem, results);
    }
    
    private Problem subproblem(Problem problem, List<Integer> posIndex, int posIdx, int posNum,
    		List<Integer> negIndex, int negIdx, int negNum) {
    	Problem sub = new Problem();
    	
    	sub.bias = problem.bias;
    	sub.n = problem.n;
    	sub.x = new Feature[posNum+negNum][];
    	sub.y = new double[posNum+negNum];
    	sub.l = 0;
    	for (int i=0; i<posNum; i++, sub.l++) {
    		sub.x[sub.l] = problem.x[posIndex.get(posIdx+i)];
    		sub.y[sub.l] = problem.y[posIndex.get(posIdx+i)];
    	}
    	for (int j=0; j<negNum; j++, sub.l++) {
    		sub.x[sub.l] = problem.x[negIndex.get(negIdx+j)];
    		sub.y[sub.l] = problem.y[negIndex.get(negIdx+j)];
    	}
    	return sub;
    }
    
    private void logResult(Problem problem, int[] results) {
    	int tp = 0, fp = 0, tn = 0, fn = 0;
    	
    	for (int i=0; i<problem.l; i++) {
    		if (problem.y[i] >= .5) {
				if (results[i] >= .5) {
					tp ++;
				} else {
					fn ++;
				}
			} else {
				if (results[i] >= .5) {
					fp ++;
				} else {
					tn ++;
				}
			}
    	}
    	helper.logResult(tp, fp, tn, fn);
    }
    
}



package com.gmail.devinz1993.minmax.jobs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.StringTokenizer;

import com.gmail.devinz1993.minmax.impls.Impl;
import com.gmail.devinz1993.minmax.impls.Impls;

import de.bwaldvogel.liblinear.InvalidInputDataException;


interface Job {
	
	void work(double threshold) throws IOException, InvalidInputDataException;
	
	void terminate();
	
}

public class Jobs {
	
	private Jobs() {}
	
	public static File TRAIN = new File("data/train.in");
	public static File TEST = new File("data/test.in");
	
	static {
		if (!TRAIN.exists()) {
			preproc("train");
		}
		if (!TEST.exists()) {
			preproc("test");
		}
	}
	
	private static void preproc(String prefix) {
		System.out.print("Preprocessing "+prefix+".txt ... ");
		PrintWriter writer = null;
		Scanner scanner = null;
		
		try {
			scanner = new Scanner(new FileInputStream("data/"+prefix+".txt"));
			try {
				writer = new PrintWriter(new FileOutputStream("data/"+prefix+".in"));
				try {
		            while (scanner.hasNextLine()) {
		                StringTokenizer tok = new StringTokenizer(scanner.nextLine());
		                
		                if (tok.nextToken().charAt(0)=='A') {
		                    writer.print("1 ");
		                } else {
		                    writer.print("0 ");
		                }
		                while (tok.hasMoreTokens()) {
		                    writer.print(tok.nextToken()+" ");
		                }
		                writer.println();
		            }
				} finally {
					writer.close();
				}
			} catch (FileNotFoundException e) {
				System.out.println("Aborted.");
				throw new IllegalArgumentException(e);
			} finally {
				scanner.close();
			}
		} catch (FileNotFoundException e) {
			System.out.println("Aborted.");
			throw new IllegalArgumentException(e);
		}
		System.out.println("Done.");
	}
	
	private static final String USAGE = "Usage: java --classpath bin:lib/liblinear-java-1.95.jar"
			+ " com.gmail.devinz1993.jobs.Jobs [--liblinear] [--minmax] [OPTION]";
	
	public static void main(String[] args) {
		Impl implementor = null;
		boolean minmax = false;
		double tmin = 0, tmax = 0, tstep = 1;
		int mmin = 2000, mmax = 2000, mstep = 1000;
		int nmin = 17, nmax = 17, nstep = 1;
		int idx = 0;
		
		if (idx < args.length && args[idx].equals("--liblinear")) {
			implementor = Impls.newImplementor("liblinear");
			idx++;
		} else {
			implementor = Impls.newImplementor("logistic");
		}
		if (idx < args.length && args[idx].equals("--minmax")) {
			minmax = true;
			idx++;
		}
		for (; idx<args.length; idx+=2) {
			if (idx+1 == args.length || !args[idx].startsWith("--")) {
				throw new IllegalArgumentException(args[idx]+"\n"+USAGE);
			} else if (args[idx].equals("--tmin")) {
				tmin = Double.parseDouble(args[idx+1]);
			} else if (args[idx].equals("--tmax")) {
				tmax = Double.parseDouble(args[idx+1]);
			} else if (args[idx].equals("--tstep")) {
				tstep = Double.parseDouble(args[idx+1]);
			} else if (minmax) {
				if (args[idx].equals("--mmin")) {
					mmin = Integer.parseInt(args[idx+1]);
				} else if (args[idx].equals("--mmax")) {
					mmax = Integer.parseInt(args[idx+1]);
				} else if (args[idx].equals("--mstep")) {
					mstep = Integer.parseInt(args[idx+1]);
				} else if (args[idx].equals("--nmin")) {
					nmin = Integer.parseInt(args[idx+1]);
				} else if (args[idx].equals("--nmax")) {
					nmax = Integer.parseInt(args[idx+1]);
				} else if (args[idx].equals("--nstep")) {
					nstep = Integer.parseInt(args[idx+1]);
				} else {
					throw new IllegalArgumentException(args[idx]+"\n"+USAGE);
				}
			} else {
				throw new IllegalArgumentException(args[idx]+"\n"+USAGE);
			}
		}
		if (tstep < 0 || mstep < 0 || nstep < 0 || tmin > tmax || 
				mmin > mmax || nmin > nmax || mmin <= 0 || nmin <= 0) {
			throw new IllegalArgumentException(args[idx]+"\n"+USAGE);
		} else if (minmax) {
			batchMinMax(implementor, tmin, tmax, tstep,
					mmin, mmax, mstep, nmin, nmax, nstep);
		} else {
			batchBasic(implementor, tmin, tmax, tstep);
		}
	}
	
	private static void batchBasic(Impl implementor, double tmin, double tmax, double tstep) {
		Job job = new BasicJob(implementor);
		
		try {
			for (double t=tmin; t<=tmax; t+=tstep) {
				job.work(t);
			}
		} catch (IOException | InvalidInputDataException e) {
			throw new RuntimeException(e);
		} finally {
			job.terminate();
		}
	}
	
	private static void batchMinMax(Impl implementor, double tmin, double tmax, double tstep, 
			int mmin, int mmax, int mstep, int nmin, int nmax, int nstep) {
		Job job = null;
		
		for (int m=mmin; m<=mmax; m+=mstep) {
			for (int n=nmin; n<=nmax; n+=nstep) {
				job = new MinMaxJob(implementor, m, n);
				try {
					for (double t=tmin; t<=tmax; t+=tstep) {
						job.work(t);
					}
				} catch (IOException | InvalidInputDataException e) {
					throw new RuntimeException(e);
				} finally {
					job.terminate();
				}
			}
		}
	}
	
}

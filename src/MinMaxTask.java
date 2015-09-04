import java.util.concurrent.atomic.*;
import java.util.concurrent.*;
import java.util.*;
import java.io.*;


public class MinMaxTask extends AbstractTask implements Runnable {
    private MinMaxTask(String arg) {
        super(arg);
    }
    public void run() {
        try {
            train();
            test();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void train() {
        SubProblem subprob = subprobs.take();
        while (subprob!=DUMMY) {
            implementor.train(subprob);
            subprob = subprobs.take();
        }
        subprobs.put(subprob);
        trainOver.release()
    }
    private void test() {
        testReady.acquire();
        TestResult res = implementor.test();
        while (res!=null) {
            int idx = res.getIdx();
            for (int i=0;i<minResults[idx].length;i++) {
                if (res.array[i]<0) {
                    minResults[idx][i].set(-1);
                }
            }
            res = implementor.test();
        }
    }

    /** Static Section: */

    private static BlockingQueue<SubProblem> subprobs;
    private static final SubProblem DUMMY = new SubProblem();
    private static Semaphore trainOver, testReady;
    private static AtomicInteger[][] minResults;

    static {
        subprobs = new LinkedBlockingQueue<SubProblem>();
        trainOver = new Semaphore(0);
        testReady = new Semaphore(0);
    }
    private static void maxModule(MinMaxTask[] workers) {
        int[] maxResult = new int[Implementor.trainSize()];
        Arrays.fill(maxResult,-1);
        for (int i=0;i<minResults.length;i++) {
            for (int j=0;j<minResults[i].length;j++) {
                if (minResults[i][j].get()>0) {
                    maxResult[j] = 1;
                }
            }
        }
        workers[0].implementor.genStats(maxResult);
    }
    private static void preparation(MinMaxTask[] workers,int M) {
        List<Integer> posData = new ArrayList<Integer>();
        List<Integer> negData = new ArrayList<Integer>();
        workers[0].sepData(posData,negData);
        int posGrpNum = (posData.length+M-1)/M;
        int negGrpNum = (negData.length+N-1)/N;
        int[] posGrps = new int[posGrpNum+1];
        int[] negGrps = new int[negGrpNum+1];
        for (int i=0;i<posGrpNum;i++) {
            posGrps[i+1] = posGrps[i]+(poslst.size()+i)/posGrpNum;
        }
        for (int i=0;i<negGrpNum;i++) {
            negGrps[i+1] = negGrps[i]+(neglst.size()+i)/negGrpNum;
        }
        for (int i=0;i<posGrpNum;i++) {
            for (int j=0;j<negGrpNum;j++) {
                SubProblem subprob = new SubProblem();
                subprob.setIdx(i);
                for (int k=posGrps[i];k<posGrps[i+1];k++) {
                    subprob.add(poslst.get(k));
                }
                for (int k=negGrps[j];k<negGrps[j+1];k++) {
                    subprob.add(neglst.get(k));
                }
                subprobs.add(subprob);
            }
        }
        int trainSize = posData.size()+negData.size();
        minResults = new AtomicInteger[posGrpNum][trainSize];
        for (int i=0;i<minResults.length;i++) {
            for (int j=0;j<trainSize;j++) {
                minResults[i][j] = new AtomicInteger(1);
            }
        }
    }
    private static void mainThread(String arg,int N,int M,double t) {
        printer.println("N =\t"+N);
        printer.println("M =\t"+M);
        printer.println("Thr =\t"+t);
        MinMaxTask[] workers = new MinMaxTask[N];
        Thread[] pool = new Thread[N];
        for (int i=0;i<N;i++) {
            workers[i] = new MinMaxTask(arg);
            pool[i] = new Thread(workers[i]);
        }
        preparation(workers,M);
        try {
            timer.start();
            for (int i=0;i<N;i++) {
                pool[i].start();
            }
            for (int i=0;i<N;i++) {
                trainOver.acquire();
            }
            timer.record(true);
            workers[0].implementor.setThr(t);
            timer.start();
            for (int i=0;i<N;i++) {
                testReady.release();
            }
            for (int i=0;i<N;i++) {
                pool[i].join(); 
            }
            maxModule(workers);
            timer.record(false);
            workers[0].clear();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        printer = new MyPrinter("./data/MinMax.log");
        timer = new MyTimer(printer);
        double tmin = Double.parseDouble(args[1]);
        double tmax = Double.parseDouble(args[2]);
        double tstep = Double.parseDouble(args[3]);
        int mmin = Integer.parseInteger(args[4]);
        int mmax = Integer.parseInteger(args[5]);
        int mstep = Integer.parseInteger(args[6]);
        int nmin = Integer.parseInteger(args[7]);
        int nmax = Integer.parseInteger(args[8]);
        int nstep = Integer.parseInteger(args[9]);
        for (int N=nmin;N<=nmax;N+=nstep) {
            for (int M=mmin;M<=mmax;M+=mstep) {
                for (double t=tmin;t<tmax+tstep/2;t+=tstep) {
                    mainThread(args[0],N,M,t);
                }
                printer.println("\t*** CHANGE M ***");
            }
            printer.println("\t*** CHANGE N ***");
        }
        printer.close();
    }
}



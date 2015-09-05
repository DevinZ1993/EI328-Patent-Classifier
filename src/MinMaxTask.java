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
    private void train() throws InterruptedException {
        SubProblem subprob = subprobs.take();
        while (subprob!=SubProblem.DUMMY) {
            implementor.train(subprob);
            subprob = subprobs.take();
        }
        subprobs.put(subprob);
        trainOver.release();
    }
    private void test() throws InterruptedException {
        testReady.acquire();
        TestResult res = implementor.test();
        while (res!=null) {
            int idx = res.getIdx();
            for (int i=0;i<minResults[idx].length;i++) {
                if (res.array[i]<=0) {
                    minResults[idx][i].set(0);
                }
            }
            res = implementor.test();
        }
    }

    /** Static Section: */

    private static BlockingQueue<SubProblem> subprobs;
    private static Semaphore trainOver, testReady;
    private static AtomicInteger[][] minResults;
    private static int numOfMin, testSize;

    static {
        subprobs = new LinkedBlockingQueue<SubProblem>();
        trainOver = new Semaphore(0);
        testReady = new Semaphore(0);
    }
    private static void buildSubProbs(Implementor impl,int M) {
        List<Integer> posData = new LinkedList<Integer>();
        List<Integer> negData = new LinkedList<Integer>();
        impl.staticSepData(posData,negData);
        int posGrpNum = (posData.size()+M-1)/M;
        int negGrpNum = (negData.size()+M-1)/M;
        int[] posGrps = new int[posGrpNum+1];
        int[] negGrps = new int[negGrpNum+1];
        for (int i=0;i<posGrpNum;i++) {
            posGrps[i+1] = posGrps[i]+(posData.size()+i)/posGrpNum;
        }
        for (int i=0;i<negGrpNum;i++) {
            negGrps[i+1] = negGrps[i]+(negData.size()+i)/negGrpNum;
        }
        subprobs.clear();
        for (int i=0;i<posGrpNum;i++) {
            for (int j=0;j<negGrpNum;j++) {
                SubProblem subprob = new SubProblem();
                subprob.setIdx(i);
                for (int k=posGrps[i];k<posGrps[i+1];k++) {
                    subprob.add(posData.get(k));
                }
                for (int k=negGrps[j];k<negGrps[j+1];k++) {
                    subprob.add(negData.get(k));
                }
                subprobs.add(subprob);
            }
        }
        subprobs.add(SubProblem.DUMMY);
        numOfMin = posGrpNum;
    }
    private static void buildMinModules() {
        minResults = new AtomicInteger[numOfMin][testSize];
        for (int i=0;i<minResults.length;i++) {
            for (int j=0;j<testSize;j++) {
                minResults[i][j] = new AtomicInteger(1);
            }
        }
    }
    private static void maxModule(Implementor impl) {
        int[] maxResult = new int[testSize];
        Arrays.fill(maxResult,0);
        for (int i=0;i<minResults.length;i++) {
            for (int j=0;j<minResults[i].length;j++) {
                if (minResults[i][j].get()>0) {
                    maxResult[j] = 1;
                }
            }
        }
        impl.staticGenStats(maxResult);
    }
    private static void mainThread(String arg,int N,int M,double t) {
        printer.println("N  =  "+N);
        printer.println("M  =  "+M);
        printer.println("Thr = "+t);
        MinMaxTask[] workers = new MinMaxTask[N];
        Thread[] pool = new Thread[N];
        for (int i=0;i<N;i++) {
            workers[i] = new MinMaxTask(arg);
            pool[i] = new Thread(workers[i]);
        }
        Implementor impl = workers[0].implementor;
        testSize = impl.staticTestSize();
        buildSubProbs(impl,M);
        buildMinModules();
        try {
            timer.start();
            for (int i=0;i<N;i++) {
                pool[i].start();
            }
            for (int i=0;i<N;i++) {
                trainOver.acquire();
            }
            timer.record(true);
            impl.staticSetThr(t);
            timer.start();
            for (int i=0;i<N;i++) {
                testReady.release();
            }
            for (int i=0;i<N;i++) {
                pool[i].join(); 
            }
            maxModule(impl);
            timer.record(false);
            impl.staticClear();
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
        int mmin = Integer.parseInt(args[4]);
        int mmax = Integer.parseInt(args[5]);
        int mstep = Integer.parseInt(args[6]);
        int nmin = Integer.parseInt(args[7]);
        int nmax = Integer.parseInt(args[8]);
        int nstep = Integer.parseInt(args[9]);
        for (int N=nmin;N<=nmax;N+=nstep) {
            for (int M=mmin;M<=mmax;M+=mstep) {
                for (double t=tmin;t<tmax+tstep/2;t+=tstep) {
                    if (N>0 && M>0) {
                        mainThread(args[0],N,M,t);
                    } else {
                        throw new IllegalArgumentException();
                    }
                }
                printer.println("\t*** CHANGE M ***");
            }
            printer.println("\t*** CHANGE N ***");
        }
        printer.close();
    }
}



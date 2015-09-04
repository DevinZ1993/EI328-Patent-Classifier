import java.util.*;
import java.io.*;


class MyPrinter {
    private PrintWriter out;

    public MyPrinter(String filepath) {
        try {
            out = new PrintWriter(filepath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public final void close() {
        if (out!=null) {
            out.close();
        }
    }
    public final void print(String str) {
        System.out.print(str);
        out.print(str);
    }
    public final void println(String str) {
        System.out.println(str);
        out.println(str);
    }
    public final void println() {
        System.out.println();
        out.println();
    }
}

class MyTimer {
    private long start, end;
    private boolean state;
    private MyPrinter printer;

    public MyTimer(MyPrinter printer) {
        this.printer = printer;
    }
    public final synchronized void start() {
        if (!state) {
            start = System.currentTimeMillis();
            state = true;
        }
    }
    public final synchronized void record(boolean isTraining) {
        if (state) {
            end = System.currentTimeMillis();
            state = false;
            long delt = end - start;
            if (isTraining) {
                printer.println("\tTraining: "+delt+" ms elapsed.");
            } else {
                printer.println("\tTesting: "+delt+" ms elapsed.");
            }
        }
    }
}

class SubProblem {
    private List<Integer> list;
    private int index;

    public SubProblem() {
        list = new ArrayList<Integer>();
    }
    public final void setIdx(int index) {
        this.index = index;
    }
    public final int getIdx() {
        return index;
    }
    public final void add(int k) {
        list.add(k);
    }
    public final int get(int idx) {
        return list.get(idx);
    }
    public final int size() {
        return list.size();
    }
}

class TestResult {
    public final int[] array;
    private int index;

    public TestResult(int size) {
        array = new int[size];
    }
    public final void setIdx(int index) {
        this.index = index;
    }
    public final int getIdx() {
        return index;
    }
}

public abstract class Implementor {
    protected MyPrinter printer;

    public Implementor(MyPrinter printer) {
        this.printer = printer;
    }
    public final void genStats(int[] res) {
        int truePos=0, trueNeg=0, falsePos=0, falseNeg=0;
        for (int i=0;i<res.length;i++) {
            if (getTestTag(i)>0) {
                if (res[i]>0) {
                    truePos++;
                } else {
                    falseNeg++;
                }
            } else {
                if (res[i]<=0) {
                    trueNeg++;
                } else {
                    falsePos++;
                }
            }  
        }
        recRes(truePos,trueNeg,falsePos,falseNeg);
    }
    private void recRes(int truePos,int trueNeg,int falsePos,int falseNeg) {
        int total = truePos+trueNeg+falsePos+falseNeg;
        double acc = (truePos+trueNeg+.0)/total;
        printer.println("\tacc\t= "+acc);
        double p = (truePos+.0)/(truePos+falsePos);
        double r = (truePos+.0)/(truePos+falseNeg);
        double f1 = 2*p*r/(p+r);
        printer.println("\tF1\t= "+f1);
        double tpr = (truePos+.0)/(truePos+falseNeg);
        double fpr = (falsePos+.0)/(falsePos+trueNeg);
        printer.println("\tTPR\t= "+tpr);
        printer.println("\tFPR\t= "+fpr);
    }

    public abstract void train(SubProblem sub);
    public abstract void setThr(double threshold);
    public abstract void clear();
    public abstract TestResult test();
    public abstract void sepData(List<Integer> posData,List<Integer> negData);
    public abstract int  testSize();
    protected abstract int getTestTag(int idx);
    
}


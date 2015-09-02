import java.util.*;
import java.io.*;


class MyPrinter {
    private FileWriter rout;
    private PrintWriter out;

    public MyPrinter(String resultPath) {
        try {
            out = new PrintWriter(resultPath);
            rout = new FileWriter("./data/roc.out");
            rout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public final synchronized void print(String str) {
            System.out.print(str);
            out.print(str);
    }
    public final void println(String str) {
        print(str+"\n");
    }
    public final void println() {
        print("\n");
    }
    public final void close() {
        out.close();
    }

    public final void rocRecord(double fpr,double tpr) {
        try {
            rout = new FileWriter("./data/roc.out",true);
            rout.write(fpr+"\t"+tpr+"\n");
            rout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public final void rocOver() {
        try {
            rout = new FileWriter("./data/roc.out",true);
            rout.write("_over_");
            rout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public final void rocExit() {
        try {
            rout = new FileWriter("./data/roc.out",true);
            rout.write("_exit_");
            rout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class MyTimer {
    private long start, end;
    private MyPrinter printer;
    private boolean state;

    public MyTimer(MyPrinter printer) {
        this.printer = printer;
    }
    public final synchronized void start() {
        if (!state) {
            start = System.currentTimeMillis();
            state = true;
        }
    }
    public final synchronized void record(boolean train) {
        if (state) {
            end = System.currentTimeMillis();
            state = false;
            long delt = end - start;
            if (train) {
                printer.println("\tTraining:\t"+delt+"ms elapsed.");
            } else {
                printer.println("\tTesting:\t"+delt+"ms elapsed.");
            }
        }
    }
}

public abstract class Implementor {
    private MyPrinter printer;

    public Implementor(MyPrinter printer) {
        this.printer = printer;
    }
    public final void test() {
        int[] res = doTest();
        int truePos=0, trueNeg=0, falsePos=0, falseNeg=0;
        for (int i=0;i<res.length;i++) {
            if (getTestResult(i)>0) {
                if (res[i]>0) {
                    truePos++;
                } else {
                    falseNeg++;
                }
            } else {
                if (res[i]<0) {
                    trueNeg++;
                } else {
                    falsePos++;
                }
            }   
        }
        recStats(truePos,trueNeg,falsePos,falseNeg);
    }
    private void recStatc(int truePos,int trueNeg,int falsePos,int falseNeg) {
        int total = truePos+trueNeg+falsePos+falseNeg;
        double acc = (truePos+trueNeg+.0)/total;
        printer.println("\tacc\t= "+acc);
        double p = (truePos+.0)/(truePos+falsePos);
        double r = (truePos+.0)/(truePos+falseNeg);
        double f1 = 2*p*r/(p+r);
        printer.println("\tF1\t= "+f1);
        double tpr = (truePos+.0)/(truePos+falseNeg);
        double fpr = (falsePos+.0)/(falsePos+trueNeg);
        printer.rocRecord(fpr,tpr);
        printer.println("\tTPR\t= "+tpr);
        printer.println("\tFPR\t= "+fpr);
        printer.println();
    }

    public abstract void setProblem(Collection<Integer> subset);
    public abstract void train();
    public abstract void setThreshold(double threshold);
    protected abstract int[] doTest();
    protected abstract int getTestResult(int idx);
}


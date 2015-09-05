import java.util.*;
import java.io.*;


public class BasicTask extends AbstractTask {
    private BasicTask(String arg) {
        super(arg);
    }
    private void work(double t) {
        printer.println();
        printer.println();
        printer.println("Thr = "+t);
        timer.start();
        implementor.train(null);
        timer.record(true);
        implementor.staticSetThr(t);
        timer.start();
        int[] res = implementor.test().array;
        implementor.staticGenStats(res);
        timer.record(false);
        implementor.staticClear();
    }

    /** Static Section: */

    private static BasicTask task;

    public static void main(String[] args) {
        printer = new MyPrinter("./data/Basic.log");
        timer = new MyTimer(printer);
        double tmin = Double.parseDouble(args[1]);
        double tmax = Double.parseDouble(args[2]);
        double tstep = Double.parseDouble(args[3]);
        for (double t=tmin;t<tmax+tstep/2;t+=tstep) {
            (new BasicTask(args[0])).work(t);
        }
        printer.println("\t*** CHANGE M ***");
        printer.close();
    }
}



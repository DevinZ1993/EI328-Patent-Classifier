import java.util.*;
import java.io.*;


public class BasicTask extends AbstractTask {
    private BasicTask(Implementor impl) {
        super(impl,"BasicTask");
    }
    protected void mainThread(double threshold) {
        printer.println("Threshold:\t"+threshold);
        timer.start();
        implementor.train();
        timer.record(true);
        implementor.setThreshold(threshold);
        timer.start();
        implementor.test();
        timer.record(false);
    }

    public static void main(String[] args) {
        Implementor implementor = null;
        MyPrinter printer = new MyPrinter("./data/Basic.out");
        if (args[0].equals("--Liblinear")) {
            implementor = new LiblinAdapter(printer);
        } else if (args[0].equals("--DIY")) {
            implementor = new MyImplementor(printer);
        } else {
            throw new IllegalArgumentException();
        }
        BasicTask task = new BasicTask(implementor);
        double tmin = Double.parseDouble(args[1]);
        double tmax = Double.parseDouble(args[2]);
        double tstep = Double.parseDouble(args[3]);
        task.forEachThreshold(tmin,tmax,tstep);
        task.release();
    }
}



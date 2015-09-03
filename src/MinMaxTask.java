import java.util.*;
import java.io.*;


public class MinMaxTask extends AbstractTask {
    private MinMaxTask(Implementor impl) {
        super(impl);
    }
    protected void mainThread(double threshold) {
        timer.start();
        implementor.train();
        timer.record(true);
        implementor.setThreshold(threshold);
        timer.start();
        implementor.test();
        timer.record(false);
    }

    public static void main(String[] args) {
        MyPrinter printer = new MyPrinter("./data/Basic.out");
        Implementor implementor = null;
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



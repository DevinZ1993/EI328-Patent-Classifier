import java.util.*;
import java.io.*;


public abstract class AbstractTask {
    protected Implementor implementor;
    protected MyPrinter printer;
    protected MyTimer timer;

    protected AbstractTask(Implementor implementor,String taskName) {
        this.implementor = implementor;
        implementor.read();
        printer = new MyPrinter("./data/"+taskName+".out");
        timer = new MyTimer(printer);
    }
    protected void release() {
        printer.rocExit();
        printer.close();
    }
    protected void forEachThreshold(double tmin,double tmax,double tstep) {
        for (double t=tmin;t<tmax+tstep/2;t+=tstep) {
            mainThread(t);
        }
        printer.rocOver();
    }

    protected abstract void mainThread(double threshold);

    static {
        if (!(new File("./data/train.in")).exists()) {
            procInputFile("./data/train","");
        }
        if (!(new File("./data/test.in")).exists()) {
            procInputFile("./data/test","5001:1.00");
        }
    }

    private static void procInputFile(String filename,String lineTail) {
        System.out.println("Generating "+filename+".in, please wait a minute.");
        try {
            Scanner fin = new Scanner(new FileInputStream(filename+".txt"));
            PrintWriter fout = new PrintWriter(new FileOutputStream(filename+".in"));
            int cnt=0, total=0;
            while (fin.hasNextLine()) {
                StringTokenizer tok = new StringTokenizer(fin.nextLine());
                if (tok.nextToken().charAt(0)=='A') {
                    fout.print("1 ");
                    cnt = 0;
                } else {
                    fout.print("-1 ");
                }
                while (tok.hasMoreTokens()) {
                    fout.print(tok.nextToken()+" ");
                }
                fout.println(lineTail);
                total++;
            }
            System.out.println(filename+".in:\t"+cnt+"/"+total);
            fout.close();
            fin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("\tDone.");
    }
    
}


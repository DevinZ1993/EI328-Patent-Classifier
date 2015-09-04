import java.util.*;
import java.io.*;


public abstract class AbstractTask extends Thread {
    protected Implementor implementor;

    protected AbstractTask(String arg) {
        if (arg.equals("--Liblinear")) {
            implementor = new LiblinAdapter(printer);
        } else if (arg.equals("--DIY")) {
            implementor = new MyImplementor(printer);
        } else {
            throw new IllegalArgumentException();
        }
    }

    /** Static Section: */

    protected static MyPrinter printer;
    protected static MyTimer timer;

    static {
        if (!(new File("./data/train.in")).exists()) {
            procInputFile("./data/train");
        }
        if (!(new File("./data/test.in")).exists()) {
            procInputFile("./data/test");
        }
    }
    private static void procInputFile(String filename) {
        System.out.println("Generating "+filename+".in, please wait a minute.");
        try {
            Scanner fin = new Scanner(new FileInputStream(filename+".txt"));
            PrintWriter fout = new PrintWriter(new FileOutputStream(filename+".in"));
            int cnt=0, total=0;
            while (fin.hasNextLine()) {
                StringTokenizer tok = new StringTokenizer(fin.nextLine());
                if (tok.nextToken().charAt(0)=='A') {
                    fout.print("1 ");
                    cnt++;
                } else {
                    fout.print("0 ");
                }
                while (tok.hasMoreTokens()) {
                    fout.print(tok.nextToken()+" ");
                }
                fout.println();
                total++;
            }
            System.out.println(filename+".in:     "+cnt+"/"+total);
            fout.close();
            fin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("\tDone.");
    }
}


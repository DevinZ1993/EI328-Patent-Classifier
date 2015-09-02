import de.bwaldvogel.liblinear.*;
import java.util.*;
import java.io.*;


public class LiblinAdapter extends Implementor {
    private static Problem train, test;
    
    static {
        try {
            train = Train.readProblem(new File("./data/train.in"),1);
            test = Train.readProblem(new File("./data/test.in"),1);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidInputDataException e) {
            e.printStackTrace();
        }
    }

    private Parameter param;
    private Problem myTrain;
    private Model model;

    public LiblinAdapter(MyPrinter printer) {
        super(printer);
        myTrain = train;
    }
    public final void setProblem(Collection<Integer> subset) {
        myTrain = new Problem();
        myTrain.l = subset.size();
        myTrain.n = train.n;
        for (int i=0;i<subset.size();i++) {
            int idx = subset.get(i);
            myTrain.x[i] = train.x[idx];
            myTrain.y[i] = train.y[idx];
        }
    }
    public final void train() {
        model = Linear.train(myTrain,param);
    }
    public final void setThreshold(double threshold) {
        try {
            model.save(new File("./data/old_model.txt"));
            BufferedReader fin = new BufferedReader(new FileReader("./data/old_model.txt"));
            PrintWriter fout = new PrintWriter(new FileWriter("./data/new_model.txt"));
            for (int i=0;i<3;i++) {
                fout.println(fin.readLine());
            }
            StringTokenizer tok = new StringTokenizer(fin.readLine());
            fout.print(tok.nextToken()+" ");
            int num = Integer.parseInt(tok.nextToken())+1;
            fout.println(num);
            for (int i=0;i<num+1;i++) {
                fout.println(fin.readLine());
            }
            fout.println(-threshold+" ");
            fout.println(fin.readLine());
            fout.close();
            fin.close();
            model = Model.load(new File("./data/new_model.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    protected final int[] doTest() {
        int[] res = new int[test.l];
        Arrays.fill(res,-1);
        for (int n=0;n<test.l;n++) {
            if (Linear.predict(model,test.x[n])>0) {
                res[n] = 1;
            }
        }
        return res;
    }
    protected final int getTestResult(int idx) {
        if (idx<0 || idx>=test.l) {
            throw new IllegalArgumentException();
        }
        return (test.y[idx]>0)? 1:-1;
    }
}


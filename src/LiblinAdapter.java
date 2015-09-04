import de.bwaldvogel.liblinear.*;
import java.util.concurrent.*;
import java.util.*;
import java.io.*;


public class LiblinAdapter extends Implementor {
    public LiblinAdapter(MyPrinter printer) {
        super(printer);
    }
    public final void train(SubProblem sub) {
        Problem myTrain = train;
        int idx = -1;
        if (sub!=null) {
            idx = sub.getIdx();
            myTrain = new Problem();
            myTrain.l = sub.size();
            myTrain.n = train.n;
            for (int i=0;i<sub.size();i++) {
                int j = sub.get(i);
                myTrain.x[i] = train.x[j];
                myTrain.y[i] = train.y[j];
            }
        }
        try {
            Model model = Linear.train(myTrain,new Parameter
                    (SolverType.L2R_L2LOSS_SVC_DUAL,1.0,0.01));
            models.put(model);
            idxMap.put(model,idx);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public final TestResult test() {
        try {
            Model model = models.take();
            if (model==DUMMY) {
                models.put(model);
            } else {
                TestResult res = new TestResult(test.l);
                for (int n=0;n<test.l;n++) {
                    if (Linear.predict(model,test.x[n])>0) {
                        res.array[n] = 1;
                    }
                }
                res.setIdx(idxMap.get(model));
                return res;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
    public final synchronized void setThr(double threshold) {
        try {
            int numOfModel = models.size();
            for (int n=0;n<numOfModel;n++) {
                Model model = models.take();
                int idx = idxMap.remove(model);
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
                models.put(model);
                idxMap.put(model,idx);
            }
            models.put(DUMMY);
            idxMap.put(DUMMY,-1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public final synchronized void clear() {
        models.clear();
        idxMap.clear();
    }
    public final void sepData(List<Integer> posData,List<Integer> negData) {
        for (int i=0;i<train.l;i++) {
            if (train.y[i]>0) {
                posData.add(i);
            } else {
                negData.add(i);
            }
        }
    }
    protected final int getTestTag(int idx) {
        if (idx<0 || idx>=test.l) {
            throw new IllegalArgumentException();
        }
        return (test.y[idx]>0)? 1:-1;
    }

    /** Static Section: */

    private static Problem train, test;
    private static BlockingQueue<Model> models;
    private static final Model DUMMY = new Model();
    private static Map<Model,Integer> idxMap;
    
    static {
        try {
            System.out.println("Reading data files, please wait a minute.");
            train = Train.readProblem(new File("./data/train.in"),1);
            test = Train.readProblem(new File("./data/test.in"),1);
            models = new LinkedBlockingQueue<Model>();
            idxMap = new HashMap<Model,Integer>();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidInputDataException e) {
            e.printStackTrace();
        }
    }
}


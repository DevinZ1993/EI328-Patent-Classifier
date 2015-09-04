import java.util.concurrent.*;
import java.util.*;
import java.io.*;


class Term {
    public int index;
    public double value;

    public Term(int index,double value) {
        this.index = index;
        this.value = value;
    }
}

class MyProblem {
    public List<List<Term>> x;
    public List<Integer> y;
    public final int dim;

    public MyProblem(int dim) {
        x = new ArrayList<List<Term>>();
        y = new ArrayList<Integer>();
        this.dim = dim;
    }
    public final void add(List<Term> coef,int val) {
        x.add(coef);
        y.add(val);
    }
    public MyProblem(String filepath) {
        x = new ArrayList<List<Term>>();
        y = new ArrayList<Integer>();
        int  maxIdx = 0;
        try {
            Scanner in = new Scanner(new File(filepath));
            while (in.hasNextLine()) {
                List<Term> tmp = new ArrayList<Term>();
                StringTokenizer tok = new StringTokenizer(in.nextLine());
                y.add(Integer.valueOf(tok.nextToken()));
                while (tok.hasMoreTokens()) {
                    StringTokenizer tok1 = new StringTokenizer(tok.nextToken(),":");
                    int idx = Integer.parseInt(tok1.nextToken());
                    double val = Double.parseDouble(tok1.nextToken());
                    tmp.add(new Term(idx,val));
                    maxIdx = (idx>maxIdx)? idx:maxIdx;
                }
                x.add(tmp);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            dim = maxIdx;
        }
    }
    public final int getY(int idx) {
        if (idx<0 || idx>=y.size()) {
            throw new IllegalArgumentException();
        }
        return y.get(idx);
    }
    public final int size() {
        return x.size();
    }
}

class MyModel {
    private MyProblem train;
    private final int dim;
    private double[] weights;

    private MyModel() {
        dim = 0;
    }
    private MyModel(MyProblem train) {
        this.train = train;
        dim = train.dim;
        weights = new double[dim+1];
        for (int n=0;n<1000;n++) {
            double[] grad = calGradient();
            double norm = getNorm(grad);
            if (n%20==0) {
                System.out.println("Loop "+n+": \tnorm(grad) = "+norm);
            }
            //double step = optStep(grad,norm);
            double step = norm;
            for (int i=0;i<=dim;i++) {
                weights[i] -= step*grad[i];
            }
        } 
    }
    public final int predict(List<Term> x) {
        return (sigmoid(x)>0.5)? 1:-1;
    }
    public final void addThr(double threshold) {
        weights[0] -= threshold;
    }
    private double[] calGradient() {
        double[] grad = new double[dim+1];
        for (int i=0;i<=dim;i++) {
            grad[i] = weights[i]*LAMBDA;
        }
        for (int n=0;n<train.size();n++) {
            List<Term> x = train.x.get(n);
            double delt = sigmoid(x)-(train.getY(n)+1)/2;
            grad[0] += delt;
            for (Term term: x) {
                grad[term.index] += term.value*delt;
            }
        }
        return grad;
    }
    private double sigmoid(List<Term> x) {
        double val = weights[0];
        for (Term term: x) {
            if (term.index<=dim) {
                val += weights[term.index]*term.value;
            }
        }
        return 1./(1+Math.exp(-val));
    }
    private double getNorm(double[] grad) {
        double val = 0;
        for (int i=1;i<grad.length;i++) {
            val += grad[i]*grad[i];
        }
        return Math.sqrt(val);
    }

    private static final double LAMBDA = 0;
    private static Object classLock = MyModel.class;
    private static MyModel DUMMY;

    public static MyModel train(MyProblem train) {
        return new MyModel(train);
    }
    public static MyModel getDummy() {
        if (DUMMY==null) {
            synchronized(classLock) {
                if (DUMMY==null) {
                    DUMMY = new MyModel();
                }
            }
        }
        return DUMMY;
    }
}

public class MyImplementor extends Implementor {
    public MyImplementor(MyPrinter printer) {
        super(printer);
    }
    public final void train(SubProblem sub) {
        MyProblem myTrain = train;
        int idx = -1;
        if (sub!=null) {
            idx = sub.getIdx();
            myTrain = new MyProblem(train.dim);
            for (int i=0;i<sub.size();i++) {
                int j = sub.get(i);
                myTrain.add(train.x.get(j),train.y.get(j));
            }
        }
        try {
            MyModel model = MyModel.train(myTrain);
            models.put(model);
            idxMap.put(model,idx);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public final TestResult test() {
        try {
            MyModel model = models.take();
            if (model==DUMMY) {
                models.put(model);
            } else {
                TestResult res = new TestResult(test.size());
                for (int n=0;n<test.size();n++) {
                    if (model.predict(test.x.get(n))>0) {
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
        for (MyModel model: models) {
            model.addThr(threshold);
        }
        models.add(DUMMY);
        idxMap.put(DUMMY,-1);
    }
    public final synchronized void clear() {
        models.clear();
        idxMap.clear();
    }
    public final void sepData(List<Integer> posData,List<Integer> negData) {
        for (int i=0;i<train.size();i++) {
            if (train.getY(i)>0) {
                posData.add(i);
            } else {
                negData.add(i);
            }
        }
    }
    protected final int getTestTag(int idx) {
        if (idx<0 || idx>=test.size()) {
            throw new IllegalArgumentException();
        }
        return (test.getY(idx)>0)? 1:-1;
    }

    /** Static Section: */

    private static MyProblem train, test;
    private static BlockingQueue<MyModel> models;
    private static final MyModel DUMMY = MyModel.getDummy();
    private static Map<MyModel,Integer> idxMap;
    
    static {
        System.out.println("Reading data files, please wait a minute.");
        train = new MyProblem("./data/train.in");
        test = new MyProblem("./data/test.in");
        models = new LinkedBlockingQueue<MyModel>();
        idxMap = new HashMap<MyModel,Integer>();
    }
}


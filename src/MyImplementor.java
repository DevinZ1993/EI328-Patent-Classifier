import java.util.*;
import java.io.*;


class MyProblem {
    public List<Map<Integer,Double>> x;
    public List<Integer> y;
    public final int dim;

    public MyProblem(int dim) {
        x = new ArrayList<Map<Integer,Double>>();
        y = new ArrayList<Integer>();
        this.dim = dim;
    }
    public final void add(Map<Integer,Double> coef,int val) {
        x.add(coef);
        y.add(val);
    }
    public MyProblem(String filepath) {
        x = new ArrayList<Map<Integer,Double>>();
        y = new ArrayList<Integer>();
        try {
            Scanner in = new Scanner(new File(filepath));
            while (in.hasNextLine()) {
                Map<Integer,Double> tmp = new HashMap<Integer,Double>();
                StringTokenizer tok = new StringTokenizer(in.nextLine());
                y.add(Integer.valueOf(tok.nextToken()));
                while (tok.hasMoreTokens()) {
                    StringTokenizer tok1 = new StringTokenizer(tok.nextToken(),":");
                    int idx = Integer.parseInt(tok1.nextToken());
                    double val = Double.parseDouble(tok1.nextToken());
                    tmp.put(idx,val);
                    dim = (idx>dim)? idx:dim;
                }
                x.add(tmp);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public final void setThreshold(double threshold) {
        this.threshold = threshold;
    }
    public final int getY(int idx) {
        if (idx<0 || idx>=l) {
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

    private Model(MyProblem train) {
        this.train = train;
        dim = train.dim;
        weights = new double[dim+1];
        for (int n=0;n<1000;n++) {
            double[] grad = calGradient();
            double norm = getNorm(grad);
            if (n%20==0) {
                System.out.println("Loop "+n+": norm(grad) = "+norm);
            }
            //double step = optStep(grad,norm);
            double step = norm;
            for (int i=0;i<=dim;i++) {
                weights[i] -= step*grad[i];
            }
        } 
    }
    public final int predict(Map<Integer,Double> x) {
        return (sigmoid(x)>0.5)? 1:-1;
    }
    public final void addThreshold(double threshold) {
        weights[0] -= threshold;
    }
    private double[] calGradient() {
        double[] grad = new double[dim+1];
        for (int i=0;i<=dim;i++) {
            grad[i] *= weights[i]*LAMBDA;
        }
        for (int n=0;n<train.size();n++) {
            Map<Integer,Double> x = train.x.get(n);
            double delt = sigmoid(x)-(train.getY(n)+1)/2;
            grad[0] += key;
            for (Integer key: x.keySet()) {
                grad[key] += x.get(key)*delt;
            }
        }
    }
    private double sigmoid(Map<Integer,Double> x) {
        double val = weights[0];
        for (Integer key: x.keySet()) {
            val += weights[key]*x.get(key);
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

    public static Model train(MyProblem train) {
        model = new MyModel(train);
    }
}

public class MyImplementor extends Implementor {
    private static MyProblem train, test;
    
    static {
        train = new MyProblem("./data/train.in");
        test = new MyProblem("./data/test.in");
    }

    private MyProblem myTrain;
    private MyModel model;

    public MyImplementor(MyPrinter printer) {
        super(printer);
        myTrain = train;
    }
    public final void setProblem(Collection<Integer> subset) {
        myTrain = new MyProblem(train.dim);
        for (Integer idx: subset) {
            myTrain.add(train.x.get(idx),train.y.get(idx));
        }
    }
    public final void train() {
        model = MyModel.train(myTrain);
    }
    public final void setThreshold(double threshold) {
        if (model!=null) {
            model.addThreshold(threshold);
        }
    }
    protected final int[] doTest {
        int[] res = new int[test.size()];
        Arrays.fill(res,-1);
        for (int n=0;n<test.size();n++) {
            if (model.predict(test.x[n])>0) {
                res[n] = 1;
            }
        }
        return res;
    }
    protected final int getTestResult(int idx) {
        if (idx<0 || idx>=test.l) {
            throw new IllegalArgumentException();
        }
        return (test.getY(idx)>0)? 1:-1;
    }
}


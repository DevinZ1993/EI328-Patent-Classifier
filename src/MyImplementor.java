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
    public final void addThreshold(double threshold) {
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

    public static MyModel train(MyProblem train) {
        return new MyModel(train);
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
    public final void setProblem(List<Integer> subset) {
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
    protected final int[] doTest() {
        int[] res = new int[test.size()];
        Arrays.fill(res,-1);
        for (int n=0;n<test.size();n++) {
            if (model.predict(test.x.get(n))>0) {
                res[n] = 1;
            }
        }
        return res;
    }
    protected final int getTestResult(int idx) {
        if (idx<0 || idx>=test.size()) {
            throw new IllegalArgumentException();
        }
        return (test.getY(idx)>0)? 1:-1;
    }
}


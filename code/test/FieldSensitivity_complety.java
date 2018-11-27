package test;
import benchmark.internal.Benchmark;
import benchmark.objects.A;
import benchmark.objects.B;


public class FieldSensitivity_complety{
    public FieldSensitivity_complety() {}

    private void assign(A x, A y) {y.f = x.f;}

   

    public static void main(String[] args) {
        FieldSensitivity_complety fs2 = new FieldSensitivity_complety();
        //fs2.test();
        boolean s = true;
        Benchmark.alloc(1);
        B b = new B();
        Benchmark.alloc(2);
        A a = new A(b);
        Benchmark.alloc(3);
        A c = new A();
        Benchmark.alloc(4);
        B e = new B();
        if(args.length > 1) {
            c.f = e;
            c.g = b;
        }else{
            fs2.assign(a, c);
            c.g = e;
        }
        B d = c.getF();
        b = c.g;
        Benchmark.test(1, d);
        Benchmark.test(2, b);
    }
}

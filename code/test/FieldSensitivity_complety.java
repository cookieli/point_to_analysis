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
        Benchmark.alloc(10);
        B[] array = new B[4];
        Benchmark.alloc(1);
        B b = new B();
        Benchmark.alloc(2);
        A a = new A(b);
        Benchmark.alloc(3);
        A c = new A();
        Benchmark.alloc(4);
        B e = new B();
        Benchmark.alloc(5);
        B f = new B();
        if(args.length > 1) {
            c.putF(e);
            c.g = b;
        }else{
            fs2.assign(a, c);
            c.g = e;
        }
        B d = c.getF();
        b = c.g;
        array[0] = b;
        array[1] = e;
        array[2] = d;
        array[3] = f;
        Benchmark.test(1, d);
        Benchmark.test(2, b);
        Benchmark.test(3, array);
    }
}

package ikayaki.core;

public class JNITest {

    static {
        System.loadLibrary("PAcquireJNI");
    }

    public static native void helloC();

    public static native double getDouble();

    public static native void getArray(double[] arr);

    public static void helloJava() {
        System.out.println("Hello Java!");
    }

    public static void main(String[] args) {
        helloC();

        System.out.println("getDouble: " + getDouble());

        double[] arr = new double[5];
        getArray(arr);
        System.out.println("getArray:");
        for (double d : arr) {
            System.out.println(d);
        }
    }
}

import java.util.Random;

public class test {
    public static void main(String[] args) {
        Random random=new Random();
        System.out.println(random.doubles());
        System.out.println(random.ints());
        System.out.println(random.longs());
        System.out.println(random.nextInt());
        System.out.println(random.nextInt(1000));

    }
}

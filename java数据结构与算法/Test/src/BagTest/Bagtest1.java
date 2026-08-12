package BagTest;

public class Bagtest1 {
    public static void main(String[] args) {
        Bag<Integer> b = new Bag<Integer>();
        b.add(1);
        b.add(2);
        b.add(3);
        System.out.println(b.size());
        for (Object o : b) {
            System.out.println(o);
        }
        System.out.println(b.isEmpty());
        System.out.println(b.size());


    }
}

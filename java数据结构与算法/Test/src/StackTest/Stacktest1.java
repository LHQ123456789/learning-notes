package StackTest;

public class Stacktest1 {
    public static void main(String[] args) {
        Stack<Integer> s = new Stack<Integer>();
        s.push(1);
        s.push(2);
        s.push(3);
        System.out.println(s.size());
        for (Object o : s) {
            System.out.println(o);
        }
        System.out.println(s.isEmpty());
        System.out.println(s.size());
        System.out.println(s.pop());
        System.out.println(s.pop());


        System.out.println(s.peek());




    }
}

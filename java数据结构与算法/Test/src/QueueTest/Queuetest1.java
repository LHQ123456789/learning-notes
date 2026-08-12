package QueueTest;

public class Queuetest1 {
    public static void main(String[] args) {
        Queue<Integer> q = new Queue<Integer>();
        q.enqueue(1);
        q.enqueue(2);
        q.enqueue(3);
        System.out.println(q.size());
        for (Object o : q) {
            System.out.println(o);
        }
        System.out.println(q.isEmpty());
        System.out.println(q.size());
        System.out.println(q.dequeue());
        System.out.println(q.dequeue());
    }
}

package org.apache.tomcat;

public class Concurrency001 {

    public static final boolean[] DATA = new boolean[1];
    public static final Object LOCK = new Object();
    public static boolean run = true;

    public static void main(String[] args) throws InterruptedException {

        ThreadFlip threadA = new ThreadFlip();
        threadA.start();

        Thread.sleep(500);

        ThreadFlip threadB = new ThreadFlip();
        threadB.start();

        threadA.join();
        threadB.join();
    }


    public static class ThreadFlip extends Thread {

        private int count = 0;
        private String failureMode = "";

        @Override
        public void run() {
            try {
                boolean localData;
                while (run) {
                    count ++;
                    DATA[0] = !DATA[0];
                    localData = DATA[0];
                    synchronized (LOCK) {
                        LOCK.notify();
                        LOCK.wait();
                        // Should have been flipped
                        if (localData == DATA[0]) {
                            run = false;
                            failureMode = "No flip";
                        }
                    }
                }
            } catch (InterruptedException e) {
                failureMode = "Interrupted";
            }

            System.out.println("ThreadFlip ended after [" + count + "] iterations with message [" + failureMode + "]");
        }
    }
}

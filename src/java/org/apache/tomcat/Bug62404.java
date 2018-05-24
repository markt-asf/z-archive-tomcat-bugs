package org.apache.tomcat;


public class Bug62404 {

    public static void main(String[] args) throws Exception {
        Bean bean = new Bean();

        Thread watcher = new Thread(new Watcher(bean));
        watcher.start();

        Thread editor = new Thread(new Editor(bean));
        editor.start();
    }


    private static class Editor implements Runnable {

        private final Bean bean;

        public Editor(Bean bean) {
            this.bean = bean;
        }

        @Override
        public void run() {
            // Give both threads a chance to start up
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // Ignore
            }

            System.out.println("Editor set value at   " + System.nanoTime());
            bean.setFlag(true);
        }
    }


    private static class Watcher implements Runnable {

        private final Bean bean;

        private final Object lock = new Object();

        public Watcher(Bean bean) {
            this.bean = bean;
        }

        @Override
        public void run() {
            System.out.println("Watcher started at " + System.nanoTime());

            while (true) {
                synchronized (lock) {
                    if (bean.getFlag()) {
                        System.out.println("Watcher saw change at " + System.nanoTime());
                        return;
                    } else {
                        System.out.println("Watcher saw same at   " + System.nanoTime());
                    }
                }
            }
        }
    }

    private static class Bean {
        private boolean flag = false;


        public boolean getFlag() {
            return flag;
        }


        public void setFlag(boolean flag) {
            this.flag = flag;
        }
    }
}

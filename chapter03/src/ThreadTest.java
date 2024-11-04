public class ThreadTest {
    public static void main(String[] args) {
        // 创建并启动线程
        Thread thread01 = new Thread(new Handle(), "Thread01");
        thread01.start();
        Thread thread02 = new Thread(new Handle(), "Thread02");
        thread02.start();
    }

    // 将 Handle 类定义为静态内部类（或外部类），以避免潜在的访问问题
    public static class Handle implements Runnable {
        @Override
        public void run() {
            try {
                System.out.println(Thread.currentThread().getName() + "进入");
                Thread.sleep(100);
                System.out.println(Thread.currentThread().getName() + "退出");
            } catch (Exception e) {
                System.out.println(e.getStackTrace());
            }

        }
    }
}
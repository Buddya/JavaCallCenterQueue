import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;


public class Main {
    private final static int CALLS_AMOUNT = 60;
    private final static int FREQ_OF_CALLS = 1000;
    private static final int PHONE_NUMBER_LENGTH = 11;
    private static final int TIME_OF_PROCESSING = 3000;
    private static final int OPERATORS_AMOUNT = 5;
    private static final int FREQ_OF_CHECK = 500;

    private static volatile boolean callsRemaining;

    public static void main(String[] args) throws InterruptedException {
        final Queue<Call> calls = new LinkedBlockingQueue<>();

        List<Thread> operatorsList = new ArrayList<>();
        for (int i = 1; i <= OPERATORS_AMOUNT; i++) {
            Thread operator = new Thread(acceptCall(calls));
            operatorsList.add(operator);
            operator.start();
        }

        Thread threadATC = new Thread(getATC(calls));
        threadATC.start();

        threadATC.join();
        operatorsList.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        System.out.println("Time to close");
    }

    //BUSY WAITING
    private static Runnable acceptCall(Queue<Call> calls) {
        return () -> {
            while (true) {
                Call call = calls.poll();
                try {
                    if (call == null) {
                        if (callsRemaining) {
                            System.out.println("Очередь пуста");
                            return;
                        }
                        Thread.sleep(FREQ_OF_CHECK);
                    } else {
                        System.out.printf("%s обрабатывает звонок от абонента: %s\n",
                                Thread.currentThread().getName(),
                                call.getPhoneNumber());
                        Thread.sleep(TIME_OF_PROCESSING);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private static Runnable getATC(Queue<Call> calls) {
        return () -> {
            for (int i = 0; i < CALLS_AMOUNT; i++) {
                Call call = new Call(getRandomPhone());
                if (calls.offer(call)) {
                    System.out.printf("Звонок от %s добавлен в очередь\n", call.getPhoneNumber());
                }
                try {
                    if (i % 2 != 0) {
                        Thread.sleep(FREQ_OF_CALLS);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            callsRemaining = true;
        };
    }

    private static String getRandomPhone() {
        String s = "123456789";
        StringBuffer phoneNumber = new StringBuffer();

        for (int i = 0; i < PHONE_NUMBER_LENGTH; i++) {
            phoneNumber.append(s.charAt(new Random().nextInt(s.length())));
        }
        return phoneNumber.toString();
    }
}

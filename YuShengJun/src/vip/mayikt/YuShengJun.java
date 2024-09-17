package vip.mayikt;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

// This is a simple Java class designed to simulate HTTP requests for testing purposes.
public class YuShengJun {
    // Default IP used when no other IP is specified.
    private static final String DEFAULT_IP = "127.0.0.1";

    // The mode of the HTTP request (GET or POST).
    private HTTPRequestMode RequestMode = HTTPRequestMode.GET;

    // The mode for generating fake IPs.
    private FakeIPMode fakeIPMode = FakeIPMode.Default;

    // Number of threads to be used in the test.
    private int ThreadNum = Runtime.getRuntime().availableProcessors();

    // The target IP address for the requests.
    private String TargetIP;

    // The fake IP to use if not using the default behavior.
    private String FakeIP = DEFAULT_IP;

    // The service used to manage the threads.
    private ExecutorService executorService;

    // The size of the data to send in POST requests.
    private int dataSize;

    // Flag indicating whether to display logs during execution.
    private boolean showlog = false;

    // Counter for tracking the number of requests sent.
    private AtomicLong count = new AtomicLong(0);

    /**
     * Generates a random byte array of specified size in kilobytes.
     * @return A byte array containing random data.
     */
    public byte[] generateRandomData() {
        Random random = new Random();
        byte[] data = new byte[dataSize * 1024];
        for (int i = 0; i < dataSize * 1024; i++) {
            data[i] = (byte) random.nextInt(256);
        }
        return data;
    }

    /**
     * Generates a random IP address.
     * @return A string representing a randomly generated IP address.
     */
    public String generateRandomIP() {
        Random random = new Random();
        int[] ip = new int[4];
        for (int i = 0; i < 4; i++) {
            ip[i] = random.nextInt(256);
        }
        return ip[0] + "." + ip[1] + "." + ip[2] + "." + ip[3];
    }

    /**
     * Extracts the IP address from a given URL.
     * @param url The URL to extract the IP from.
     * @return The extracted IP address.
     */
    public String getIP(String url) {
        url = url.replace("http://", "").replace("https://", "");
        try {
            URI uri = new URI("http://" + url);
            InetAddress address = InetAddress.getByName(uri.getHost());
            return address.getHostAddress();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get IP from URL: " + url, e);
        }
    }

    /**
     * Configures the class for performing GET requests.
     * @param TargetIP The target IP address.
     * @param ThreadNum The number of threads to use.
     * @param fakeIPMode The mode for generating fake IPs.
     * @param FakeIP The specific fake IP to use.
     * @param showlog Whether to show logs.
     */
    public void setGET(String TargetIP, int ThreadNum, FakeIPMode fakeIPMode, String FakeIP, boolean showlog) {
        // Check if IP is in the correct format
        if (TargetIP.matches("^(http|https)://[\\\\w.-]+(:\\\\d+)?/?$")) {
            TargetIP = getIP(TargetIP);
        }
        TargetIP = TargetIP.replace("http://", "").replace("https://", "");
        this.RequestMode = HTTPRequestMode.GET;
        this.TargetIP = TargetIP;
        this.ThreadNum = ThreadNum;
        this.FakeIP = FakeIP;
        this.fakeIPMode = fakeIPMode;
        this.showlog = showlog;
    }

    /**
     * Configures the class for performing POST requests.
     * @param TargetIP The target IP address.
     * @param ThreadNum The number of threads to use.
     * @param fakeIPMode The mode for generating fake IPs.
     * @param FakeIP The specific fake IP to use.
     * @param dataSize The size of the data payload.
     * @param showlog Whether to show logs.
     */
    public void setPOST(String TargetIP, int ThreadNum, FakeIPMode fakeIPMode, String FakeIP, int dataSize, boolean showlog) {
        // Check if IP is in the correct format
        if (TargetIP.matches("^(http|https)://[\\\\w.-]+(:\\\\d+)?/?$")) {
            TargetIP = getIP(TargetIP);
        }
        TargetIP = TargetIP.replace("http://", "").replace("https://", "");
        this.RequestMode = HTTPRequestMode.POST;
        this.TargetIP = TargetIP;
        this.ThreadNum = ThreadNum;
        this.FakeIP = FakeIP;
        this.dataSize = dataSize;
        this.fakeIPMode = fakeIPMode;
        this.showlog = showlog;
    }

    /**
     * Starts the performance test by initiating multiple threads to send HTTP requests.
     */
    public void startTest() {
        try {
            count.set(0); // Reset the counter
            executorService = Executors.newFixedThreadPool(ThreadNum, new NamedThreadFactory());

            HttpRequest httpRequest = buildHttpRequest(TargetIP);

            for (int i = 0; i < ThreadNum; i++) {
                String threadIP = fakeIPMode == FakeIPMode.Random ? generateRandomIP() : FakeIP;
                executorService.submit(() -> {
                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            HttpRequest request = buildHttpRequest(threadIP);
                            if (showlog) {
                                HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.discarding())
                                        .thenAccept(_ -> {

                                        })
                                        .exceptionally(ex -> {
                                            System.err.println("Error sending request: " + ex.getMessage());
                                            return null;
                                        });
                            } else {
                                HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.discarding())
                                        .thenAccept(_ -> {

                                        })
                                        .exceptionally(_ -> null);
                            }
                            count.incrementAndGet(); // Atomically increment the counter
                            System.out.print("\rsend " + count.get());
                        } catch (Exception e) {
                            if (showlog) {
                                System.err.println("Error sending request: " + e.getMessage());
                            }
                        }
                    }
                });
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URI syntax", e);
        }
    }

    private HttpRequest buildHttpRequest(String ip) throws URISyntaxException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(new URI("http://" + TargetIP));
        switch (RequestMode) {
            case GET:
                builder.GET();
                break;
            case POST:
                builder.POST(HttpRequest.BodyPublishers.ofByteArray(generateRandomData()));
                break;
        }
        if (fakeIPMode != FakeIPMode.Default) {
            builder.header("X-Forwarded-For", ip);
        }
        return builder.build();
    }


    /**
     * Stops all running threads and shuts down the executor service.
     */
    public void stop() {
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Enum for specifying the type of HTTP request.
     */
    public enum HTTPRequestMode {
        GET,
        POST
    }

    /**
     * Enum for specifying the mode of fake IP generation.
     */
    public enum FakeIPMode {
        Random,
        Fixed,
        Default
    }

    /**
     * Factory for creating named threads.
     */
    private static class NamedThreadFactory implements ThreadFactory {
        private final AtomicInteger count = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "Thread-" + count.incrementAndGet());
            thread.setDaemon(true); // Set as daemon thread
            return thread;
        }
    }
}


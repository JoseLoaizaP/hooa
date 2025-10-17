import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import DBConfig.ConnectionManager;
import model.MainAccount;
import model.Pocket;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PocketServiceTest {
    private class Request {
        public String action;
        public Map<String, String> data;
    }

    private class Response {
        public String status;
        public JsonObject data;
    }

    private static Gson gson = new Gson();
    private static Connection connection;

    @BeforeClass
    public static void init() throws Exception {
        System.out.println("Starting test...");
        connection = ConnectionManager.getInstance("jdbc:h2:mem:pocket_manager_db", "sa", "").getConnection();
        new Thread(() -> {
            try {
                new Server().init(5000.0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        Thread.sleep(2000);
    }

    public Response addPocket(String name, double initialAmount, BufferedReader reader,
            BufferedWriter writer) throws Exception {
               Request request = new Request();
        request.action = "ADD_POCKET";
        request.data = Map.of("name", name, "initialAmount", String.valueOf(initialAmount));

        String jsonRequest = gson.toJson(request);
        writer.write(jsonRequest);
        writer.newLine();
        writer.flush();

        String line = reader.readLine();
        Response response = gson.fromJson(line, Response.class);
        return response;
    }

    @Test
    public void A_testAddPocket() throws Exception {
        Socket socket = new Socket("localhost", 5000);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        Response response = addPocket("Test Pocket", 1000.0, reader, writer);

        System.out.println(response);

        socket.close();
        assert response.status.equals("ok");
        Pocket pocket = gson.fromJson(response.data, Pocket.class);

        assert pocket.getName().equals("Test Pocket");
        assert pocket.getBalance() == 1000.0;
        assert pocket.getMainAccount() != null;
        assert pocket.getMainAccount().getAvailableBalance() == 4000.0;
        assert pocket.getMainAccount().getTotalBalance() == 5000.0;
    }

    public Response depositPocket(String name, double amount, BufferedReader reader, BufferedWriter writer)
            throws Exception {
        Request request = new Request();
        request.action = "DEPOSIT_POCKET";
        request.data = Map.of("name", name, "amount", String.valueOf(amount));

        String jsonRequest = gson.toJson(request);
        writer.write(jsonRequest);
        writer.newLine();
        writer.flush();

        String line = reader.readLine();
        Response response = gson.fromJson(line, Response.class);
        return response;
    }

    @Test
    public void B_testDepositPocket() throws Exception {
        Socket socket = new Socket("localhost", 5000);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        Response response = depositPocket("Test Pocket", 200, reader, writer);

        System.out.println(response);
        socket.close();
        assert response.status.equals("ok");
        Pocket pocket = gson.fromJson(response.data, Pocket.class);

        assert pocket.getName().equals("Test Pocket");
        assert pocket.getBalance() == 1200;
        assert pocket.getMainAccount() != null;
        assert pocket.getMainAccount().getAvailableBalance() == 3800;
        assert pocket.getMainAccount().getTotalBalance() == 5000;

    }

    @Test
    public void C_testWithdrawPocket() throws Exception {
        Socket socket = new Socket("localhost", 5000);

        Request request = new Request();
        request.action = "WITHDRAW_POCKET";
        request.data = Map.of("name", "Test Pocket", "amount", "500");
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        String jsonRequest = gson.toJson(request);
        writer.write(jsonRequest);
        writer.newLine();
        writer.flush();

        String line = reader.readLine();
        Response response = gson.fromJson(line, Response.class);
        System.out.println(response);

        socket.close();

        assert response.status.equals("ok");
        Pocket pocket = gson.fromJson(response.data, Pocket.class);

        assert pocket.getName().equals("Test Pocket");
        assert pocket.getBalance() == 700;
        assert pocket.getMainAccount() != null;
        assert pocket.getMainAccount().getAvailableBalance() == 4300;
        assert pocket.getMainAccount().getTotalBalance() == 5000;

    }

    @Test
    public void D_testDepositPocketFailed() throws Exception {
        Socket socket = new Socket("localhost", 5000);
        Request request = new Request();
        request.action = "DEPOSIT_POCKET";
        request.data = Map.of("name", "Test Pocket", "amount", "5000");
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        String jsonRequest = gson.toJson(request);
        writer.write(jsonRequest);
        writer.newLine();
        writer.flush();

        String line = reader.readLine();
        Response response = gson.fromJson(line, Response.class);
        System.out.println(response);
        socket.close();
        assert response.status.equals("error");
        assert response.data.get("message").getAsString().contains("Insufficient funds in main account");

    }

    @Test
    public void E_testWithdrawPocketFailed() throws Exception {
        Socket socket = new Socket("localhost", 5000);

        Request request = new Request();
        request.action = "WITHDRAW_POCKET";
        request.data = Map.of("name", "Test Pocket", "amount", "1000");
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        String jsonRequest = gson.toJson(request);
        writer.write(jsonRequest);
        writer.newLine();
        writer.flush();

        String line = reader.readLine();
        Response response = gson.fromJson(line, Response.class);
        System.out.println(response);

        socket.close();

        assert response.status.equals("error");
        assert response.data.get("message").getAsString().equals("Insufficient funds in pocket");

    }

    public MainAccount getAccount() throws Exception {
        Socket socket = new Socket("localhost", 5000);

        Request request = new Request();
        request.action = "GET_ACCOUNT";
        request.data = Map.of();
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        String jsonRequest = gson.toJson(request);
        writer.write(jsonRequest);
        writer.newLine();
        writer.flush();

        String line = reader.readLine();
        Response response = gson.fromJson(line, Response.class);
        System.out.println(response);

        socket.close();
        assert response.status.equals("ok");
        MainAccount responseAccount = gson.fromJson(response.data, MainAccount.class);
        return responseAccount;
    }

    @Test
    public void F_testGetAccount() throws Exception {
        MainAccount responseAccount = getAccount();

        assert responseAccount.getAvailableBalance() == 4300;
        assert responseAccount.getTotalBalance() == 5000;
        assert responseAccount.getPockets().size() == 1;
        assert responseAccount.getPockets().get(0).getName().equals("Test Pocket");
        assert responseAccount.getPockets().get(0).getBalance() == 700;
    }

    @Test
    public void G_testDepositAccount() throws Exception {
        Socket socket = new Socket("localhost", 5000);

        Request request = new Request();
        request.action = "DEPOSIT_ACCOUNT";
        request.data = Map.of("amount", "200");
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        String jsonRequest = gson.toJson(request);
        writer.write(jsonRequest);
        writer.newLine();
        writer.flush();

        String line = reader.readLine();
        Response response = gson.fromJson(line, Response.class);
        System.out.println(response);

        socket.close();

        assert response.status.equals("ok");
        MainAccount responseAccount = gson.fromJson(response.data, MainAccount.class);

        assert responseAccount.getAvailableBalance() == 4500;
        assert responseAccount.getTotalBalance() == 5200;

    }

    @Test
    public void H_testConcurrence() throws Exception {

        int n = 100;

        CountDownLatch ready = new CountDownLatch(n);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(n);

        List<ConcurrentTest> tests = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            ConcurrentTest test = new ConcurrentTest(i, ready, start, done);
            tests.add(test);
            test.start();
        }
        ready.await();

        long t0 = System.currentTimeMillis();
        start.countDown();
        done.await();
        long t1 = System.currentTimeMillis();
        long totalTime = t1 - t0 ;


        double avgTime = tests.stream().mapToLong(ConcurrentTest::getTime).average().orElse(0);
        System.out.println("Total time for " + n + " requests: " + totalTime + " ms");

        assertTrue("Total time is greater than " + n * avgTime, totalTime < n * avgTime * 0.7);

        Set<String> badResponses = tests.stream()
                .map(ConcurrentTest::getResponse)
                .filter(r -> !"ok".equals(r.status))
                .map(r -> r.status)
                .collect(Collectors.toSet());

        assertTrue("Hubo respuestas con error: " + badResponses, badResponses.isEmpty());
        if (badResponses.size() > 0) {
            System.out.println("Bad responses: " + badResponses);
            
        }

        double totalDeposited = tests.size() * 10;
        MainAccount responseAccount = getAccount();

        assert responseAccount.getAvailableBalance() == 4500 - totalDeposited;
        assert responseAccount.getTotalBalance() == 5200;
        assert responseAccount.getPockets().size() == 1 + tests.size();
        assert responseAccount.getPockets().get(0).getName().equals("Test Pocket");
        assert responseAccount.getPockets().get(0).getBalance() == 700;
        double pocketsBalance = responseAccount.getPockets().stream().mapToDouble(Pocket::getBalance).sum();
        assert pocketsBalance == 700 + totalDeposited;

    }

    private class ConcurrentTest extends Thread {

        private Response response;
        private CountDownLatch ready;
        private CountDownLatch start;
        private CountDownLatch done;
        private long time;
        private int id;

        public ConcurrentTest(int id, CountDownLatch ready, CountDownLatch start, CountDownLatch done) {
            this.id = id;
            this.ready = ready;
            this.start = start;
            this.done = done;
        }

        public Response getResponse() {
            return response;
        }

        public long getTime() {
            return time;
        }

        public void run() {
            try {
                ready.countDown();
                start.await();
                time = System.currentTimeMillis();
                Socket socket = new Socket("localhost", 5000);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                response = addPocket("Test Pocket " + id, 5.0, reader, writer);
                socket.close();
                socket = new Socket("localhost", 5000);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                response = depositPocket("Test Pocket " + id, 5.0, reader, writer);
                time = System.currentTimeMillis() - time;
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                done.countDown();
            }
        }

    }

}

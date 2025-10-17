import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dtos.Request;
import dtos.Response;
import model.MainAccount;
import model.Pocket;
import services.PocketService;

public class Server {

    private PocketService pocketService;
    private Gson gson;

    public static void main(String[] args) throws Exception {
        Server server = new Server();
        server.init(1000.0);
    }

    public void init(Double initialAmount) throws Exception {
        this.gson = new Gson();
        this.pocketService = new PocketService(initialAmount);

        ServerSocket serverSocket = new ServerSocket(5000);
        System.out.println("Server started on port 5000");

        while (true) {
            Socket socket = serverSocket.accept();
            new Thread(() -> handleClient(socket)).start();
        }
    }

    public void handleClient(Socket socket) {
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            String line = reader.readLine();
            Response response;

            if (line == null || line.isBlank()) {
                response = errorResponse("Empty request");
            } else {
                Request request = gson.fromJson(line, Request.class);
                if (request == null || request.action == null) {
                    response = errorResponse("Invalid request");
                } else {
                    response = handleRequest(request); // nunca retorna null
                }
            }

            writer.write(gson.toJson(response));
            writer.newLine();
            writer.flush();

        } catch (Exception e) {
            try {
                if (writer != null) {
                    Response r = errorResponse(e.getMessage() == null ? "Internal error" : e.getMessage());
                    writer.write(gson.toJson(r));
                    writer.newLine();
                    writer.flush();
                }
            } catch (Exception ignore) {}
        } finally {
            try { if (reader != null) reader.close(); } catch (Exception ignore) {}
            try { if (writer != null) writer.close(); } catch (Exception ignore) {}
            try { socket.close(); } catch (Exception ignore) {}
        }
    }

    public Response handleRequest(Request request) throws Exception {
        Response response = new Response();
        response.status = "ok";
        response.data = new JsonObject();

        try {
            switch (request.action) {
                case "ADD_POCKET": {
                    String name = request.data.get("name");
                    double initialAmount = Double.parseDouble(request.data.get("initialAmount"));
                    Pocket newPocket = pocketService.addPocket(name, initialAmount);
                    response.data = gson.toJsonTree(newPocket).getAsJsonObject();
                    break;
                }
                case "DEPOSIT_POCKET": {
                    String pocketName = request.data.get("name");
                    double depositAmount = Double.parseDouble(request.data.get("amount"));
                    Pocket depositedPocket = pocketService.depositInPocket(pocketName, depositAmount);
                    response.data = gson.toJsonTree(depositedPocket).getAsJsonObject();
                    break;
                }
                case "WITHDRAW_POCKET": {
                    String withdrawPocketName = request.data.get("name");
                    double withdrawAmount = Double.parseDouble(request.data.get("amount"));
                    Pocket withdrawnPocket = pocketService.withdrawFromPocket(withdrawPocketName, withdrawAmount);
                    response.data = gson.toJsonTree(withdrawnPocket).getAsJsonObject();
                    break;
                }
                case "DEPOSIT_ACCOUNT": {
                    double accountDepositAmount = Double.parseDouble(request.data.get("amount"));
                    MainAccount updatedAccount = pocketService.depositInAccount(accountDepositAmount);
                    response.data = gson.toJsonTree(updatedAccount).getAsJsonObject();
                    break;
                }
                case "GET_ACCOUNT": {
                    MainAccount account = pocketService.getMainAccount();
                    response.data = gson.toJsonTree(account).getAsJsonObject();
                    break;
                }
                default: {
                    response.status = "error";
                    response.data = new JsonObject();
                    response.data.addProperty("message", "Unknown action");
                }
            }
        } catch (IllegalArgumentException | IllegalStateException ex) {
            response.status = "error";
            response.data = new JsonObject();
            response.data.addProperty("message", ex.getMessage()); // <- CLAVE CORRECTA
        } catch (Exception ex) {
            response.status = "error";
            response.data = new JsonObject();
            response.data.addProperty("message", "Internal error");
        }
        return response;
    }

    private Response errorResponse(String msg) {
        Response r = new Response();
        r.status = "error";
        r.data = new JsonObject();
        r.data.addProperty("message", msg); // <- CLAVE CORRECTA
        return r;
    }
}

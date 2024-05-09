package TransactionServer;

import TransactionService.TransactionServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class TransactionServer {
    public static void main(String[] args) throws Exception {
        Server server = ServerBuilder.forPort(50051)
                .addService(new TransactionServiceImpl())
                .build()
                .start();

        System.out.println("Server started on port 50051");
        server.awaitTermination();
    }
}

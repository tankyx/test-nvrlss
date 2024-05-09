import com.TransactionProcess.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

public class TransactionClient {
    private final TransactionServiceGrpc.TransactionServiceBlockingStub blockingStub;
    private final TransactionServiceGrpc.TransactionServiceStub asyncStub;
    private final AtomicLong latencyStart = new AtomicLong();
    private static CountDownLatch externalTransactionLatch;

    public TransactionClient(ManagedChannel channel) {
        blockingStub = TransactionServiceGrpc.newBlockingStub(channel);
        asyncStub = TransactionServiceGrpc.newStub(channel);
    }

    public static void main(String[] args) {
        // Initialize gRPC channel to the server
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();
        TransactionClient client = new TransactionClient(channel);

        externalTransactionLatch = new CountDownLatch(100);

        // Create 100 users
        for (int i = 1; i <= 100; i++) {
            String userId = "U" + i;
            client.addUser(userId, "User" + i, "Surname" + i, 1000.0);
        }

        // For each user, perform valid and invalid internal transactions, and a valid external withdrawal
        for (int i = 1; i <= 100; i++) {
            String userId = "U" + i;
            String recipientId = "U" + (i == 100 ? 1 : i + 1); // Rotate recipient for valid transaction

            // Valid Internal Transaction
            String ValidIntTransactionId = client.sendMoneyInternally(userId, recipientId, 100.0);
            client.streamTransactionStatus(ValidIntTransactionId);

            // Invalid Internal Transaction (attempt to send more than balance)
            String InvalidIntTransactionId = client.sendMoneyInternally(userId, recipientId, 5000.0);
            client.streamTransactionStatus(InvalidIntTransactionId);

            // External Transaction
            String ExtTransactionId = client.sendMoneyExternally(userId, 50.0, "external-address-" + UUID.randomUUID());

            // Stream transaction status updates
            client.streamTransactionStatus(ExtTransactionId);
        }

        // Wait for all external transactions to complete
        try {
            externalTransactionLatch.await();
        } catch (InterruptedException e) {
            System.err.println("Interrupted while waiting for external transactions to complete.");
        }

        // Shut down the gRPC channel
        channel.shutdown();
    }

    public void addUser(String userId, String name, String surname, double balance) {
        AddUserRequest request = AddUserRequest.newBuilder()
                .setId(userId)
                .setName(name)
                .setSurname(surname)
                .setBalance(balance)
                .build();
        try {
            AddUserResponse response = blockingStub.addUser(request);
            System.out.println("User Added - ID: " + response.getUserId() + ", Status: " + response.getStatus());
        } catch (Exception e) {
            System.err.println("AddUser RPC failed: " + e.getMessage());
        }
    }

    public String sendMoneyInternally(String senderId, String recipientId, double amount) {
        InternalTransferRequest request = InternalTransferRequest.newBuilder()
                .setSenderId(senderId)
                .setRecipientId(recipientId)
                .setAmount(amount)
                .build();
        try {
            TransactionResponse response = blockingStub.sendMoneyInternally(request);
            System.out.println("Internal Transaction - Sender: " + senderId + ", Recipient: " + recipientId
                    + ", Status: " + response.getStatus());
            return response.getTransactionId(); // Return the transaction ID to stream updates
        } catch (Exception e) {
            System.err.println("SendMoneyInternally RPC failed: " + e.getMessage());
            return null;
        }
    }

    public String sendMoneyExternally(String userId, double amount, String externalAddress) {
        ExternalWithdrawalRequest request = ExternalWithdrawalRequest.newBuilder()
                .setUserId(userId)
                .setAmount(amount)
                .setExternalAddress(externalAddress)
                .build();
        try {
            Instant start = Instant.now();
            TransactionResponse response = blockingStub.sendMoneyExternally(request);
            latencyStart.set(start.toEpochMilli());
            System.out.println("External Transaction - User: " + userId + ", Status: " + response.getStatus());
            return response.getTransactionId(); // Return the transaction ID to stream updates
        } catch (Exception e) {
            System.err.println("SendMoneyExternally RPC failed: " + e.getMessage());
            return null;
        }
    }

    public void streamTransactionStatus(String transactionId) {
        if (transactionId == null) {
            System.err.println("Invalid transaction ID; cannot stream status updates.");
            return;
        }

        TransactionStatusRequest request = TransactionStatusRequest.newBuilder()
                .setTransactionId(transactionId)
                .build();

        // Set up an observer to listen to transaction status updates
        asyncStub.streamTransactionStatus(request, new StreamObserver<>() {
            @Override
            public void onNext(StatusUpdate update) {
                String status = update.getStatus();
                String receivedTransactionId = update.getTransactionId();

                // Ignore intermediate status updates like "PROCESSING" and only print final statuses
                if ("FAILED".equals(status) || "COMPLETED".equals(status)) {
                    Instant end = Instant.now();
                    long latencyMs = Duration.between(Instant.ofEpochMilli(latencyStart.get()), end).toMillis();

                    System.out.println("Transaction Final Update - ID: " + receivedTransactionId + ", Status: " + status
                            + ", Latency: " + latencyMs + " ms");
                    externalTransactionLatch.countDown();
                }
            }

            @Override
            public void onError(Throwable t) {
                // Handle errors
                System.err.println("StreamTransactionStatus RPC Error: " + t.getMessage());
                externalTransactionLatch.countDown();
            }

            @Override
            public void onCompleted() {
                // Called when the stream is complete
                System.out.println("StreamTransactionStatus completed.");
            }
        });
    }
}

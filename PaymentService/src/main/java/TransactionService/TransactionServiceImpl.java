package TransactionService;

import WithdrawalService.DataStore.InMemory;
import WithdrawalService.Models.User;
import WithdrawalService.WithdrawalService;
import WithdrawalService.WithdrawalServiceStub;
import WithdrawalService.WithdrawalService.WithdrawalState;
import com.TransactionProcess.grpc.*;
import io.grpc.stub.StreamObserver;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class TransactionServiceImpl extends TransactionServiceGrpc.TransactionServiceImplBase {
    private final Map<String, StreamObserver<StatusUpdate>> statusObservers = new ConcurrentHashMap<>();
    private final InMemory memory = new InMemory();
    private final AtomicLong transactionCounter = new AtomicLong(1);
    private final WithdrawalService withdrawalService = new WithdrawalServiceStub();

    public TransactionServiceImpl() {
    }

    @Override
    public void addUser(AddUserRequest request, StreamObserver<AddUserResponse> responseObserver) {
        AddUserResponse response;

        if (memory.userExists(request.getId())) {
            response = AddUserResponse.newBuilder()
                    .setUserId(request.getId())
                    .setStatus("ERROR : User already exists")
                    .build();
        } else {
            memory.addUser(request.getName(), request.getSurname(), request.getId(), request.getBalance());
            response = AddUserResponse.newBuilder()
                    .setUserId(request.getId())
                    .setStatus("SUCCESS")
                    .build();
        }
        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }
    @Override
    public void sendMoneyInternally(InternalTransferRequest request, StreamObserver<TransactionResponse> responseObserver) {
        User sender = memory.getUser(request.getSenderId());
        User recipient = memory.getUser(request.getRecipientId());
        String transactionId = "TX" + transactionCounter.getAndIncrement();

        //Both users exist
        if (sender != null && recipient != null) {
            BigDecimal transactionAmount = BigDecimal.valueOf(request.getAmount());
            if (!memory.createTransaction(transactionId, sender.getId(), recipient.getId(), transactionAmount)) {
                TransactionResponse response = TransactionResponse.newBuilder()
                        .setTransactionId(transactionId)
                        .setStatus("Failed to create transaction : Transaction already exists.")
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }

            // If the sender balance is not sufficient, set the transaction state to FAILED
            // Else, update the balances of both clients and set the transaction state to COMPLETED
            if (sender.getBalance().compareTo(transactionAmount) < 0) {
                memory.changeTransactionState(transactionId, WithdrawalState.FAILED);
            } else {
                sender.setBalance(sender.getBalance().subtract(transactionAmount));
                recipient.setBalance(recipient.getBalance().add(transactionAmount));
                memory.changeTransactionState(transactionId, WithdrawalState.COMPLETED);
            }

            WithdrawalState status = memory.getTransaction(transactionId).getState();
            notifyObservers(transactionId, status.toString());

            TransactionResponse response = TransactionResponse.newBuilder()
                    .setTransactionId(transactionId)
                    .setStatus(status.toString())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            if (!memory.userExists(request.getSenderId())) {
                TransactionResponse response = TransactionResponse.newBuilder()
                        .setTransactionId(transactionId)
                        .setStatus("FAILED : Sender ID doesn't exist")
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                return;
            }
            if (!memory.userExists(request.getRecipientId())) {
                TransactionResponse response = TransactionResponse.newBuilder()
                        .setTransactionId(transactionId)
                        .setStatus("FAILED : Recipient ID doesn't exist")
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        }

    }

    @Override
    public void sendMoneyExternally(ExternalWithdrawalRequest request, StreamObserver<TransactionResponse> responseObserver) {
        String transactionId = "TX" + transactionCounter.getAndIncrement();

        if (!memory.userExists(request.getUserId())) {
            TransactionResponse response = TransactionResponse.newBuilder()
                    .setTransactionId(transactionId)
                    .setStatus("FAILED : Sender ID doesn't exist")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        User sender = memory.getUser(request.getUserId());
        BigDecimal transactionAmount = BigDecimal.valueOf(request.getAmount());

        memory.createTransaction(transactionId, sender.getId(), request.getExternalAddress(), transactionAmount);

        // If the sender balance is not sufficient, set the transaction state to FAILED
        if (sender.getBalance().compareTo(transactionAmount) < 0) {
            memory.getTransaction(transactionId).setState(WithdrawalState.FAILED);
        } else {
            memory.getTransaction(transactionId).setState(WithdrawalState.PROCESSING);
            sender.setBalance(sender.getBalance().subtract(transactionAmount));
            withdrawalService.requestWithdrawal(new WithdrawalService.WithdrawalId(UUID.randomUUID()),
                    new WithdrawalService.Address(request.getExternalAddress()), BigDecimal.valueOf(request.getAmount()));
        }
        WithdrawalState status = memory.getTransaction(transactionId).getState();
        notifyObservers(transactionId, status.toString());

        TransactionResponse response = TransactionResponse.newBuilder()
                .setTransactionId(transactionId)
                .setStatus(status.toString())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void streamTransactionStatus(TransactionStatusRequest request, StreamObserver<StatusUpdate> responseObserver) {
        String transactionId = request.getTransactionId();
        statusObservers.put(transactionId, responseObserver);
    }

    private void notifyObservers(String transactionId, String status) {
        StatusUpdate update = StatusUpdate.newBuilder()
                .setTransactionId(transactionId)
                .setStatus(status)
                .build();
        StreamObserver<StatusUpdate> observer = statusObservers.get(transactionId);
        if (observer != null) {
            observer.onNext(update);
            if ("COMPLETED".equals(status) || "FAILED".equals(status)) {
                observer.onCompleted();
                statusObservers.remove(transactionId);
            }
        }
    }
}

package TransactionService;

import WithdrawalService.DataStore.InMemory;
import WithdrawalService.WithdrawalService;
import WithdrawalService.WithdrawalServiceStub;
import com.TransactionProcess.grpc.*;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.Map;
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

    }
    @Override
    public void sendMoneyInternally(InternalTransferRequest request, StreamObserver<TransactionResponse> responseObserver) {

    }

    @Override
    public void sendMoneyExternally(ExternalWithdrawalRequest request, StreamObserver<TransactionResponse> responseObserver) {

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
            observer.onCompleted();
        }
    }
}

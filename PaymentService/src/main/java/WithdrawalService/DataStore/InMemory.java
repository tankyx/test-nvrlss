package WithdrawalService.DataStore;

import WithdrawalService.Models.Transaction;
import WithdrawalService.Models.User;
import WithdrawalService.Utils.SaltGenerator;
import WithdrawalService.WithdrawalService.WithdrawalState;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemory {
    private final ConcurrentMap<String, User> users = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Transaction> transactions = new ConcurrentHashMap<>();

    public void addUser(String name, String surname, String id, Double balance) {
        String salt = SaltGenerator.generateUniqueSalt();
        User user = new User(name, surname, id, balance, salt);

        users.putIfAbsent(user.getId(), user);
    }

    public User getUser(String id) {
        return users.get(id);
    }
    public boolean userExists(String id) {
        return users.containsKey(id);
    }

    public boolean removeUser(String id) {
        return users.remove(id) != null;
    }

    public boolean createTransaction(String id, String senderId, String receiverId, BigDecimal amount) {
        if (!users.containsKey(senderId) || !users.containsKey(receiverId)) {
            return false;
        }
        Transaction transaction = new Transaction(id, senderId, receiverId, amount, WithdrawalState.PROCESSING);
        return transactions.putIfAbsent(transaction.getId(), transaction) == null;
    }

    public boolean createTransactionWithAddress(String id, String senderId, String address, BigDecimal amount) {
        if (!users.containsKey(senderId)) {
            return false;
        }
        Transaction transaction = new Transaction(id, senderId, address, amount, WithdrawalState.PROCESSING);
        return transactions.putIfAbsent(transaction.getId(), transaction) == null;
    }
    public void addTransaction(Transaction transaction) {
        transactions.putIfAbsent(transaction.getId(), transaction);
    }

    public Transaction getTransaction(String id) {
        return transactions.get(id);
    }

    public void changeTransactionState(String id, WithdrawalState state) {
        Transaction transaction = transactions.get(id);
        if (transaction == null) {
            return;
        }
        transaction.setState(state);
    }
}

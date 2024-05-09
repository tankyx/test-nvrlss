package WithdrawalService.DataStore;

import WithdrawalService.Models.Transaction;
import WithdrawalService.Models.User;
import WithdrawalService.Utils.SaltGenerator;
import WithdrawalService.WithdrawalService.WithdrawalState;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemory {
    private final ConcurrentMap<String, User> users = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Transaction> transactions = new ConcurrentHashMap<>();

    public void addUser(String name, String surname, Double balance) {
        String salt = SaltGenerator.generateUniqueSalt();
        User user = new User(name, surname, balance, salt);

        users.putIfAbsent(user.getId(), user);
    }

    public User getUser(String id) {
        return users.get(id);
    }

    public boolean removeUser(String id) {
        return users.remove(id) != null;
    }

    public void addTransaction(Transaction transaction) {
        transactions.putIfAbsent(transaction.getId(), transaction);
    }

    public Transaction getTransaction(String id) {
        return transactions.get(id);
    }

    public boolean changeTransactionState(String id, WithdrawalState state) {
        Transaction transaction = transactions.get(id);
        if (transaction == null) {
            return false;
        }
        transaction.setState(state);
        return true;
    }
}

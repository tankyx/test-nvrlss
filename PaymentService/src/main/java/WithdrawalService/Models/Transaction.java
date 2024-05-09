package WithdrawalService.Models;

import java.math.BigDecimal;
import java.util.Objects;

import WithdrawalService.WithdrawalService.WithdrawalState;

/*
    * Transaction model

    * id - transaction id
    * senderId - sender id
    * receiverId - receiver id
    * amount - transaction amount
    * state - transaction state
 */

public class Transaction {
    String id;
    String senderId;
    String receiverId;
    BigDecimal amount;
    WithdrawalState state;

    public Transaction(String id, String senderId, String receiverId, BigDecimal amount, WithdrawalState state) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.amount = amount;
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public WithdrawalState getState() {
        return state;
    }

    public void setState(WithdrawalState state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id) && Objects.equals(senderId, that.senderId) && Objects.equals(receiverId, that.receiverId) && Objects.equals(amount, that.amount) && state == that.state;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, senderId, receiverId, amount, state);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + id + '\'' +
                ", senderId='" + senderId + '\'' +
                ", receiverId='" + receiverId + '\'' +
                ", amount=" + amount +
                ", state=" + state +
                '}';
    }
}
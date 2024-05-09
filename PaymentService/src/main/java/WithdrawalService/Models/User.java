package WithdrawalService.Models;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/*
    * User model

    * id - user id
    * address - user address
    * name - username
    * surname - user surname
    * balance - user balance
 */
public class User {
    String id;
    UUID address;
    String name;
    String surname;
    BigDecimal balance;

    public User(String name, String surname, String id, Double balance) {
        this.name = name;
        this.surname = surname;
        this.balance = BigDecimal.valueOf(balance);
        this.id = id;
        this.address = UUID.randomUUID();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UUID getAddress() {
        return address;
    }

    public void setAddress(UUID address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(address, user.address) && Objects.equals(name, user.name) && Objects.equals(surname, user.surname) && Objects.equals(balance, user.balance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, address, name, surname, balance);
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", address='" + address + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", balance=" + balance +
                '}';
    }
}

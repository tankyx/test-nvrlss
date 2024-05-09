package WithdrawalService.Models;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.Objects;

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
    String address;
    String name;
    String surname;
    BigDecimal balance;

    private void generateId() {
        this.id = "user-" + this.name + "-" + this.surname;
    }
    /*
    * Need to generate a wallet address for the user
    * using the generated id and using a unique salt to it
    * and storing it as a base64 encoded string
    */
    private void generateWalletAddress(String salt) {
        this.address = Base64.getEncoder().encodeToString((this.id + salt).getBytes());
    }
    public User(String name, String surname, Double balance, String salt) {
        this.name = name;
        this.surname = surname;
        this.balance = BigDecimal.valueOf(balance);

        generateId();
        generateWalletAddress(salt);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
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

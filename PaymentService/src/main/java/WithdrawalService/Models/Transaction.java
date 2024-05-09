package WithdrawalService.Models;

import java.math.BigDecimal;
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
}

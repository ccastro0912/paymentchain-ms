
package com.paymentchain.transactions.repository;

import com.paymentchain.transactions.entities.Transaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TransactionRepository extends JpaRepository<Transaction, Long>{
    
    @Query("SELECT c FROM Transaction c WHERE c.ibanAccount = ?1")
    public List<Transaction> findByIbanAccount(String ibanAccount);
}

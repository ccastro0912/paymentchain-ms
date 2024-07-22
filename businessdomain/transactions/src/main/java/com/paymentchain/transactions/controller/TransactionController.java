
package com.paymentchain.transactions.controller;

import com.paymentchain.transactions.entities.Transaction;
import com.paymentchain.transactions.repository.TransactionRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transaction")
public class TransactionController {
    
    @Autowired
    TransactionRepository transactionRepository;
    
    @GetMapping()
    public List<Transaction> GetAll()
    {
        return transactionRepository.findAll();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Transaction> GetById(@PathVariable long id)
    {
        Optional<Transaction> transaction = transactionRepository.findById(id);
        if(transaction.isPresent())
            return new ResponseEntity(transaction.get(), HttpStatus.OK);
        else
            return new ResponseEntity(HttpStatus.NOT_FOUND);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Transaction> Put(@PathVariable Long id, @RequestBody Transaction transaction) {
        Optional<Transaction> transactionGetById = transactionRepository.findById(id);
        if(transactionGetById.isPresent())
        {
            Transaction nTransaction = transactionGetById.get();
            nTransaction.setAmount(transaction.getAmount());
            nTransaction.setChannel(transaction.getChannel());
            nTransaction.setDate(transaction.getDate());
            nTransaction.setDescription(transaction.getDescription());
            nTransaction.setFee(transaction.getFee());
            nTransaction.setIbanAccount(transaction.getIbanAccount());
            nTransaction.setReference(transaction.getReference());
            nTransaction.setStatus(transaction.getStatus());
            return new ResponseEntity(transactionRepository.save(nTransaction), HttpStatus.OK);
        }
        else
            return new ResponseEntity(HttpStatus.NOT_FOUND);
    }
    
    @PostMapping
    public ResponseEntity<?> Post(@RequestBody Transaction transaction) {
        Transaction save = transactionRepository.save(transaction);
        return ResponseEntity.ok(save);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity Delete(@PathVariable Long id) {
        transactionRepository.deleteById(id);
        return new ResponseEntity(HttpStatus.OK);
    }
    
    @GetMapping("/customer/transactions")
    public List<Transaction> GetCustomerTransactions(@RequestParam String ibanAccount)
    {
        return transactionRepository.findByIbanAccount(ibanAccount);
    }
}

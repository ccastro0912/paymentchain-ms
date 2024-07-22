package com.paymentchain.customer.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.paymentchain.customer.entities.Customer;
import com.paymentchain.customer.entities.CustomerProduct;
import com.paymentchain.customer.repository.CustomerRepository;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import java.util.Collections;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

@RestController
@RequestMapping("/customer")
public class CustomerController {
    
    @Autowired
    CustomerRepository customerRepository;
    
    private final WebClient.Builder webClientBuilder;
    
    HttpClient client = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .option(EpollChannelOption.TCP_KEEPIDLE, 300)
            .option(EpollChannelOption.TCP_KEEPINTVL, 60)
            .responseTimeout(Duration.ofSeconds(1))
            .doOnConnected(connection -> {
                connection.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS));
                connection.addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS));
            });
    
    public CustomerController(WebClient.Builder webClientBuilder){
        this.webClientBuilder = webClientBuilder;
    }
    
    @GetMapping()
    public List<Customer> GetAll() {
        return customerRepository.findAll();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Customer> get(@PathVariable Long id) {
        Optional<Customer> customer = customerRepository.findById(id);
        if(customer.isPresent())
            return new ResponseEntity(customer.get(), HttpStatus.OK);
        else
            return new ResponseEntity(HttpStatus.NOT_FOUND);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Customer> put(@PathVariable Long id, @RequestBody Customer customer) {
        Optional<Customer> customerGetById = customerRepository.findById(id);
        if(customerGetById.isPresent())
        {
            Customer nCustomer = customerGetById.get();
            nCustomer.setName(customer.getName());
            nCustomer.setPhone(customer.getPhone());
            Customer saveCustomer = customerRepository.save(nCustomer);
            return new ResponseEntity(saveCustomer, HttpStatus.OK);
        }
        else
            return new ResponseEntity(HttpStatus.NOT_FOUND);
    }
    
    @PostMapping
    public ResponseEntity<?> post(@RequestBody Customer customer) {
        customer.getProducts().forEach(x ->
            x.setCustomer(customer)
        );
        Customer save = customerRepository.save(customer);
        return ResponseEntity.ok(save);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity delete(@PathVariable Long id) {
        customerRepository.deleteById(id);
        return new ResponseEntity(HttpStatus.OK);
    }
    
    @GetMapping("/full")
    public ResponseEntity<Customer> getFull(@RequestParam String code) {
        Customer customer = customerRepository.findByCode(code);
        List<CustomerProduct> products = customer.getProducts();
        products.forEach(x -> {
            String productName = GetProductName(x.getId());
            x.setProductName(productName);
        });
        return new ResponseEntity(customer, HttpStatus.OK);
    }
    
    //@Value("${custom.activeprofileName}")    
    //private String profile
    
    @Autowired
    private Environment env;
    
    @GetMapping("/check")
    public String Check()
    {
        return "Hello your property value is: " + env.getProperty("custom.activeprofileName");
    }
    
    private String GetProductName(long id){
        try
        {
            WebClient build = webClientBuilder.clientConnector(new ReactorClientHttpConnector(client))
                    .baseUrl("http://localhost:8082/product")
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .defaultUriVariables(Collections.singletonMap("url", "http://localhost:8082/product"))
                    .build();
            JsonNode block = build.method(HttpMethod.GET).uri("/"+ id)
                    .retrieve().bodyToMono(JsonNode.class).block();
            if (block != null && block.has("name")) {
                return block.get("name").asText();
            } else {
                // Maneja el caso donde el campo "name" no está presente en el JSON
                return "Nombre no encontrado";
            }
        } 
        catch (WebClientResponseException e) 
        {
            System.err.println("Error en la llamada al servicio: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return "";
        } catch (Exception e) {
            return "";
        }
    }
    
    private List<?> GetTransactions(String iban){
        try
        {
            WebClient build = webClientBuilder.clientConnector(new ReactorClientHttpConnector(client))
                    .baseUrl("http://localhost:8083/transaction")
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
            
            List<?> transactions = build.method(HttpMethod.GET).uri(uribuilder -> uribuilder
                    .path("customer/transactions")
                    .queryParam("ibanAccount",iban)
                    .build())
                    .retrieve().bodyToFlux(Object.class).collectList().block();
            return transactions;
        } 
        catch (WebClientResponseException e) 
        {
            System.err.println("Error en la llamada al servicio: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}

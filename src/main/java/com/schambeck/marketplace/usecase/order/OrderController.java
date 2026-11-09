package com.schambeck.marketplace.usecase.order;
import com.schambeck.marketplace.security.AuthenticationService; import lombok.RequiredArgsConstructor; import org.springframework.data.domain.*; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.security.Principal; import java.util.UUID;
@RestController @RequestMapping("orders") @RequiredArgsConstructor public class OrderController { private final OrderService service; private final AuthenticationService auth;
 @PostMapping("/checkout") public ResponseEntity<OrderWeb> checkout(@RequestHeader(value="Idempotency-Key",required=false) String key,@RequestBody(required=false) CheckoutRequest req,Principal p){return ResponseEntity.ok(OrderWeb.from(service.checkout(auth.getLoggedUsername(p),key,req==null?null:req.getInstructions())));}
 @GetMapping("/{id}") public OrderWeb get(@PathVariable UUID id,Principal p){return OrderWeb.from(service.find(id,auth.getLoggedUsername(p)));}
 @GetMapping public Page<OrderWeb> list(Pageable pageable,Principal p){return service.list(auth.getLoggedUsername(p),pageable).map(OrderWeb::from);}
}

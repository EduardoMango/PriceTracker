package com.eduardomango.pricetracker.product;

import com.eduardomango.pricetracker.product.domain.dto.ProductRequest;
import com.eduardomango.pricetracker.product.domain.dto.ProductResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")

@AllArgsConstructor
public class ProductController {

    private final IProductService productService;

    @GetMapping()
    public ResponseEntity<List<ProductResponse>> getAllProducts(@RequestParam(required = false) String name,
                                                                @RequestParam(required = false) String nameMatch,
                                                                @RequestParam(required = false) String description,
                                                                @RequestParam(required = false) BigDecimal minPrice,
                                                                @RequestParam(required = false)BigDecimal maxPrice) {

        return ResponseEntity.ok(productService.getAllProducts(name, nameMatch, description, minPrice, maxPrice));
    }

    @GetMapping("{productId}")
    public ResponseEntity<ProductResponse> findById(@PathVariable UUID productId) {
        return ResponseEntity.ok(productService.getProductById(productId));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> save(@RequestBody ProductRequest productRequest) {
        return ResponseEntity.ok(productService.save(productRequest));
    }

    @DeleteMapping("{productId}")
    public ResponseEntity<Void> delete(@PathVariable UUID productId) {
        productService.delete(productId);
        return ResponseEntity.noContent().build();
    }
}

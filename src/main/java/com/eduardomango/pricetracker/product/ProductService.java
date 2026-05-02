package com.eduardomango.pricetracker.product;

import com.eduardomango.pricetracker.common.architecture.scraping.ClientOrchestrator;
import com.eduardomango.pricetracker.common.exceptions.EntityNotFoundException;
import com.eduardomango.pricetracker.common.model.IMapper;
import com.eduardomango.pricetracker.common.model.Price;
import com.eduardomango.pricetracker.product.domain.ProductEntity;
import com.eduardomango.pricetracker.product.domain.dto.ProductRequest;
import com.eduardomango.pricetracker.product.domain.dto.ProductResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ProductService implements IProductService{

    private final ProductRepository productRepository;
    private final IMapper<ProductEntity, ProductRequest> requestMapper;
    private final IMapper<ProductEntity, ProductResponse> responseMapper;
    private final ClientOrchestrator clientOrchestrator;

    @Override
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(responseMapper::toDTO)
                .toList();
    }

    @Override
    public ProductResponse getProductById(UUID productId) {
        return productRepository.findByExternalId(productId)
                .map(responseMapper::toDTO)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Product", "Product was not found", "productId", productId.toString()
                ));
    }

    @Override
    public ProductResponse save(ProductRequest productRequest) {

        URI url = URI.create(productRequest.url().value());

        System.out.println(url);

        //These lines were added to test the price
        ProductEntity toBeSaved = clientOrchestrator.scrape(url);

        System.out.println(toBeSaved);

        ProductEntity saved = productRepository.save(toBeSaved);
        return responseMapper.toDTO(saved);
    }

    @Override
    public void delete(UUID productId) {
        ProductEntity toBeDeleted = productRepository.findByExternalId(productId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Product", "Product was not found", "productId", productId.toString()));

        productRepository.delete(toBeDeleted);
    }
}

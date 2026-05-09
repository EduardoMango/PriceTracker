package com.eduardomango.pricetracker.common.architecture.scraping.adapters;

import com.eduardomango.pricetracker.common.architecture.scraping.ClientService;
import com.eduardomango.pricetracker.common.exceptions.ParseException;
import com.eduardomango.pricetracker.common.exceptions.UnsuportedWebsite;
import com.eduardomango.pricetracker.common.model.Price;
import com.eduardomango.pricetracker.product.domain.ProductEntity;
import com.eduardomango.pricetracker.product.domain.URL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LibreriaPalitoClient implements ClientService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    private static final Pattern PRICE_PATTERN = Pattern.compile("[\"']price[\"']\\s*:\\s*\"?([\\d.]+)\"?");

    public LibreriaPalitoClient(WebClient.Builder builder, ObjectMapper objectMapper) {
        this.webClient = builder
                .baseUrl("https://www.libreriapalito.com.ar")
                .build();
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(URI url) {
        return url.getHost() != null && url.getHost().endsWith("libreriapalito.com.ar");
    }

    @Override
    public Mono<ProductEntity> getProduct(URI url) {
        return fetchHtml(url).map(html -> {
            Document doc = Jsoup.parse(html);

            // Extraemos el script completo para el primer registro del producto
            String scriptContent = extractProductDetailScript(doc);

            ProductEntity p = parseProductDetail(scriptContent);
            p.setUrl(new URL(url.toString()));
            p.setDescription(doc.select("meta[name=description]").attr("content"));

            return p;
        });
    }

    @Override
    public Mono<Price> getPrice(URI url) {
        return fetchHtml(url).map(html -> {
            // Use regex to find the price
            Matcher matcher = PRICE_PATTERN.matcher(html);
            if (matcher.find()) {
                return new Price(new BigDecimal(matcher.group(1)), "ARS");
            }

            throw new ParseException("Price not found in HTML: ");
        });
    }

    private Mono<String> fetchHtml(URI url) {
        if (!supports(url)) return Mono.error(new UnsuportedWebsite(url.getHost()));

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .switchIfEmpty(Mono.error(new ParseException("Empty html response from Libreria Palito")));
    }

    private String extractProductDetailScript(Document doc) {
        return doc.select("script").stream()
                .map(Element::html)
                .filter(content -> content.contains("var productDetail ="))
                .findFirst()
                .orElseThrow(() -> new ParseException("No se encontró el script 'productDetail'"));
    }

    private ProductEntity parseProductDetail(String script) {
        try {
            // Extract the JSON part of the script
            Pattern pattern = Pattern.compile("var productDetail = (\\{.*?});");
            Matcher matcher = pattern.matcher(script);

            if (matcher.find()) {
                String json = matcher.group(1);

                JsonNode node = objectMapper.readTree(json);

                ProductEntity product = new ProductEntity();
                product.setName(node.get("item_name").asString());

                BigDecimal price = new BigDecimal(node.get("price").asString());
                product.setCurrentPrice(new Price(price, "ARS"));

                product.setLastChecked(LocalDateTime.now());


                return product;
            }
        } catch (Exception e) {
            throw new ParseException("Could not parse product", e);
        }
        throw new ParseException("JSON not found for product");
    }
}

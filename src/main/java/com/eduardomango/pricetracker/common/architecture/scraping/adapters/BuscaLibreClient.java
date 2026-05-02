package com.eduardomango.pricetracker.common.architecture.scraping.adapters;

import com.eduardomango.pricetracker.common.architecture.scraping.ClientService;
import com.eduardomango.pricetracker.common.exceptions.EntityNotFoundException;
import com.eduardomango.pricetracker.common.model.Price;
import com.eduardomango.pricetracker.product.domain.ProductEntity;
import com.eduardomango.pricetracker.product.domain.URL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BuscaLibreClient implements ClientService {

    private final WebClient webClient;

    public BuscaLibreClient(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("https://www.buscalibre.com.ar")
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(5 * 1024 * 1024))
                .build();
    }

    @Override
    public boolean supports(URI url) {
        return url.getHost() != null && url.getHost().endsWith("buscalibre.com.ar");    }

    @Override
    public ProductEntity getProduct(URI url) {

        System.out.println(url);

        //Retrieve the HTML content of the page
        String html = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        //Parse the HTML content using Jsoup
        assert html != null;
        Document doc = Jsoup.parse(html);

        String scriptContent = doc.select("script").stream()
                .map(Element::html)
                .filter(content -> content.contains("dataLayer = [{"))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Product", "Product was not found", "url", url.toString()));

        ProductEntity product = parseDataLayer(scriptContent);
        product.setUrl(new URL(url.toString()));

        return product;
    }

    private ProductEntity parseDataLayer(String script) {
        // Use regex to extract the data
        String name = extractValue(script, "'item_name': '(.*?)'");
        String price = extractValue(script, "'price': (\\d+\\.?\\d*)");

        ProductEntity product = new ProductEntity();
        product.setName(name);
        product.setCurrentPrice(new Price(new BigDecimal(price), "ARS"));
        product.setLastChecked(LocalDateTime.now());

        return product;
    }

    private String extractValue(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }
}

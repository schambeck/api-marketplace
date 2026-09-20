package com.schambeck.marketplace.usecase.catalog;

import com.schambeck.marketplace.usecase.product.ProductMapper;
import com.schambeck.marketplace.usecase.product.ProductService;
import com.schambeck.marketplace.usecase.product.ProductWeb;
import com.schambeck.marketplace.usecase.offer.OfferMapper;
import com.schambeck.marketplace.usecase.offer.OfferService;
import com.schambeck.marketplace.usecase.offer.OfferWeb;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("catalog")
@RequiredArgsConstructor
public class CatalogController {
    private static final ProductMapper mapper = ProductMapper.INSTANCE;
    private final ProductService service;
    private final OfferService offerService;

    @GetMapping("{id}")
    public ResponseEntity<ProductWeb> get(@PathVariable int id) {
        return ResponseEntity.ok(mapper.toWeb(service.findById(id)));
    }

    @GetMapping
    public ResponseEntity<Page<ProductWeb>> search(@PageableDefault Pageable pageable, @RequestParam(required = false) String search) {
        return ResponseEntity.ok(service.search(pageable, search).map(mapper::toWeb));
    }

    @GetMapping("offers")
    public ResponseEntity<Page<OfferWeb>> searchOffers(@PageableDefault Pageable pageable,
                                                       @RequestParam(required = false) String search) {
        return ResponseEntity.ok(offerService.searchPublished(pageable, search).map(OfferMapper::toWeb));
    }

    @GetMapping("offers/{id}")
    public ResponseEntity<OfferWeb> getOffer(@PathVariable Integer id) {
        return ResponseEntity.ok(OfferMapper.toWeb(offerService.findPublishedById(id)));
    }
}

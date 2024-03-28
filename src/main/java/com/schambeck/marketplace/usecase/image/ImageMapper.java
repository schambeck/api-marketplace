package com.schambeck.marketplace.usecase.image;

import com.schambeck.marketplace.usecase.product.ProductRepository;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class ImageMapper {
    @Autowired
    protected ProductRepository productRepository;

    public abstract ImageWeb toWeb(Image entity);
    
    public abstract Image toEntity(ImageWeb web);
    
    public abstract void copy(Image source, @MappingTarget Image target);
    
    @AfterMapping
    public void loadRelations(@MappingTarget Image.ImageBuilder target, ImageWeb web) {
        if (web.getProduct() != null) {
            target.product(productRepository.findByIdOrThrow(web.getProduct().getId()));
        }
    }
    
}

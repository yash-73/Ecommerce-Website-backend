package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repositories.CategoryRepository;
import com.ecommerce.project.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileService fileService;

    @Value("${project.image}")
    private String path;

    @Override
    public ProductDTO addProduct(ProductDTO productDTO, Long categoryId) {


        //Finding category
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(()-> new ResourceNotFoundException("Category", "categoryId", categoryId));

        boolean isProductPresent = false;
        List<Product> products = category.getProducts();
        for (Product product : products){
            if (Objects.equals(product.getProductName(), productDTO.getProductName())){
                isProductPresent = true;
                break;
            }
        }

        if(!isProductPresent) {
            //Setting special Price based on actual price and discount
            Double specialPrice = productDTO.getPrice() - (productDTO.getDiscount() * 0.01 * productDTO.getPrice());
            productDTO.setSpecialPrice(specialPrice);

            //Setting image
            productDTO.setImage("default.png");


            //Mapping ProductDTO to actual product and saving it in repository
            Product newProduct = modelMapper.map(productDTO, Product.class);
            newProduct.setCategory(category);

            //Saving Product
            Product savedProduct = productRepository.save(newProduct);

            //Returning DTO after saving actual Product
            return modelMapper.map(savedProduct, ProductDTO.class);
        }
        else throw new APIException("Product already exists");
    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder){

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> productPage = productRepository.findAll(pageDetails);

        List<Product> products = productPage.getContent();

        if (products.isEmpty()) throw new APIException("No products available");

        List<ProductDTO> content = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(content);
        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setLastPage(productPage.isLast());
        productResponse.setPageSize(productPage.getSize());

        return productResponse;
    }

    @Override
    public ProductResponse getProductsByCategory(Long categoryId,Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(()-> new ResourceNotFoundException("Category", "categoryId", categoryId));

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
//        Page<Product> productPage = productRepository.findAllByCategoryOrderByPriceAsc(category);

        Page<Product> productPage = productRepository.findAllByCategoryOrderByPriceAsc(category, pageDetails);

        List<Product> products = productPage.getContent();
        if (products.isEmpty()) throw new ResourceNotFoundException("Products" , "categoryId", categoryId);

        List<ProductDTO> content = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(content);
        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setLastPage(productPage.isLast());
        productResponse.setPageSize(productPage.getSize());

        return productResponse;
    }

    @Override
    public ProductResponse getProductsByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {


        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        Page<Product> productPage = productRepository.findByProductNameLikeIgnoreCase('%' + keyword + '%', pageDetails);

        List<Product> products = productPage.getContent();

        if (products.isEmpty()) throw new APIException("No products found");

        List<ProductDTO> content = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(content);
        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setLastPage(productPage.isLast());
        productResponse.setPageSize(productPage.getSize());

        return productResponse;

    }

    @Override
    public ProductDTO updateProduct(ProductDTO productDTO, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product", "productId", productId));


        if (productDTO.getProductName() != null) product.setProductName(productDTO.getProductName());
        if (productDTO.getImage() != null) product.setImage(productDTO.getImage());
        if (productDTO.getDescription() != null) product.setDescription(productDTO.getDescription());
        if (productDTO.getQuantity() != null) product.setQuantity(productDTO.getQuantity());
        if (productDTO.getPrice() != null) product.setPrice(productDTO.getPrice());
        if (productDTO.getDiscount() != null) product.setDiscount(productDTO.getDiscount());

        product.setSpecialPrice(product.getPrice() - (product.getDiscount() * 0.01 * product.getPrice()));
        Product updatedProduct =  productRepository.save(product);

        return (modelMapper.map(updatedProduct, ProductDTO.class));

    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product", "productId", productId));

        productRepository.delete(product);
        return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product", "productId", productId));


//        String path = "/ProductImages";
        String fileName = fileService.uploadImage(path, image);

        product.setImage(fileName);
        Product updatedProduct  = productRepository.save(product);

        return modelMapper.map(updatedProduct, ProductDTO.class);
    }



}

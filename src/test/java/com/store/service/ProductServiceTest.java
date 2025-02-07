package com.store.service;

import com.store.dto.product.ProductRequestDTO;
import com.store.dto.product.ProductResponseDTO;
import com.store.entity.Product;
import com.store.exception.product.ProductAlreadyExistsException;
import com.store.exception.product.ProductNotFoundException;
import com.store.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setUp() {

    }

    @Test
    void getProductById_ShouldReturnAProduct_WhenProductExist() {
        Long existingProductId = 1L;
        Product existingProduct = Product.builder()
                .id(existingProductId)
                .name("T-Shirt")
                .brand("Levis")
                .price(50.0)
                .stock(20)
                .build();

        when(productRepository.findById(existingProductId)).thenReturn(Optional.of(existingProduct));

        ProductResponseDTO productResponseDTO = productService.getProductById(existingProductId);

        assertEquals(productResponseDTO.id(), existingProduct.getId());
        assertEquals(productResponseDTO.name(), existingProduct.getName());
        assertEquals(productResponseDTO.brand(), existingProduct.getBrand());
    }

    @Test
    void getProductById_ShouldThrowAnException_WhenProductNotExist() {
        Long nonExistingProductId = 1L;

        when(productRepository.findById(nonExistingProductId)).thenReturn(empty());

        assertThrows(ProductNotFoundException.class, () -> {
            productService.getProductById(nonExistingProductId);
        });

        verify(productRepository, times(1)).findById(nonExistingProductId);
    }

    @Test
    void getAllProducts_ShouldReturnAListOfProducts_IfCustomersExist() {
        List<Product> productList = new ArrayList<>();

        Product product1 = Product.builder()
                .id(1L)
                .name("T-Shirt")
                .brand("Levis")
                .price(50.0)
                .stock(20)
                .build();

        Product product2 = Product.builder()
                .id(2L)
                .name("Jean")
                .brand("Levis")
                .price(65.0)
                .stock(23)
                .build();

        productList.add(product1);
        productList.add(product2);

        when(productRepository.findAll()).thenReturn(productList);

        List<ProductResponseDTO> productResponseDTOList = productService.getAllProducts();

        assertEquals(productResponseDTOList.get(0).name(), productList.get(0).getName());
        assertEquals(productResponseDTOList.get(1).name(), productList.get(1).getName());
    }

    @Test
    void getAllProducts_ShouldThrowAnException_WhenProductsNotExist() {
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        assertThrows(ProductNotFoundException.class, () -> {
           productService.getAllProducts();
        });

        verify(productRepository, times(1)).findAll();
    }

    @Test
    void createNewProduct_ShouldSuccess_WhenProductNotExists() {
        Product product = Product.builder()
                .name("T-Shirt")
                .brand("Levis")
                .price(50.0)
                .stock(20)
                .build();

        ProductRequestDTO productRequestDTO = new ProductRequestDTO(
                "T-Shirt",
                "Levis",
                50.0,
                20
        );

        when(productRepository.save(product)).thenReturn(product);

        ProductResponseDTO productResponseDTO = productService.createNewProduct(productRequestDTO);

        assertEquals(productRequestDTO.name(), productResponseDTO.name());
        assertEquals(productRequestDTO.brand(), productResponseDTO.brand());
        assertEquals(productRequestDTO.price(), productResponseDTO.price());
    }

    @Test
    void createNewProduct_ShouldThrowAnException_WhenProductAlreadyExists() {
        Product existingProduct = Product.builder()
                .name("T-Shirt")
                .brand("Levis")
                .price(50.0)
                .stock(20)
                .build();

        ProductRequestDTO productRequestDTO = new ProductRequestDTO(
                "T-Shirt",
                "Levis",
                50.0,
                20
        );

        when(productRepository.findByNameAndBrand(productRequestDTO.name(), productRequestDTO.brand()))
                .thenReturn(Optional.of(existingProduct));

        assertThrows(ProductAlreadyExistsException.class, () ->
                productService.createNewProduct(productRequestDTO));

        verify(productRepository, never()).save(existingProduct);
    }

    @Test
    void updateProduct_ShouldSuccess_WhenProductExists() {
        Product existingProduct = Product.builder()
                .name("T-Shirt")
                .brand("Levis")
                .price(50.0)
                .stock(20)
                .build();

        ProductRequestDTO productRequestDTO = new ProductRequestDTO(
                "T-Shirt",
                "Levis",
                50.0,
                20
        );

        when(productRepository.findById(anyLong())).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(existingProduct)).thenReturn(existingProduct);

        ProductResponseDTO updatedProduct = productService.updateProduct(1L,productRequestDTO);

        assertEquals(updatedProduct.name(), productRequestDTO.name());
        assertEquals(updatedProduct.brand(), productRequestDTO.brand());
        assertEquals(updatedProduct.price(), productRequestDTO.price());
    }

    @Test
    void updateProduct_ShouldThrowAnException_WhenProductNotExists() {
        Long nonExistingProductId = 1L;
        ProductRequestDTO productRequestDTO =  new ProductRequestDTO(
                "T-Shirt",
                "Levis",
                50.0,
                20
        );

        when(productRepository.findById(nonExistingProductId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () ->
                productService.updateProduct(nonExistingProductId, productRequestDTO)
        );

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deleteProduct_ShouldSuccess_WhenProductExists() {
        Long existingProductId = 1L;
        Product existingProduct = Product.builder()
                .name("T-Shirt")
                .brand("Levis")
                .price(50.0)
                .stock(20)
                .build();

        when(productRepository.findById(existingProductId)).thenReturn(Optional.of(existingProduct));

        productService.deleteProduct(existingProductId);

        verify(productRepository, times(1)).delete(existingProduct);
    }

    @Test
    void deleteProduct_ShouldThrowAnException_WhenProductNotExists() {
        Long nonExistingProductId = 1L;

        when(productRepository.findById(nonExistingProductId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productService.deleteProduct(nonExistingProductId));

        verify(productRepository, times(1)).findById(nonExistingProductId);
    }
}
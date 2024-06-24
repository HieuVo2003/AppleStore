package Nhom08_AppleStore.controller;

import Nhom08_AppleStore.model.Product;
import Nhom08_AppleStore.service.CategoryService;
import Nhom08_AppleStore.service.ProductService;
import Nhom08_AppleStore.service.YearManufactureService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;
    // Display a list of all products
    @Autowired
    private YearManufactureService yearManufactureService;
    @GetMapping
    public String showProductList(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "/products/products-list";
    }
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("years", yearManufactureService.getAllYearManufacture());
        return "/products/add-product";
    }
    @PostMapping("/add")
    public String addProduct(@Valid Product product, BindingResult result, @RequestParam("imageUrl") MultipartFile imageUrl) {
        if (result.hasErrors()) {
            return "/products/add-product";
        }
        if(!imageUrl.isEmpty()){
            try {
                File uploadDir=new File("src/main/resources/static/");
                if(!uploadDir.exists()){
                    uploadDir.mkdirs();
                }
                String image= imageUrl.getOriginalFilename();

                Path imagePath=  Paths.get("src/main/resources/static/"+image);
                Files.write( imagePath,imageUrl.getBytes());
                product.setImUrl(imagePath.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        productService.addProduct(product);
        return "redirect:/products";
    }
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("years", yearManufactureService.getAllYearManufacture());
        return "/products/update-product";
    }
    @PostMapping("/update/{id}")
    public String updateProduct(@PathVariable Long id, @Valid Product product,@RequestParam("imageUrl")MultipartFile imageUrl, BindingResult result) {
        if (result.hasErrors()) {
            product.setId(id);
            return "/products/update-product";
        }
        if(!imageUrl.isEmpty()){
            try {
                File uploadDir=new File("src/main/resources/static/");
                if(!uploadDir.exists()){
                    uploadDir.mkdirs();
                }
                String image= imageUrl.getOriginalFilename();

                Path imagePath=  Paths.get("src/main/resources/static/"+image);
                Files.write( imagePath,imageUrl.getBytes());
                product.setImUrl(imagePath.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        productService.updateProduct(product);
        return "redirect:/products";
    }
    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProductById(id);
        return "redirect:/products";
    }
    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<List<String>> searchProducts(@RequestParam("term") String term) {
        List<String> matchedProducts = productService.getAllProducts().stream()
                .filter(product -> product.getName().toLowerCase().contains(term.toLowerCase()))
                .map(Product::getName)
                .collect(Collectors.toList());

        return ResponseEntity.ok().body(matchedProducts);
    }
    @GetMapping("/{id}")
    public String showProductDetails(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));

        model.addAttribute("product", product);
        return "/products/product-details";
    }
}

package com.beacon.spring_agent_mcp.repo;

import com.beacon.spring_agent_mcp.dto.SupportSystemDto;
import com.beacon.spring_agent_mcp.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepo extends JpaRepository<Product,Long> {
    List<Product> findProductByNameIgnoreCaseOrSkuContainingIgnoreCase(String name, String sku);
    Product findProductBySkuIgnoreCase(String trim);
}

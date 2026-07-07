package com.example.placement_tracker.controller;

import com.example.placement_tracker.dto.CompanyRequest;
import com.example.placement_tracker.dto.CompanyResponse;
import com.example.placement_tracker.dto.ErrorResponse;
import com.example.placement_tracker.service.CompanyService;
import jakarta.validation.Valid;
import org.hibernate.cfg.SchemaToolingSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/companies/")
@CrossOrigin(origins =  "*")
public class CompanyController {
    @Autowired
    CompanyService companyService;

    //CREATE Company
    @PostMapping
    public ResponseEntity<?> createCompany(@Valid @RequestBody CompanyRequest request){
        System.out.println("Inside createCompany");
        try{
            CompanyResponse companyResponse = companyService.createCompany(request);
            return ResponseEntity.status(201).body(companyResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400)
                    .body(new ErrorResponse("VALIDATION_ERROR", e.getMessage(),System.currentTimeMillis()));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
//            return ResponseEntity.status(500)
//                    .body(new ErrorResponse("INTERNAL_ERROR", "Failed to create comapny",System.currentTimeMillis()));
        }
    }

    //READ ALL
    @GetMapping
    public ResponseEntity<?> getAllCompanies(){
        try{
            List<CompanyResponse> companies = companyService.getAllCompanies();
            return ResponseEntity.ok(companies);
        }catch (Exception e){
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("INTERNAL_ERROR","Failed to fetch companies",System.currentTimeMillis()));
        }
    }

    //READ ONE COMPANY
    @GetMapping("/company/{id}")
    public ResponseEntity<?> getCompanyById(@PathVariable UUID id){
        try{
            CompanyResponse companyResponse = companyService.getCompanyById(id);
            return ResponseEntity.ok(companyResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404)
                    .body(new ErrorResponse("NOT_FOUND","Failed to fetch company", System.currentTimeMillis()));
        }
    }

    //UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCompany(@PathVariable UUID id, @RequestBody CompanyRequest request){
        try{
            CompanyResponse companyResponse = companyService.updateCompany(id,request);
            return ResponseEntity.ok(companyResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400)
                    .body(new ErrorResponse("VALIDATION_ERROR",e.getMessage(),System.currentTimeMillis()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("INTERNAL_ERROR","Failed to update company",System.currentTimeMillis()));
        }
    }

    //DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCompany(@PathVariable UUID id){
        try{
            companyService.deleteCompany(id);
            return ResponseEntity.ok()
                    .body(new ErrorResponse("SUCCESS","Company deleted successfully",System.currentTimeMillis()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404)
                    .body(new ErrorResponse("NOT_FOUND", e.getMessage(), System.currentTimeMillis()));
        }catch (Exception e){
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("INTERNAL_ERROR","Failed to delete company",System.currentTimeMillis()));
        }
    }

}

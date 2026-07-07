package com.example.placement_tracker.service;

import com.example.placement_tracker.dto.CompanyRequest;
import com.example.placement_tracker.dto.CompanyResponse;
import com.example.placement_tracker.entity.Company;
import com.example.placement_tracker.repository.CompanyRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CompanyService {
    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    ObjectMapper objectMapper;

    //CREATE company
    public CompanyResponse createCompany(CompanyRequest request) {
        if (companyRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Company Already exists : " + request.getName());
        } else {
            JsonNode packagesJsonNode = null;
            if (request.getPackagesOffered() != null) {
                try {
                    packagesJsonNode = objectMapper.readTree(request.getPackagesOffered());
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid JSON for packages: " + e.getMessage());
                }
            }

            Company company = Company.builder()
                    .name(request.getName())
                    .logoUrl(request.getLogoUrl())
                    .website(request.getWebsite())
                    .description(request.getDescription())
                    .headQuarters(request.getHeadQuarters())
                    .industry(request.getIndustry())
                    .hiringFor(request.getHiringFor())
                    .packagesOffered(packagesJsonNode)
                    .averageDifficulty(request.getAverageDifficulty())
                    .totalApplicants(request.getTotalApplicants())
                    .totalSelected(request.getTotalSelected())
                    .build();

            company = companyRepository.save(company);
            return entityToResponse(company);
        }
    }

    //GET ALL Companies
    public List<CompanyResponse> getAllCompanies(){
        return companyRepository.findAll()
                .stream()
                .map(this :: entityToResponse)
                .collect(Collectors.toList());
    }


    //GET company by ID
    public CompanyResponse getCompanyById(UUID id){
        Company company = companyRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Company not found: "+ id));
        return entityToResponse(company);
    }

    //UPDATE company
    public CompanyResponse updateCompany(UUID id, CompanyRequest request){

        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        JsonNode packagesJsonNode = null;

        if (request.getPackagesOffered() != null) {
            try {
                packagesJsonNode = objectMapper.readTree(request.getPackagesOffered());
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid JSON for packages: " + e.getMessage());
            }
        }

        company.setName(request.getName());
        company.setLogoUrl(request.getLogoUrl());
        company.setWebsite(request.getWebsite());
        company.setDescription(request.getDescription());
        company.setHeadQuarters(request.getHeadQuarters());
        company.setIndustry(request.getIndustry());
        company.setHiringFor(request.getHiringFor());
        company.setPackagesOffered(packagesJsonNode);
        company.setAverageDifficulty(request.getAverageDifficulty());
        company.setTotalApplicants(request.getTotalApplicants());
        company.setTotalSelected(request.getTotalSelected());

        company = companyRepository.save(company);

        return entityToResponse(company);

        }

    //DELETE company
    public void deleteCompany(UUID id){
        if(!companyRepository.existsById(id)){
            throw new IllegalArgumentException("Company not found");
        }else companyRepository.deleteById(id);
    }


    //HELPER
    private CompanyResponse entityToResponse(Company company){
        return CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .logoUrl(company.getLogoUrl())
                .website(company.getWebsite())
                .description(company.getDescription())
                .headQuarters(company.getHeadQuarters())
                .industry(company.getIndustry())
                .hiringFor(company.getHiringFor())
                .packagesOffered(company.getPackagesOffered() != null ?
                        company.getPackagesOffered().toString() : null)
                .averageDifficulty(company.getAverageDifficulty())
                .totalApplicants(company.getTotalApplicants())
                .totalSelected(company.getTotalSelected())
                .createdAt(company.getCreatedAt())
                .build();
    }



}

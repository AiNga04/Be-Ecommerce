package com.zyna.dev.ecommerce.products.service;

import com.zyna.dev.ecommerce.products.models.SizeGuide;
import com.zyna.dev.ecommerce.products.repository.SizeGuideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SizeGuideService {

    private final SizeGuideRepository sizeGuideRepository;

    public List<SizeGuide> getAllSizeGuides() {
        return sizeGuideRepository.findAll();
    }

    public SizeGuide getSizeGuideById(Long id) {
        return sizeGuideRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SizeGuide not found with id: " + id));
    }

    @Transactional
    public SizeGuide createSizeGuide(String name, String description, org.springframework.web.multipart.MultipartFile image) {
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = com.zyna.dev.ecommerce.common.utils.FileUploadUtil.saveImage(image);
        }

        SizeGuide sizeGuide = SizeGuide.builder()
                .name(name)
                .description(description)
                .imageUrl(imageUrl)
                .build();
        return sizeGuideRepository.save(sizeGuide);
    }

    @Transactional
    public SizeGuide updateSizeGuide(Long id, String name, String description, org.springframework.web.multipart.MultipartFile image) {
        SizeGuide sizeGuide = getSizeGuideById(id);
        
        if (name != null) sizeGuide.setName(name);
        if (description != null) sizeGuide.setDescription(description);
        
        if (image != null && !image.isEmpty()) {
            // Delete old image if exists? Optional but good practice.
            // FileUploadUtil.replaceImage handles strict replacement usually.
            String newUrl = com.zyna.dev.ecommerce.common.utils.FileUploadUtil.saveImage(image);
            sizeGuide.setImageUrl(newUrl);
        }
        
        return sizeGuideRepository.save(sizeGuide);
    }

    @Transactional
    public void deleteSizeGuide(Long id) {
        if (!sizeGuideRepository.existsById(id)) {
            throw new com.zyna.dev.ecommerce.common.exceptions.ApplicationException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "SizeGuide not found!"
            );
        }
        sizeGuideRepository.deleteById(id);
    }
}

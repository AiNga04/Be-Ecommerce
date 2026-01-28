package com.zyna.dev.ecommerce.products.service;

import com.zyna.dev.ecommerce.products.dto.request.ColorRequest;
import com.zyna.dev.ecommerce.products.models.Color;
import com.zyna.dev.ecommerce.products.repository.ColorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ColorService {

    private final ColorRepository colorRepository;

    public List<Color> getAllColors() {
        return colorRepository.findAll();
    }

    public Color getColorById(Long id) {
        return colorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Color not found with id: " + id));
    }

    @Transactional
    public Color createColor(ColorRequest request) {
        String code = request.getCode() != null ? request.getCode().trim() : null;
        String name = request.getName() != null ? request.getName().trim() : null;

        if (colorRepository.findByCode(code).isPresent()) {
            throw new com.zyna.dev.ecommerce.common.exceptions.ApplicationException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Color code already exists!"
            );
        }
        if (colorRepository.findByName(name).isPresent()) {
            throw new com.zyna.dev.ecommerce.common.exceptions.ApplicationException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Color name already exists!"
            );
        }

        try {
            Color color = Color.builder()
                    .name(name)
                    .code(code)
                    .description(request.getDescription())
                    .build();
            return colorRepository.save(color);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new com.zyna.dev.ecommerce.common.exceptions.ApplicationException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Color code or name already exists!"
            );
        }
    }

    @Transactional
    public Color updateColor(Long id, ColorRequest request) {
        Color color = getColorById(id);
        String code = request.getCode() != null ? request.getCode().trim() : null;
        String name = request.getName() != null ? request.getName().trim() : null;

        if (!color.getCode().equals(code) && colorRepository.findByCode(code).isPresent()) {
            throw new com.zyna.dev.ecommerce.common.exceptions.ApplicationException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Color code already exists!"
            );
        }
        if (!color.getName().equals(name) && colorRepository.findByName(name).isPresent()) {
            throw new com.zyna.dev.ecommerce.common.exceptions.ApplicationException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Color name already exists!"
            );
        }

        try {
            color.setName(name);
            color.setCode(code);
            color.setDescription(request.getDescription());
            return colorRepository.save(color);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new com.zyna.dev.ecommerce.common.exceptions.ApplicationException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Color code or name already exists!"
            );
        }
    }

    @Transactional
    public void deleteColor(Long id) {
        if (!colorRepository.existsById(id)) {
            throw new com.zyna.dev.ecommerce.common.exceptions.ApplicationException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Color not found!"
            );
        }
        colorRepository.deleteById(id);
    }
}

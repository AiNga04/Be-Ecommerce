package com.zyna.dev.ecommerce.products.service;

import com.zyna.dev.ecommerce.products.dto.request.SizeRequest;
import com.zyna.dev.ecommerce.products.models.Size;
import com.zyna.dev.ecommerce.products.repository.SizeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SizeService {

    private final SizeRepository sizeRepository;

    public List<Size> getAllSizes() {
        return sizeRepository.findAll();
    }

    public Size getSizeById(Long id) {
        return sizeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kích thước với id: " + id));
    }

    @Transactional
    public Size createSize(SizeRequest request) {
        String code = request.getCode() != null ? request.getCode().trim() : null;
        String name = request.getName() != null ? request.getName().trim() : null;

        if (sizeRepository.findByCode(code).isPresent()) {
            throw new com.zyna.dev.ecommerce.common.exceptions.ApplicationException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Mã kích thước đã tồn tại"
            );
        }
        if (sizeRepository.findByName(name).isPresent()) {
            throw new com.zyna.dev.ecommerce.common.exceptions.ApplicationException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Tên kích thước đã tồn tại"
            );
        }

        try {
            Size size = Size.builder()
                    .name(name)
                    .code(code)
                    .description(request.getDescription())
                    .build();
            return sizeRepository.save(size);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new com.zyna.dev.ecommerce.common.exceptions.ApplicationException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Mã hoặc tên kích thước đã tồn tại"
            );
        }
    }

    @Transactional
    public Size updateSize(Long id, SizeRequest request) {
        Size size = getSizeById(id);
        String code = request.getCode() != null ? request.getCode().trim() : null;
        String name = request.getName() != null ? request.getName().trim() : null;

        if (!size.getCode().equals(code) && sizeRepository.findByCode(code).isPresent()) {
            throw new com.zyna.dev.ecommerce.common.exceptions.ApplicationException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Mã kích thước đã tồn tại"
            );
        }
        if (!size.getName().equals(name) && sizeRepository.findByName(name).isPresent()) {
            throw new com.zyna.dev.ecommerce.common.exceptions.ApplicationException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Tên kích thước đã tồn tại"
            );
        }

        try {
            size.setName(name);
            size.setCode(code);
            size.setDescription(request.getDescription());
            return sizeRepository.save(size);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new com.zyna.dev.ecommerce.common.exceptions.ApplicationException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Mã hoặc tên kích thước đã tồn tại"
            );
        }
    }

    @Transactional
    public void deleteSize(Long id) {
        if (!sizeRepository.existsById(id)) {
            throw new com.zyna.dev.ecommerce.common.exceptions.ApplicationException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Không tìm thấy kích thước"
            );
        }
        sizeRepository.deleteById(id);
    }
}

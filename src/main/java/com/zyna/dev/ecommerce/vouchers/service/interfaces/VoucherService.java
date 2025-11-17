package com.zyna.dev.ecommerce.vouchers.service.interfaces;

import com.zyna.dev.ecommerce.vouchers.dto.request.VoucherApplyRequest;
import com.zyna.dev.ecommerce.vouchers.dto.request.VoucherCreateRequest;
import com.zyna.dev.ecommerce.vouchers.dto.request.VoucherUpdateRequest;
import com.zyna.dev.ecommerce.vouchers.dto.response.VoucherApplyResponse;
import com.zyna.dev.ecommerce.vouchers.dto.response.VoucherResponse;
import org.springframework.data.domain.Page;

public interface VoucherService {

    VoucherResponse create(VoucherCreateRequest request);

    VoucherResponse update(Long id, VoucherUpdateRequest request);

    void deactivate(Long id);

    void activate(Long id);

    VoucherResponse getById(Long id);

    Page<VoucherResponse> list(int page, int size);

    Page<VoucherResponse> listActive(int page, int size); // 👈 thêm

    VoucherApplyResponse apply(VoucherApplyRequest request);
}



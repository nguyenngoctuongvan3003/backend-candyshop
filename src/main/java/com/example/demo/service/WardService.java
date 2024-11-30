package com.example.demo.service;

import com.example.demo.dto.PagedResponseDTO;
import com.example.demo.dto.WardRequestDTO;
import com.example.demo.exception.ResourceConflictException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Ward;

public interface WardService {

    /**
     * Tạo mới một phường trong một quận.
     * 
     * @param districtId        ID của quận nơi phường được tạo.
     * @param wardRequestDTO    Dữ liệu yêu cầu để tạo phường.
     * @return Ward             Thực thể phường vừa được tạo.
     * @throws Exception        Các lỗi không xác định.
     * @throws ResourceNotFoundException Khi không tìm thấy quận.
     * @throws ResourceConflictException Khi tên phường đã tồn tại trong quận.
     */
    Ward createWard(String districtId, WardRequestDTO wardRequestDTO) 
            throws Exception, ResourceNotFoundException, ResourceConflictException;

    /**
     * Cập nhật thông tin phường.
     * 
     * @param wardId            ID của phường cần cập nhật.
     * @param wardRequestDTO    Dữ liệu yêu cầu để cập nhật phường.
     * @return Ward             Thực thể phường vừa được cập nhật.
     * @throws Exception        Các lỗi không xác định.
     * @throws ResourceNotFoundException Khi không tìm thấy phường.
     * @throws ResourceConflictException Khi tên phường mới đã tồn tại trong quận.
     */
    Ward updateWard(String wardId, WardRequestDTO wardRequestDTO) 
            throws Exception, ResourceNotFoundException, ResourceConflictException;

    /**
     * Xóa phường theo ID.
     * 
     * @param wardId ID của phường cần xóa.
     * @throws Exception        Các lỗi không xác định.
     * @throws ResourceNotFoundException Khi không tìm thấy phường.
     */
    void deleteWard(String wardId) throws Exception, ResourceNotFoundException;

    /**
     * Lấy thông tin phường theo ID.
     * 
     * @param wardId ID của phường.
     * @return Ward  Thực thể phường được tìm thấy.
     * @throws Exception        Các lỗi không xác định.
     * @throws ResourceNotFoundException Khi không tìm thấy phường.
     */
    Ward getWard(String wardId) throws Exception, ResourceNotFoundException;

    /**
     * Lấy danh sách các phường thuộc một quận, hỗ trợ phân trang.
     * 
     * @param districtId ID của quận.
     * @param page       Số trang.
     * @param limit      Kích thước trang.
     * @param sortField  Trường cần sắp xếp.
     * @param sortOrder  Thứ tự sắp xếp ("asc" hoặc "desc").
     * @return PagedResponseDTO<Ward> Kết quả phân trang các phường.
     * @throws Exception Các lỗi không xác định.
     */
    PagedResponseDTO<Ward> getWardsByDistrictId(String districtId, int page, int limit, String sortField, String sortOrder) 
            throws Exception;

}

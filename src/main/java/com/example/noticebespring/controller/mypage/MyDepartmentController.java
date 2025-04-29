package com.example.noticebespring.controller.mypage;

import com.example.noticebespring.common.response.CommonResponse;
import com.example.noticebespring.common.response.CustomException;
import com.example.noticebespring.common.response.ErrorCode;
import com.example.noticebespring.dto.mypage.bookmark.DepartmentDto;
import com.example.noticebespring.service.auth.UserService;
import com.example.noticebespring.service.mypage.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/mypage/department")
public class MyDepartmentController {

    private final DepartmentService departmentService;
    private final UserService userService;

    @GetMapping("/")
    public CommonResponse<List<DepartmentDto>> getDepartmentList(){
        Integer userId = userService.getAuthenticatedUser().getId();
        try{
            List<DepartmentDto> departmentList = departmentService.getDepartment(userId);
            return CommonResponse.ok(departmentList);
        } catch (Exception e) {
            return CommonResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/")
    public CommonResponse<DepartmentDto> addDepartment(@RequestBody DepartmentDto departmentDto){
        Integer userId = userService.getAuthenticatedUser().getId();
        try {
            //단과대학 이름 추출
            String collegeName = departmentDto.collegeName();
            if(collegeName.isEmpty() || collegeName.isBlank()){
                return CommonResponse.fail(ErrorCode.INVALID_COLLEGE_NAME);
            }

            //학과 이름 추출
            String departmentName = departmentDto.departmentName();
            if(departmentName.isEmpty() || collegeName.isBlank()){
                return CommonResponse.fail(ErrorCode.INVALID_DEPARTMENT_NAME);
            }

            //단과대학 및 학과 정보 추가
            DepartmentDto newDepartment = departmentService.addDepartment(userId, collegeName, departmentName);
            return CommonResponse.created(newDepartment);
        } catch (CustomException e) {
            if(e.getErrorCode().equals(ErrorCode.NOT_FOUND_USER)){
                return CommonResponse.fail(ErrorCode.NOT_FOUND_USER);
            }
            else if(e.getErrorCode().equals(ErrorCode.EXISTS_ALREADY_DEPARTMENT)){
                return CommonResponse.fail(ErrorCode.EXISTS_ALREADY_DEPARTMENT);
            }
            else{
                return CommonResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e){
            return CommonResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/")
    public CommonResponse<Void> removeDepartment(@RequestBody DepartmentDto departmentDto){
        Integer userId = userService.getAuthenticatedUser().getId();
        try{
            String collegeName = departmentDto.collegeName();
            String departmentName = departmentDto.departmentName();

            departmentService.removeDepartment(userId, collegeName, departmentName);
            return CommonResponse.ok(null);
        } catch (CustomException e) {
            if(e.getErrorCode().equals(ErrorCode.NOT_FOUND_USER)){
                return CommonResponse.fail(ErrorCode.NOT_FOUND_USER);
            }
            else if(e.getErrorCode().equals(ErrorCode.EXISTS_ALREADY_DEPARTMENT)){
                return CommonResponse.fail(ErrorCode.EXISTS_ALREADY_DEPARTMENT);
            }
            else{
                return CommonResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e){
            return CommonResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}

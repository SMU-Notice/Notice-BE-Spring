package com.example.noticebespring.controller.mypage;

import com.example.noticebespring.common.response.CommonResponse;
import com.example.noticebespring.common.response.CustomException;
import com.example.noticebespring.common.response.ErrorCode;
import com.example.noticebespring.dto.mypage.DepartmentDto;
import com.example.noticebespring.service.auth.UserService;
import com.example.noticebespring.service.mypage.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage/department")
@Tag(name = "회원정보(학과) API", description = "마이페이지의 회원 정보 내 학과 조회, 추가, 제거 API")
public class MyDepartmentController {
    private final DepartmentService departmentService;
    private final UserService userService;

    @Operation(
            summary = "학과 정보 조회",
            description = "사용자가 등록한 모든 학과를 담은 리스트 조회(단과대학 이름, 학과 이름)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "학과 리스트 조회 성공",
                            content = {@Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                            {
                                                "success": true,
                                                "data": [{
                                                    "collegeName" : "융합공과대학",
                                                    "departmentName" : "컴퓨터과학전공"
                                                },
                                                {
                                                    "collegeName" : "경영경제대학",
                                                    "departmentName" : "경제학부"
                                                }],
                                                "error": null
                                            }
                                        """)
                            )
                    }),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                            content = {@Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example =  """
                                                    {
                                                        "success": false,
                                                        "data": null,
                                                        "error": {
                                                            "errorCode": "50000",
                                                            "message": "서버 내부 오류입니다."
                                                        }
                                                    }
                                                    """)
                            )

                            }),
            }
    )
    @GetMapping("")
    public CommonResponse<List<DepartmentDto>> getDepartmentList(){
        Integer userId = userService.getAuthenticatedUser().getId();
        try{
            List<DepartmentDto> departmentList = departmentService.getDepartment(userId);
            return CommonResponse.ok(departmentList);
        } catch (Exception e) {
            return CommonResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(
            summary = "학과 추가",
            description = "사용자가 '단과대학 - 학과' 의 형식으로 학과 추가",
            responses = {
                    @ApiResponse(responseCode = "200", description = "학과 추가 성공",
                            content = {@Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                            {
                                                "success": true,
                                                "data": {
                                                    "collegeName" : "융합공과대학",
                                                    "departmentName" : "컴퓨터과학전공"
                                                },
                                                "error": null
                                            }
                                        """)
                            )
                            }),
                    @ApiResponse(responseCode = "400", description = "유효하지 않은 단과대학 이름",
                            content = {@Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                            {
                                                "success": false,
                                                "data": null,
                                                "error": {
                                                    "errorCode": "40008",
                                                    "message": "단과대학 이름이 유효하지 않습니다."
                                                }
                                            }
                                        """)
                            )
                            }),
                    @ApiResponse(responseCode = "400", description = "유효하지 않은 학과 이름",
                            content = {@Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                            {
                                                "success": false,
                                                "data": null,
                                                "error": {
                                                    "errorCode": "40009",
                                                    "message": "학과 이름이 유효하지 않습니다."
                                                }
                                            }
                                        """)
                            )
                            }),
                    @ApiResponse(responseCode = "409", description = "동일한 이름의 학과가 이미 추가됨",
                            content = {@Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                            {
                                                "success": false,
                                                "data": null,
                                                "error": {
                                                    "errorCode": "40903",
                                                    "message": "이미 추가된 학과입니다."
                                                }
                                            }
                                        """)
                            )
                            }),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                            content = {@Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example =  """
                                                    {
                                                        "success": false,
                                                        "data": null,
                                                        "error": {
                                                            "errorCode": "50000",
                                                            "message": "서버 내부 오류입니다."
                                                        }
                                                    }
                                                    """)
                            )

                            }),
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "추가할 학과 정보",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DepartmentDto.class),
                            examples = @ExampleObject(
                                    value = """
                                        {
                                            "collegeName": "융합공과대학",
                                            "departmentName": "컴퓨터과학전공"
                                        }
                                        """
                            )
                    )
            )
    )
    @PostMapping("")
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


    @Operation(
            summary = "학과 제거",
            description = "사용자의 요청을 바탕으로 학과 제거",
            responses = {
                    @ApiResponse(responseCode = "200", description = "학과 제거 성공",
                            content = {@Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                            {
                                                "success": true,
                                                "data": null,
                                                "error": null
                                            }
                                        """)
                            )
                            }),
                    @ApiResponse(responseCode = "400", description = "유효하지 않은 단과대학 이름",
                            content = {@Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                            {
                                                "success": false,
                                                "data": null,
                                                "error": {
                                                    "errorCode": "40008",
                                                    "message": "단과대학 이름이 유효하지 않습니다."
                                                }
                                            }
                                        """)
                            )
                            }),
                    @ApiResponse(responseCode = "400", description = "유효하지 않은 학과 이름",
                            content = {@Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = """
                                            {
                                                "success": false,
                                                "data": null,
                                                "error": {
                                                    "errorCode": "40009",
                                                    "message": "학과 이름이 유효하지 않습니다."
                                                }
                                            }
                                        """)
                            )
                            }),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                            content = {@Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example =  """
                                                    {
                                                        "success": false,
                                                        "data": null,
                                                        "error": {
                                                            "errorCode": "50000",
                                                            "message": "서버 내부 오류입니다."
                                                        }
                                                    }
                                                    """)
                            )

                            }),
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "제거할 학과 정보",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DepartmentDto.class),
                            examples = @ExampleObject(
                                    value = """
                                        {
                                            "collegeName": "융합공과대학",
                                            "departmentName": "컴퓨터과학전공"
                                        }
                                        """
                            )
                    )
            )
    )
    @DeleteMapping("")
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
            else{
                return CommonResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e){
            return CommonResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}

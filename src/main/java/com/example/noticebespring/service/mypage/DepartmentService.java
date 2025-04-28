package com.example.noticebespring.service.mypage;

import com.example.noticebespring.common.response.CustomException;
import com.example.noticebespring.common.response.ErrorCode;
import com.example.noticebespring.dto.mypage.bookmark.DepartmentDto;
import com.example.noticebespring.entity.College;
import com.example.noticebespring.entity.Department;
import com.example.noticebespring.repository.CollegeRepository;
import com.example.noticebespring.repository.DepartmentRepository;
import com.example.noticebespring.repository.UserRepository;
import com.example.noticebespring.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;




@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentService {
    private final UserRepository userRepository;
    private final CollegeRepository collegeRepository;
    private final DepartmentRepository departmentRepository;


    // 사용자의 학과 정보 불러오기
    @Transactional(readOnly = true)
    public List<DepartmentDto> getDepartment(Integer userId){
        log.info("사용자의 학과 정보 조회 시작 : userId: {}", userId);
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

            // 등록된 학과 리스트 조회
            List<DepartmentDto> addedDepartments = new ArrayList<>();
            for (Department department : user.getDepartmentList()) {
                addedDepartments.add(new DepartmentDto(department.getCollege().getName(), department.getName()));
            }
            return addedDepartments;
        } catch (Exception e) {
            log.error("학과 정보 조회 과정에서 오류 발생: {}", userId, e);
            throw e;
        }
    }

    //사용자의 학과 추가
    @Transactional
    public DepartmentDto addDepartment(Integer userId, String collegeName, String departmentName){
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

            // 단과대학 이름으로 조회 후 없으면 등록
            College college = collegeRepository.findByName(collegeName)
                    .orElseGet(() -> {
                        College newCollege = College.builder()
                                .name(collegeName)
                                .build();
                        return collegeRepository.save(newCollege);
                    });

            // 학과 이름을 조회 후 없으면 등록
            Department department = departmentRepository.findByNameAndCollege(departmentName, college)
                    .orElseGet(() -> {
                        Department newDepartment = Department.builder()
                                .name(departmentName)
                                .college(college)
                                .build();
                        return departmentRepository.save(newDepartment);
                    });

            // 이미 등록된 학과 이름인지 확인
            if (user.getDepartmentList().contains(department)) {
                throw new CustomException(ErrorCode.EXISTS_ALREADY_DEPARTMENT);
            }

            //학과와 사용자의 관계 형성
            user.addDepartment(department);
            log.info("학과 추가 성공: userId: {}, college: {}, department: {}"
                    ,userId, collegeName, departmentName);

            userRepository.save(user);

            return new DepartmentDto(collegeName, departmentName);

        } catch(Exception e){
            log.error("단과대학과 학과 이름 등록 중 오류 발생: userId: {}, college: {}, department: {}",
                    userId, collegeName, departmentName);
            throw e;
        }
    }

    @Transactional
    public void removeDepartment(Integer userId, String collegeName, String departmentName){
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

            College college = collegeRepository.findByName(collegeName)
                    .orElseThrow(() -> new CustomException(ErrorCode.INVALID_COLLEGE_NAME));

            Department department = departmentRepository.findByNameAndCollege(departmentName, college)
                    .orElseThrow(() -> new CustomException(ErrorCode.INVALID_DEPARTMENT_NAME));

            user.removeDepartment(department);
            log.info("학과 제거 성공: userId: {}, college: {}, department: {}"
                    ,userId, collegeName, departmentName);
            userRepository.save(user);
        } catch (Exception e){
            log.error("학과 정보 제거 중 오류 발생: userId: {}, college: {}, department: {}",
                    userId, collegeName, departmentName);
            throw e;
        }
    }
}

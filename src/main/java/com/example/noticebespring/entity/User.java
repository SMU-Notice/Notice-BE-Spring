package com.example.noticebespring.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Setter
    @Column(nullable = false, length = 255, unique = true)
    private String email;  // 이메일 (고유 값)

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;  // 생성 시각

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now(); // 생성 시 자동 설정
    }

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BookmarkFolder> bookmarkFolderList = new ArrayList<>();

    @ManyToMany
    @JoinTable( name = "user_department",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "department_id"))
    @Builder.Default
    private List<Department> departmentList = new ArrayList<>();

    public void addDepartment(Department department){
        if(!departmentList.contains(department)){
            departmentList.add(department);
            department.getUserList().add(this);
        }
    }

    public void removeDepartment(Department department){
        if(departmentList.remove(department)){
            department.getUserList().remove(this);
        }
    }


}

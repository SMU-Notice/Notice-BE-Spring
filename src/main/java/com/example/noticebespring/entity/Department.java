package com.example.noticebespring.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "department")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer Id;

    @Column(length = 30, nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    @ManyToMany(mappedBy = "departmentList")
    @Builder.Default
    private List<User> userList = new ArrayList<>();

    public void addUser(User user){
        if(!userList.contains(user)){
            userList.add(user);
            user.getDepartmentList().add(this);
        }
    }

    public void removeUser(User user){
        if(userList.remove(user)){
            user.getDepartmentList().remove(this);
        }
    }

}

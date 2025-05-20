package com.booktalk_be.domain.auth.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@Table(name = "member")
public class Member {
    @PrePersist
    public void generateId() {
        this.authority = AuthorityType.COMMON;
        delYn = false;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id", nullable = false)
    private int memberId;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "auth_type", nullable = false)
    @Convert(converter = AuthenticateType.Converter.class)
    private AuthenticateType authType;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "address")
    private String address;

    @Column(name = "gender")
    private String gender;

    @Column(name = "birth")
    private LocalDate birth;

    @Column(name = "authority")
    private AuthorityType authority;

    @Column(name = "del_yn", nullable = false)
    private Boolean delYn;

    @Builder
    public Member(String email, String name, AuthenticateType authType, String password, String phoneNumber, String address, String gender, LocalDate birth) {
        this.email = email;
        this.name = name;
        this.authType = authType;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.gender = gender;
        this.birth = birth;
    }
}

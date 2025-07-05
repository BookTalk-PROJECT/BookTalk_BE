package com.booktalk_be.domain.gathering.model.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@Table(name = "gathering") //모임 엔티티
public class Gathering{

    @Id
    @Column(name = "gathering_code", nullable = false) //모임 코드
    protected String code;

    //PK: GA_(prefix) + number
    @PrePersist
    public void generateId(){
        if(this.code == null){
            this.code = "GA_"+System.currentTimeMillis();
        }
        if(this.delYn == null) {
            this.delYn = false;
        }
        if(this.status == null){
            this.status = GatheringStatus.INTENDED;
        }
    }

    @Column(name = "status") //모임 상태
    private GatheringStatus status;

    @Column(name = "name") //모임 이름
    private String name;

    @Column(name = "recruit_info", nullable = false) //모임 정보 (여기 기타 정보들 다 들어감)
    private String recruitInfo;

    @Column(name = "emd_cd") //읍면동 코드
    private String emdCd;

    @Column(name = "sig_cd") // 행정구역 코드
    private String sigCd;

    @Lob
    @Column(name = "image_data", columnDefinition = "LONGBLOB")
    private byte[] imageData;

    @Column(name = "del_yn", nullable = false) //삭제여부
    private Boolean delYn;

    @Column(name = "summary", nullable = false) // 모임소개
    private String summary;

    @Column(name = "del_reason") //삭제사유
    private String delReason;

    @Builder
    public Gathering(String name, String recruitInfo, String emdCd, String sigCd,byte[] imageData, String summary, GatheringStatus status) {
        this.name = name;
        this.recruitInfo = recruitInfo;
        this.emdCd = emdCd;
        this.sigCd = sigCd;
        this.imageData = imageData;
        this.summary = summary;
        this.status = status;
    }
}

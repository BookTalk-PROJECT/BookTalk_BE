package com.booktalk_be.domain.gathering.model.entity;


import com.booktalk_be.common.entity.CommonEntity;
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
public class Gathering extends CommonEntity {

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

    @Column(name = "recruitment_personnel", nullable = false) //모집 인원수
    private Long recruitmentPersonnel;

    @Column(name = "recruitment_period", nullable = false) //모집 기간
    private String recruitmentPeriod;

    @Column(name = "activity_period", nullable = false) //활동 기간
    private String activityPeriod;

    @Column(name = "emd_cd") //읍면동 코드
    private String emdCd;

    @Column(name = "sig_cd") // 행정구역 코드
    private String sigCd;

    @Column(name = "image_url") // 이미지 경로 Url 방식
    private String imageUrl;

    @Column(name = "del_yn", nullable = false) //삭제여부
    private Boolean delYn;

    @Column(name = "summary", nullable = false) // 모임소개
    private String summary;

    @Column(name = "del_reason") //삭제사유
    private String delReason;

    @Builder
    public Gathering(String name,
                     Long recruitmentPersonnel,
                     String recruitmentPeriod,
                     String activityPeriod,
                     String emdCd,
                     String sigCd,
                     String imageData,
                     String summary,
                     GatheringStatus status) {
        this.name = name;
        this.recruitmentPersonnel = recruitmentPersonnel;
        this.recruitmentPeriod = recruitmentPeriod;
        this.activityPeriod = activityPeriod;
        this.emdCd = emdCd;
        this.sigCd = sigCd;
        this.imageUrl = imageData;
        this.summary = summary;
        this.status = status;
    }

    public void updateCore(String name,
                           Long recruitmentPersonnel,
                           String recruitmentPeriod,
                           String activityPeriod,
                           String location,
                           String summary,
                           GatheringStatus status) {

        if (name != null) {
            this.name = name;
        }
        if (recruitmentPersonnel != null) {
            this.recruitmentPersonnel = recruitmentPersonnel;
        }
        if (recruitmentPeriod != null) {
            this.recruitmentPeriod = recruitmentPeriod;
        }
        if (activityPeriod != null) {
            this.activityPeriod = activityPeriod;
        }
        if (location != null) {
            // 생성 로직과 포맷 맞추기
            this.sigCd = location;
            this.emdCd = location + "_읍면동 코드";
        }
        if (summary != null) {
            this.summary = summary;
        }
        if (status != null) {
            this.status = status;
        }
    }

    public void changeImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}

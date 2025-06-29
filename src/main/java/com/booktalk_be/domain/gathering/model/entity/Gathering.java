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
    @Column(name = "gathering_code", nullable = false)
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

    @Column(name = "status")
    private GatheringStatus status;

    @Column(name = "name")
    private String name;

    @Column(name = "recruit_info", nullable = false)
    private String recruitInfo;

    @Column(name = "emd_cd")
    private String emdCd;

    @Column(name = "sig_cd")
    private String sigCd;

    @Column(name = "del_yn", nullable = false)
    private Boolean delYn;

    @Column(name = "summary", nullable = false)
    private String summary;

    @Column(name = "del_reason")
    private String delReason;

    @Builder
    public Gathering(String name, String recruitInfo, String emdCd, String sigCd, String summary) {
        this.name = name;
        this.recruitInfo = recruitInfo;
        this.emdCd = emdCd;
        this.sigCd = sigCd;
        this.summary = summary;
    }
}

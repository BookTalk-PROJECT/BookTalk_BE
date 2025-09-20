package com.booktalk_be.domain.hashtag.model.entity;

import com.booktalk_be.domain.gathering.model.entity.Gathering;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
public class HashTagId implements Serializable{

    private Gathering code;
    private HashTag hashtagId;

}
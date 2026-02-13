package com.booktalk_be.springconfig.querydsl;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;
import org.hibernate.type.StandardBasicTypes;

/**
 * MySQL FULLTEXT 검색을 위한 Hibernate 커스텀 함수 등록.
 * HQL에서 MATCH...AGAINST 구문을 직접 사용할 수 없으므로
 * function('match_against', column, keyword) 형태로 호출할 수 있도록 등록한다.
 */
public class FullTextFunctionContributor implements FunctionContributor {

    @Override
    public void contributeFunctions(FunctionContributions functionContributions) {
        functionContributions.getFunctionRegistry()
                .patternDescriptorBuilder("match_against",
                        "MATCH(?1) AGAINST(?2 IN BOOLEAN MODE)")
                .setInvariantType(
                        functionContributions.getTypeConfiguration()
                                .getBasicTypeRegistry()
                                .resolve(StandardBasicTypes.DOUBLE))
                .setExactArgumentCount(2)
                .register();
    }
}

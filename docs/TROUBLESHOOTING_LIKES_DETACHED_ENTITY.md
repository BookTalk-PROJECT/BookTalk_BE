# [ 트러블 슈팅 ] 좋아요 기능 Detached Entity 오류

## 문제

- 게시글 좋아요 추가 시 `PersistentObjectException` 발생
  ```
  org.hibernate.PersistentObjectException: detached entity passed to persist:
  com.booktalk_be.domain.member.model.entity.Member
  ```
- Security Filter에서 조회하여 Principal에 저장한 Member 엔티티를 서비스에서 사용할 때 오류 발생
- 좋아요 추가/삭제 기능 전면 동작 불가

## 원인

- **Security Filter와 Service의 영속성 컨텍스트 불일치**

```
[요청 흐름]

┌─ Filter Chain ─────────────────────────────────┐
│  JwtAuthFilter                                 │
│  └─ memberService.getMemberById() 호출         │  ← 트랜잭션 A (조회 후 종료)
│  └─ Member 조회 → Principal에 저장             │
│  └─ 트랜잭션 종료 → Member는 detached 상태      │
└────────────────────────────────────────────────┘
                      ↓
┌─ Service Layer ────────────────────────────────┐
│  @Transactional LikesService.addLike()         │  ← 트랜잭션 B (새로운 영속성 컨텍스트)
│  └─ Likes.create(code, detachedMember)         │
│  └─ likesRepository.save(likes)                │
│  └─ Hibernate가 Member도 persist 시도 → 에러!  │
└────────────────────────────────────────────────┘
```

- Filter는 DispatcherServlet 이전에 실행되어 Service의 `@Transactional` 범위 밖에 위치
- OSIV(Open Session In View)가 활성화되어 있어도 Spring Boot 기본 구현인 `OpenEntityManagerInViewInterceptor`는 DispatcherServlet 이후에 동작하므로 Filter에서 조회한 엔티티는 OSIV 영속성 컨텍스트와 무관
- Likes 엔티티가 `@ManyToOne`으로 Member를 참조하고 있어 save 시 Hibernate가 cascade persist 시도

## 해결

- `MemberRepository.getReferenceById()`를 사용하여 현재 트랜잭션의 영속성 컨텍스트에서 관리되는 Member 프록시 참조 획득

```java
// LikesServiceImpl.java

@Override
public void addLike(String code, Member member) {
    if (likesRepository.existsByCodeAndMemberId(code, member.getMemberId())) {
        return;
    }

    // 영속 상태의 Member 참조 획득 (DB 조회 없이 프록시만 생성)
    Member managedMember = memberRepository.getReferenceById(member.getMemberId());

    Likes like = Likes.create(code, managedMember);
    likesRepository.save(like);

    updateLikeCount(code, true);
}
```

- `getReferenceById()`는 실제 DB 조회 없이 프록시만 생성하므로 성능 오버헤드 없음
- 동일한 패턴을 `removeLike()` 메서드에도 적용

## 결과

| 항목 | 기존 | 개선 |
|------|------|------|
| 좋아요 추가 | `PersistentObjectException` 발생 | 정상 동작 |
| 좋아요 삭제 | 동일 오류 가능성 | 정상 동작 |
| 성능 | - | DB 추가 조회 없음 (프록시 사용) |

## 교훈

1. **Filter에서 조회한 엔티티는 Service에서 detached 상태**
   - Security Filter는 `@Transactional` 범위 밖에서 실행됨
   - Principal에 엔티티를 저장하면 이후 Service에서 영속성 문제 발생 가능

2. **OSIV는 Filter 단계를 커버하지 않음**
   - Spring Boot 기본 OSIV는 Interceptor 방식으로 DispatcherServlet 이후에 동작
   - Filter에서 조회한 엔티티는 OSIV 영속성 컨텍스트와 별개

3. **대안적 설계 고려사항**
   - Principal에 엔티티 대신 ID만 저장하는 방식 검토
   - 또는 DTO로 변환하여 저장하는 방식 검토

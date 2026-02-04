package com.booktalk_be.performance.data;

import com.booktalk_be.domain.auth.model.entity.AuthenticateType;
import com.booktalk_be.domain.board.model.entity.Board;
import com.booktalk_be.domain.bookreview.model.entity.BookReview;
import com.booktalk_be.domain.category.model.entity.Category;
import com.booktalk_be.domain.likes.model.entity.Likes;
import com.booktalk_be.domain.member.model.entity.Member;
import com.booktalk_be.domain.reply.model.entity.Reply;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Dummy Data Generator for Performance Testing
 *
 * Data Distribution (Total ~1M records):
 * - Member: 1,000
 * - Category: 100 (20 root + 80 children)
 * - Board: 100,000
 * - BookReview: 100,000
 * - Reply: 700,000 (200K root + 300K depth-1 + 200K depth-2)
 * - Likes: 100,000
 *
 * Run order matters - execute tests in order by @Order annotation
 *
 * Usage:
 * ./gradlew test --tests "*.DummyDataGeneratorTest.generateMembers" -Dspring.profiles.active=dev
 */
@SpringBootTest
@ActiveProfiles("dev")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DummyDataGeneratorTest {

    private static final Logger log = LoggerFactory.getLogger(DummyDataGeneratorTest.class);

    private static final int BATCH_SIZE = 1000;
    private static final int LOG_INTERVAL = 10000;

    private static final int MEMBER_COUNT = 1000;
    private static final int ROOT_CATEGORY_COUNT = 20;
    private static final int CHILD_CATEGORY_COUNT = 80;
    private static final int BOARD_COUNT = 100000;
    private static final int BOOK_REVIEW_COUNT = 100000;
    private static final int ROOT_REPLY_COUNT = 200000;
    private static final int DEPTH1_REPLY_COUNT = 300000;
    private static final int DEPTH2_REPLY_COUNT = 200000;
    private static final int LIKES_COUNT = 100000;

    @PersistenceContext
    private EntityManager em;

    private final Random random = new Random(42); // Fixed seed for reproducibility

    // Stored IDs for reference in subsequent tests
    private List<Integer> memberIds = new ArrayList<>();
    private List<Integer> categoryIds = new ArrayList<>();
    private List<String> boardCodes = new ArrayList<>();
    private List<String> bookReviewCodes = new ArrayList<>();
    private List<String> rootReplyCodes = new ArrayList<>();
    private List<String> depth1ReplyCodes = new ArrayList<>();

    @Test
    @Order(1)
    @Transactional
    @Commit
    @DisplayName("1. Generate 1,000 Members")
    void generateMembers() {
        Long existingCount = em.createQuery("SELECT COUNT(m) FROM Member m", Long.class).getSingleResult();
        if (existingCount >= MEMBER_COUNT) {
            log.info("=== Skipping Member Generation: {} already exist (target: {}) ===", existingCount, MEMBER_COUNT);
            return;
        }
        log.info("=== Starting Member Generation: {} records (existing: {}) ===", MEMBER_COUNT, existingCount);
        long startTime = System.currentTimeMillis();

        int startFrom = existingCount.intValue() + 1;
        for (int i = startFrom; i <= MEMBER_COUNT; i++) {
            Member member = Member.builder()
                    .email("testuser" + i + "@booktalk.com")
                    .name("TestUser" + i)
                    .authType(AuthenticateType.OWN)
                    .password("$2a$10$dummyhashedpassword" + i)
                    .phoneNumber("010-" + String.format("%04d", i / 100) + "-" + String.format("%04d", i % 10000))
                    .address("Test Address " + i)
                    .gender(i % 2 == 0 ? "M" : "F")
                    .build();

            em.persist(member);

            if (i % BATCH_SIZE == 0) {
                em.flush();
                em.clear();
                log.debug("Flushed batch at {}", i);
            }
            if (i % LOG_INTERVAL == 0) {
                log.info("Progress: {}/{} members", i, MEMBER_COUNT);
            }
        }

        em.flush();
        em.clear();

        long duration = System.currentTimeMillis() - startTime;
        log.info("=== Member Generation Complete: {} records in {}ms ===", MEMBER_COUNT, duration);
    }

    @Test
    @Order(2)
    @Transactional
    @Commit
    @DisplayName("2. Generate 100 Categories (20 root + 80 children)")
    void generateCategories() {
        Long existingCount = em.createQuery("SELECT COUNT(c) FROM Category c", Long.class).getSingleResult();
        if (existingCount >= ROOT_CATEGORY_COUNT + CHILD_CATEGORY_COUNT) {
            log.info("=== Skipping Category Generation: {} already exist ===", existingCount);
            return;
        }
        log.info("=== Starting Category Generation: {} root + {} children ===",
                ROOT_CATEGORY_COUNT, CHILD_CATEGORY_COUNT);
        long startTime = System.currentTimeMillis();

        // Create root categories
        List<Integer> rootIds = new ArrayList<>();
        for (int i = 1; i <= ROOT_CATEGORY_COUNT; i++) {
            Category category = new Category(
                    "RootCategory" + i,
                    true,
                    null  // no parent
            );
            em.persist(category);
            em.flush();
            rootIds.add(category.getCategoryId());
        }
        log.info("Created {} root categories", ROOT_CATEGORY_COUNT);

        em.clear();

        // Create child categories distributed among root categories
        for (int i = 1; i <= CHILD_CATEGORY_COUNT; i++) {
            Integer parentId = rootIds.get((i - 1) % ROOT_CATEGORY_COUNT);
            Category category = new Category(
                    "ChildCategory" + i,
                    true,
                    parentId
            );
            em.persist(category);

            if (i % BATCH_SIZE == 0) {
                em.flush();
                em.clear();
            }
        }

        em.flush();
        em.clear();

        long duration = System.currentTimeMillis() - startTime;
        log.info("=== Category Generation Complete: {} records in {}ms ===",
                ROOT_CATEGORY_COUNT + CHILD_CATEGORY_COUNT, duration);
    }

    @Test
    @Order(3)
    @Transactional
    @Commit
    @DisplayName("3. Generate 100,000 Boards")
    void generateBoards() {
        Long existingCount = em.createQuery("SELECT COUNT(b) FROM Board b", Long.class).getSingleResult();
        if (existingCount >= BOARD_COUNT) {
            log.info("=== Skipping Board Generation: {} already exist ===", existingCount);
            return;
        }
        log.info("=== Starting Board Generation: {} records (existing: {}) ===", BOARD_COUNT, existingCount);
        long startTime = System.currentTimeMillis();

        // Load member IDs
        List<Integer> memberIdList = em.createQuery(
                "SELECT m.memberId FROM Member m WHERE m.delYn = false", Integer.class)
                .getResultList();

        // Load category IDs
        List<Integer> categoryIdList = em.createQuery(
                "SELECT c.categoryId FROM Category c WHERE c.delYn = false", Integer.class)
                .getResultList();

        if (memberIdList.isEmpty() || categoryIdList.isEmpty()) {
            log.error("No members or categories found. Run generateMembers and generateCategories first.");
            return;
        }

        log.info("Loaded {} members and {} categories for reference",
                memberIdList.size(), categoryIdList.size());

        for (int i = 1; i <= BOARD_COUNT; i++) {
            Member memberRef = em.getReference(Member.class,
                    memberIdList.get(random.nextInt(memberIdList.size())));
            Integer categoryId = categoryIdList.get(random.nextInt(categoryIdList.size()));

            Board board = Board.builder()
                    .member(memberRef)
                    .categoryId(categoryId)
                    .title("Performance Test Board Title " + i + " - Lorem ipsum dolor sit amet")
                    .content("Performance test content for board " + i + ". " +
                            "This is a longer content to simulate real-world scenarios. " +
                            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                            "Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.")
                    .delYn(false)
                    .notificationYn(i % 100 == 0) // 1% are notifications
                    .build();

            em.persist(board);

            if (i % BATCH_SIZE == 0) {
                em.flush();
                em.clear();
            }
            if (i % LOG_INTERVAL == 0) {
                log.info("Progress: {}/{} boards", i, BOARD_COUNT);
            }
        }

        em.flush();
        em.clear();

        long duration = System.currentTimeMillis() - startTime;
        log.info("=== Board Generation Complete: {} records in {}ms ({} records/sec) ===",
                BOARD_COUNT, duration, BOARD_COUNT * 1000L / duration);
    }

    @Test
    @Order(4)
    @Transactional
    @Commit
    @DisplayName("4. Generate 100,000 BookReviews")
    void generateBookReviews() {
        Long existingCount = em.createQuery("SELECT COUNT(br) FROM BookReview br", Long.class).getSingleResult();
        if (existingCount >= BOOK_REVIEW_COUNT) {
            log.info("=== Skipping BookReview Generation: {} already exist ===", existingCount);
            return;
        }
        log.info("=== Starting BookReview Generation: {} records (existing: {}) ===", BOOK_REVIEW_COUNT, existingCount);
        long startTime = System.currentTimeMillis();

        // Load member IDs
        List<Integer> memberIdList = em.createQuery(
                "SELECT m.memberId FROM Member m WHERE m.delYn = false", Integer.class)
                .getResultList();

        // Load category IDs
        List<Integer> categoryIdList = em.createQuery(
                "SELECT c.categoryId FROM Category c WHERE c.delYn = false", Integer.class)
                .getResultList();

        if (memberIdList.isEmpty() || categoryIdList.isEmpty()) {
            log.error("No members or categories found. Run generateMembers and generateCategories first.");
            return;
        }

        String[] bookTitles = {"The Great Gatsby", "1984", "To Kill a Mockingbird",
                "Pride and Prejudice", "The Catcher in the Rye", "Lord of the Flies",
                "Animal Farm", "Brave New World", "The Hobbit", "Fahrenheit 451"};
        String[] authors = {"F. Scott Fitzgerald", "George Orwell", "Harper Lee",
                "Jane Austen", "J.D. Salinger", "William Golding", "J.R.R. Tolkien", "Ray Bradbury"};
        String[] publishers = {"Penguin Books", "Random House", "HarperCollins",
                "Simon & Schuster", "Macmillan"};

        for (int i = 1; i <= BOOK_REVIEW_COUNT; i++) {
            Member memberRef = em.getReference(Member.class,
                    memberIdList.get(random.nextInt(memberIdList.size())));
            Integer categoryId = categoryIdList.get(random.nextInt(categoryIdList.size()));

            BookReview bookReview = BookReview.builder()
                    .member(memberRef)
                    .categoryId(categoryId)
                    .title("Book Review #" + i + ": " + bookTitles[i % bookTitles.length])
                    .content("This is a detailed review of the book. " +
                            "The story is compelling and the characters are well-developed. " +
                            "I highly recommend this book to anyone who enjoys " +
                            (i % 2 == 0 ? "classic literature." : "modern fiction."))
                    .bookTitle(bookTitles[i % bookTitles.length])
                    .authors(authors[i % authors.length])
                    .publisher(publishers[i % publishers.length])
                    .isbn("978-" + String.format("%010d", i))
                    .thumbnailUrl("https://example.com/thumbnails/book" + i + ".jpg")
                    .rating(1 + random.nextInt(5))
                    .build();

            em.persist(bookReview);

            if (i % BATCH_SIZE == 0) {
                em.flush();
                em.clear();
            }
            if (i % LOG_INTERVAL == 0) {
                log.info("Progress: {}/{} book reviews", i, BOOK_REVIEW_COUNT);
            }
        }

        em.flush();
        em.clear();

        long duration = System.currentTimeMillis() - startTime;
        log.info("=== BookReview Generation Complete: {} records in {}ms ({} records/sec) ===",
                BOOK_REVIEW_COUNT, duration, BOOK_REVIEW_COUNT * 1000L / duration);
    }

    @Test
    @Order(5)
    @Transactional
    @Commit
    @DisplayName("5. Generate 200,000 Root Replies")
    void generateRootReplies() {
        Long existingRootCount = em.createQuery(
                "SELECT COUNT(r) FROM Reply r WHERE r.parentReplyCode IS NULL", Long.class).getSingleResult();
        if (existingRootCount >= ROOT_REPLY_COUNT) {
            log.info("=== Skipping Root Reply Generation: {} already exist ===", existingRootCount);
            return;
        }
        log.info("=== Starting Root Reply Generation: {} records (existing: {}) ===", ROOT_REPLY_COUNT, existingRootCount);
        long startTime = System.currentTimeMillis();

        // Load member IDs
        List<Integer> memberIdList = em.createQuery(
                "SELECT m.memberId FROM Member m WHERE m.delYn = false", Integer.class)
                .getResultList();

        // Load board codes
        List<String> boardCodeList = em.createQuery(
                "SELECT b.code FROM Board b WHERE b.delYn = false", String.class)
                .setMaxResults(50000)  // Limit to avoid memory issues
                .getResultList();

        // Load book review codes
        List<String> bookReviewCodeList = em.createQuery(
                "SELECT br.code FROM BookReview br WHERE br.delYn = false", String.class)
                .setMaxResults(50000)
                .getResultList();

        if (memberIdList.isEmpty() || (boardCodeList.isEmpty() && bookReviewCodeList.isEmpty())) {
            log.error("No members or posts found. Run previous generators first.");
            return;
        }

        // Combine post codes
        List<String> allPostCodes = new ArrayList<>();
        allPostCodes.addAll(boardCodeList);
        allPostCodes.addAll(bookReviewCodeList);

        log.info("Loaded {} members and {} post codes for reference",
                memberIdList.size(), allPostCodes.size());

        for (int i = 1; i <= ROOT_REPLY_COUNT; i++) {
            Member memberRef = em.getReference(Member.class,
                    memberIdList.get(random.nextInt(memberIdList.size())));
            String postCode = allPostCodes.get(random.nextInt(allPostCodes.size()));

            Reply reply = Reply.builder()
                    .member(memberRef)
                    .postCode(postCode)
                    .parentReplyCode(null)  // Root reply
                    .content("Root reply #" + i + ": This is a comment on the post. " +
                            "Great content! " + (i % 3 == 0 ? "I totally agree." : "Interesting perspective."))
                    .build();

            em.persist(reply);

            if (i % BATCH_SIZE == 0) {
                em.flush();
                em.clear();
            }
            if (i % LOG_INTERVAL == 0) {
                log.info("Progress: {}/{} root replies", i, ROOT_REPLY_COUNT);
            }
        }

        em.flush();
        em.clear();

        long duration = System.currentTimeMillis() - startTime;
        log.info("=== Root Reply Generation Complete: {} records in {}ms ({} records/sec) ===",
                ROOT_REPLY_COUNT, duration, ROOT_REPLY_COUNT * 1000L / duration);
    }

    @Test
    @Order(6)
    @Transactional
    @Commit
    @DisplayName("6. Generate 300,000 Depth-1 Replies")
    void generateDepth1Replies() {
        Long totalReplyCount = em.createQuery("SELECT COUNT(r) FROM Reply r", Long.class).getSingleResult();
        if (totalReplyCount >= ROOT_REPLY_COUNT + DEPTH1_REPLY_COUNT) {
            log.info("=== Skipping Depth-1 Reply Generation: total replies {} already sufficient ===", totalReplyCount);
            return;
        }
        log.info("=== Starting Depth-1 Reply Generation: {} records ===", DEPTH1_REPLY_COUNT);
        long startTime = System.currentTimeMillis();

        // Load member IDs
        List<Integer> memberIdList = em.createQuery(
                "SELECT m.memberId FROM Member m WHERE m.delYn = false", Integer.class)
                .getResultList();

        // Load root reply codes (parent_reply_code IS NULL)
        List<Object[]> rootReplyData = em.createQuery(
                "SELECT r.replyCode, r.postCode FROM Reply r WHERE r.parentReplyCode IS NULL AND r.delYn = false",
                Object[].class)
                .setMaxResults(100000)  // Limit to avoid memory issues
                .getResultList();

        if (memberIdList.isEmpty() || rootReplyData.isEmpty()) {
            log.error("No members or root replies found. Run previous generators first.");
            return;
        }

        log.info("Loaded {} members and {} root replies for reference",
                memberIdList.size(), rootReplyData.size());

        for (int i = 1; i <= DEPTH1_REPLY_COUNT; i++) {
            Member memberRef = em.getReference(Member.class,
                    memberIdList.get(random.nextInt(memberIdList.size())));

            Object[] parentData = rootReplyData.get(random.nextInt(rootReplyData.size()));
            String parentReplyCode = (String) parentData[0];
            String postCode = (String) parentData[1];

            Reply parentRef = em.getReference(Reply.class, parentReplyCode);

            Reply reply = Reply.builder()
                    .member(memberRef)
                    .postCode(postCode)
                    .parentReplyCode(parentRef)  // Depth-1 reply
                    .content("Depth-1 reply #" + i + ": " +
                            (i % 2 == 0 ? "I agree with your point!" : "That's an interesting take."))
                    .build();

            em.persist(reply);

            if (i % BATCH_SIZE == 0) {
                em.flush();
                em.clear();
            }
            if (i % LOG_INTERVAL == 0) {
                log.info("Progress: {}/{} depth-1 replies", i, DEPTH1_REPLY_COUNT);
            }
        }

        em.flush();
        em.clear();

        long duration = System.currentTimeMillis() - startTime;
        log.info("=== Depth-1 Reply Generation Complete: {} records in {}ms ({} records/sec) ===",
                DEPTH1_REPLY_COUNT, duration, DEPTH1_REPLY_COUNT * 1000L / duration);
    }

    @Test
    @Order(7)
    @Transactional
    @Commit
    @DisplayName("7. Generate 200,000 Depth-2 Replies")
    void generateDepth2Replies() {
        Long totalReplyCount = em.createQuery("SELECT COUNT(r) FROM Reply r", Long.class).getSingleResult();
        if (totalReplyCount >= ROOT_REPLY_COUNT + DEPTH1_REPLY_COUNT + DEPTH2_REPLY_COUNT) {
            log.info("=== Skipping Depth-2 Reply Generation: total replies {} already sufficient ===", totalReplyCount);
            return;
        }
        log.info("=== Starting Depth-2 Reply Generation: {} records ===", DEPTH2_REPLY_COUNT);
        long startTime = System.currentTimeMillis();

        // Load member IDs
        List<Integer> memberIdList = em.createQuery(
                "SELECT m.memberId FROM Member m WHERE m.delYn = false", Integer.class)
                .getResultList();

        // Load depth-1 reply codes (parent_reply_code IS NOT NULL, but their parent has NULL parent)
        List<Object[]> depth1ReplyData = em.createQuery(
                "SELECT r.replyCode, r.postCode FROM Reply r WHERE r.parentReplyCode IS NOT NULL AND r.delYn = false",
                Object[].class)
                .setMaxResults(100000)
                .getResultList();

        if (memberIdList.isEmpty() || depth1ReplyData.isEmpty()) {
            log.error("No members or depth-1 replies found. Run previous generators first.");
            return;
        }

        log.info("Loaded {} members and {} depth-1 replies for reference",
                memberIdList.size(), depth1ReplyData.size());

        for (int i = 1; i <= DEPTH2_REPLY_COUNT; i++) {
            Member memberRef = em.getReference(Member.class,
                    memberIdList.get(random.nextInt(memberIdList.size())));

            Object[] parentData = depth1ReplyData.get(random.nextInt(depth1ReplyData.size()));
            String parentReplyCode = (String) parentData[0];
            String postCode = (String) parentData[1];

            Reply parentRef = em.getReference(Reply.class, parentReplyCode);

            Reply reply = Reply.builder()
                    .member(memberRef)
                    .postCode(postCode)
                    .parentReplyCode(parentRef)  // Depth-2 reply
                    .content("Depth-2 reply #" + i + ": " +
                            (i % 3 == 0 ? "Thanks for the clarification!" :
                             i % 3 == 1 ? "Good discussion here." : "Makes sense!"))
                    .build();

            em.persist(reply);

            if (i % BATCH_SIZE == 0) {
                em.flush();
                em.clear();
            }
            if (i % LOG_INTERVAL == 0) {
                log.info("Progress: {}/{} depth-2 replies", i, DEPTH2_REPLY_COUNT);
            }
        }

        em.flush();
        em.clear();

        long duration = System.currentTimeMillis() - startTime;
        log.info("=== Depth-2 Reply Generation Complete: {} records in {}ms ({} records/sec) ===",
                DEPTH2_REPLY_COUNT, duration, DEPTH2_REPLY_COUNT * 1000L / duration);
    }

    @Test
    @Order(8)
    @Transactional
    @Commit
    @DisplayName("8. Generate 100,000 Likes")
    void generateLikes() {
        Long existingCount = ((Number) em.createNativeQuery("SELECT COUNT(*) FROM likes").getSingleResult()).longValue();
        if (existingCount >= LIKES_COUNT) {
            log.info("=== Skipping Likes Generation: {} already exist ===", existingCount);
            return;
        }
        log.info("=== Starting Likes Generation: {} records (existing: {}) ===", LIKES_COUNT, existingCount);
        long startTime = System.currentTimeMillis();

        // Load member IDs
        List<Integer> memberIdList = em.createQuery(
                "SELECT m.memberId FROM Member m WHERE m.delYn = false", Integer.class)
                .getResultList();

        // Load all post codes (boards + book reviews)
        List<String> boardCodeList = em.createQuery(
                "SELECT b.code FROM Board b WHERE b.delYn = false", String.class)
                .setMaxResults(30000)
                .getResultList();

        List<String> bookReviewCodeList = em.createQuery(
                "SELECT br.code FROM BookReview br WHERE br.delYn = false", String.class)
                .setMaxResults(30000)
                .getResultList();

        // Load reply codes
        List<String> replyCodeList = em.createQuery(
                "SELECT r.replyCode FROM Reply r WHERE r.delYn = false", String.class)
                .setMaxResults(40000)
                .getResultList();

        List<String> allCodes = new ArrayList<>();
        allCodes.addAll(boardCodeList);
        allCodes.addAll(bookReviewCodeList);
        allCodes.addAll(replyCodeList);

        if (memberIdList.isEmpty() || allCodes.isEmpty()) {
            log.error("No members or likeable content found. Run previous generators first.");
            return;
        }

        log.info("Loaded {} members and {} likeable codes for reference",
                memberIdList.size(), allCodes.size());

        // Track already created likes to avoid duplicates
        java.util.Set<String> createdLikes = new java.util.HashSet<>();
        int successCount = 0;
        int attemptCount = 0;
        int maxAttempts = LIKES_COUNT * 3; // Allow some retries for duplicate avoidance

        while (successCount < LIKES_COUNT && attemptCount < maxAttempts) {
            attemptCount++;

            Integer memberId = memberIdList.get(random.nextInt(memberIdList.size()));
            String code = allCodes.get(random.nextInt(allCodes.size()));
            String likeKey = memberId + "_" + code;

            if (createdLikes.contains(likeKey)) {
                continue;  // Skip duplicate
            }

            createdLikes.add(likeKey);
            successCount++;

            // Use native query for Likes insertion due to composite key complexity
            em.createNativeQuery(
                    "INSERT INTO likes (code, member_id, reg_time, update_time, version) VALUES (?, ?, NOW(), NOW(), 0)")
                    .setParameter(1, code)
                    .setParameter(2, memberId)
                    .executeUpdate();

            if (successCount % BATCH_SIZE == 0) {
                em.flush();
                em.clear();
            }
            if (successCount % LOG_INTERVAL == 0) {
                log.info("Progress: {}/{} likes", successCount, LIKES_COUNT);
            }
        }

        em.flush();
        em.clear();

        long duration = System.currentTimeMillis() - startTime;
        log.info("=== Likes Generation Complete: {} records in {}ms ({} records/sec) ===",
                successCount, duration, successCount * 1000L / duration);
    }

    @Test
    @Order(9)
    @DisplayName("9. Verify Data Counts")
    void verifyDataCounts() {
        log.info("=== Verifying Data Counts ===");

        Long memberCount = em.createQuery("SELECT COUNT(m) FROM Member m", Long.class).getSingleResult();
        Long categoryCount = em.createQuery("SELECT COUNT(c) FROM Category c", Long.class).getSingleResult();
        Long boardCount = em.createQuery("SELECT COUNT(b) FROM Board b", Long.class).getSingleResult();
        Long bookReviewCount = em.createQuery("SELECT COUNT(br) FROM BookReview br", Long.class).getSingleResult();
        Long replyCount = em.createQuery("SELECT COUNT(r) FROM Reply r", Long.class).getSingleResult();
        Long likesCount = em.createNativeQuery("SELECT COUNT(*) FROM likes").getSingleResult() instanceof Number n ? n.longValue() : 0L;

        log.info("Data Counts:");
        log.info("  Members: {}", memberCount);
        log.info("  Categories: {}", categoryCount);
        log.info("  Boards: {}", boardCount);
        log.info("  BookReviews: {}", bookReviewCount);
        log.info("  Replies: {}", replyCount);
        log.info("  Likes: {}", likesCount);

        Long totalCount = memberCount + categoryCount + boardCount + bookReviewCount + replyCount + likesCount;
        log.info("  Total Records: {}", totalCount);

        log.info("=== Verification Complete ===");
    }
}

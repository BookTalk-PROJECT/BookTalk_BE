#!/usr/bin/env python3
"""
Dummy Data Generator for BookTalk Performance Testing

Generates SQL dump files for MySQL direct import.
Run order:
1. python generate_dummy_data.py
2. mysql -u root -p booktalk < 01_members.sql
3. mysql -u root -p booktalk < 02_categories.sql
4. ... (in numerical order)

Data Distribution (Total ~4.7M records):
- Member: 10,000
- Category: 100 (20 root + 80 children)
- Board: 1,000,000
- BookReview: 100,000
- Reply: 3,000,000 (1M root + 1.5M depth-1 + 500K depth-2)
  -> Concentrated on 30K high-numbered posts (~100 replies/post)
     for infinite scroll testing on board detail pages
- Likes: 500,000
"""

import os
import random
from datetime import datetime, timedelta

# Configuration
OUTPUT_DIR = os.path.dirname(os.path.abspath(__file__))
BATCH_SIZE = 1000
SEED = 42

# Data counts
MEMBER_COUNT = 10000
ROOT_CATEGORY_COUNT = 20
CHILD_CATEGORY_COUNT = 80
BOARD_COUNT = 1000000
BOOK_REVIEW_COUNT = 100000
ROOT_REPLY_COUNT = 1000000
DEPTH1_REPLY_COUNT = 1500000
DEPTH2_REPLY_COUNT = 500000
LIKES_COUNT = 500000

# Reply target posts: concentrate replies on fewer posts for infinite scroll testing
# ~100 replies per post (33 root + 50 depth-1 + 17 depth-2)
# Assigned from highest code numbers (descending sort order = first seen)
REPLY_TARGET_BOARD_COUNT = 25000   # BO_000000975001 ~ BO_000001000000
REPLY_TARGET_BR_COUNT = 5000       # BR_000000095001 ~ BR_000000100000
REPLY_TARGET_POST_COUNT = REPLY_TARGET_BOARD_COUNT + REPLY_TARGET_BR_COUNT  # 30000

random.seed(SEED)

# Global mappings for data consistency
# reply_code -> post_code mapping (built during reply generation)
root_reply_to_post = {}      # root reply_code -> post_code
depth1_reply_to_post = {}    # depth-1 reply_code -> post_code

def escape_sql(s):
    """Escape string for SQL"""
    if s is None:
        return 'NULL'
    return s.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\r", "\\r")

def random_datetime(start_year=2023, end_year=2024):
    """Generate random datetime"""
    start = datetime(start_year, 1, 1)
    end = datetime(end_year, 12, 31)
    delta = end - start
    random_days = random.randint(0, delta.days)
    random_seconds = random.randint(0, 86399)
    return start + timedelta(days=random_days, seconds=random_seconds)

def format_datetime(dt):
    """Format datetime for SQL"""
    return dt.strftime('%Y-%m-%d %H:%M:%S')

def write_sql_header(f, table_name, truncate_table=None):
    """Write SQL file header with performance optimizations

    Args:
        f: file handle
        table_name: display name for comments
        truncate_table: actual table name to truncate (if None, no truncate)
    """
    f.write(f"-- Dummy data for {table_name}\n")
    f.write(f"-- Generated: {datetime.now().isoformat()}\n\n")
    f.write("SET FOREIGN_KEY_CHECKS = 0;\n")
    f.write("SET UNIQUE_CHECKS = 0;\n")
    f.write("SET AUTOCOMMIT = 0;\n")
    f.write("SET SQL_LOG_BIN = 0;\n\n")

    if truncate_table:
        f.write(f"-- Clear existing data\n")
        f.write(f"TRUNCATE TABLE {truncate_table};\n\n")

def write_sql_footer(f):
    """Write SQL file footer"""
    f.write("\nCOMMIT;\n")
    f.write("SET FOREIGN_KEY_CHECKS = 1;\n")
    f.write("SET UNIQUE_CHECKS = 1;\n")
    f.write("SET AUTOCOMMIT = 1;\n")
    f.write("SET SQL_LOG_BIN = 1;\n")

def generate_members():
    """Generate member data"""
    print(f"Generating {MEMBER_COUNT} members...")
    filepath = os.path.join(OUTPUT_DIR, "01_members.sql")

    with open(filepath, 'w', encoding='utf-8') as f:
        write_sql_header(f, "member", truncate_table="member")

        f.write("INSERT INTO member (member_id, email, name, auth_type, password, phone_number, address, gender, authority, del_yn, reg_time, update_time, version) VALUES\n")

        for i in range(1, MEMBER_COUNT + 1):
            member_id = i  # Explicitly set member_id
            email = f"testuser{i}@booktalk.com"
            name = f"TestUser{i}"
            auth_type = "OWN"
            password = f"$2a$10$dummyhashedpassword{i}"
            phone_number = f"010-{i // 100:04d}-{i % 10000:04d}"
            address = f"Test Address {i}"
            gender = "M" if i % 2 == 0 else "F"
            authority = "COMMON"
            del_yn = 0
            dt = random_datetime()
            reg_time = format_datetime(dt)
            update_time = reg_time
            version = 0

            comma = "," if i < MEMBER_COUNT else ";"
            f.write(f"({member_id}, '{escape_sql(email)}', '{escape_sql(name)}', '{auth_type}', '{escape_sql(password)}', "
                   f"'{escape_sql(phone_number)}', '{escape_sql(address)}', '{gender}', '{authority}', "
                   f"{del_yn}, '{reg_time}', '{update_time}', {version}){comma}\n")

            if i % 10000 == 0:
                print(f"  Members: {i}/{MEMBER_COUNT}")

        write_sql_footer(f)

    print(f"  Created: {filepath}")

def generate_categories():
    """Generate category data"""
    total = ROOT_CATEGORY_COUNT + CHILD_CATEGORY_COUNT
    print(f"Generating {total} categories...")
    filepath = os.path.join(OUTPUT_DIR, "02_categories.sql")

    with open(filepath, 'w', encoding='utf-8') as f:
        write_sql_header(f, "category", truncate_table="category")

        f.write("INSERT INTO category (category_id, value, is_active, p_category_id, del_yn, display_order, reg_time, update_time, version) VALUES\n")

        idx = 0
        # Root categories (category_id: 1 ~ 20)
        for i in range(1, ROOT_CATEGORY_COUNT + 1):
            idx += 1
            category_id = i
            value = f"RootCategory{i}"
            is_active = 1
            p_category_id = "NULL"
            del_yn = 0
            display_order = 0
            dt = random_datetime()
            reg_time = format_datetime(dt)
            update_time = reg_time
            version = 0

            comma = "," if idx < total else ";"
            f.write(f"({category_id}, '{escape_sql(value)}', {is_active}, {p_category_id}, {del_yn}, {display_order}, "
                   f"'{reg_time}', '{update_time}', {version}){comma}\n")

        # Child categories (category_id: 21 ~ 100)
        for i in range(1, CHILD_CATEGORY_COUNT + 1):
            idx += 1
            category_id = ROOT_CATEGORY_COUNT + i  # 21, 22, ..., 100
            value = f"ChildCategory{i}"
            is_active = 1
            parent_id = ((i - 1) % ROOT_CATEGORY_COUNT) + 1  # 1 ~ 20
            del_yn = 0
            display_order = i
            dt = random_datetime()
            reg_time = format_datetime(dt)
            update_time = reg_time
            version = 0

            comma = "," if idx < total else ";"
            f.write(f"({category_id}, '{escape_sql(value)}', {is_active}, {parent_id}, {del_yn}, {display_order}, "
                   f"'{reg_time}', '{update_time}', {version}){comma}\n")

        write_sql_footer(f)

    print(f"  Created: {filepath}")

def generate_boards():
    """Generate board data"""
    print(f"Generating {BOARD_COUNT} boards...")
    filepath = os.path.join(OUTPUT_DIR, "03_boards.sql")

    total_categories = ROOT_CATEGORY_COUNT + CHILD_CATEGORY_COUNT  # 100

    with open(filepath, 'w', encoding='utf-8') as f:
        write_sql_header(f, "board", truncate_table="board")

        batch_count = 0
        for start in range(1, BOARD_COUNT + 1, BATCH_SIZE):
            end = min(start + BATCH_SIZE - 1, BOARD_COUNT)

            if batch_count > 0:
                f.write("\n")

            f.write("INSERT INTO board (code, member_id, category_id, title, content, views, like_cnt, del_yn, notification_yn, created_by, modified_by, reg_time, update_time, version) VALUES\n")

            for i in range(start, end + 1):
                code = f"BO_{i:012d}"
                # member_id: 1 ~ MEMBER_COUNT (guaranteed to exist)
                member_id = ((i - 1) % MEMBER_COUNT) + 1
                # category_id: 1 ~ 100 (guaranteed to exist)
                category_id = ((i - 1) % total_categories) + 1
                title = f"Performance Test Board Title {i} - Lorem ipsum dolor sit amet"
                content = (f"Performance test content for board {i}. "
                          "This is a longer content to simulate real-world scenarios. "
                          "Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
                          "Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.")
                views = random.randint(0, 1000)
                like_cnt = 0
                del_yn = 0
                notification_yn = 1 if i % 100 == 0 else 0
                created_by = "SYSTEM_TEST"
                modified_by = "SYSTEM_TEST"
                dt = random_datetime()
                reg_time = format_datetime(dt)
                update_time = reg_time
                version = 0

                comma = "," if i < end else ";"
                f.write(f"('{code}', {member_id}, {category_id}, '{escape_sql(title)}', '{escape_sql(content)}', "
                       f"{views}, {like_cnt}, {del_yn}, {notification_yn}, '{created_by}', '{modified_by}', "
                       f"'{reg_time}', '{update_time}', {version}){comma}\n")

            batch_count += 1

            if end % 100000 == 0:
                print(f"  Boards: {end}/{BOARD_COUNT}")

        write_sql_footer(f)

    print(f"  Created: {filepath}")

def generate_book_reviews():
    """Generate book review data"""
    print(f"Generating {BOOK_REVIEW_COUNT} book reviews...")
    filepath = os.path.join(OUTPUT_DIR, "04_book_reviews.sql")

    total_categories = ROOT_CATEGORY_COUNT + CHILD_CATEGORY_COUNT  # 100

    book_titles = ["The Great Gatsby", "1984", "To Kill a Mockingbird",
                   "Pride and Prejudice", "The Catcher in the Rye", "Lord of the Flies",
                   "Animal Farm", "Brave New World", "The Hobbit", "Fahrenheit 451"]
    authors = ["F. Scott Fitzgerald", "George Orwell", "Harper Lee",
               "Jane Austen", "J.D. Salinger", "William Golding", "J.R.R. Tolkien", "Ray Bradbury"]
    publishers = ["Penguin Books", "Random House", "HarperCollins",
                  "Simon & Schuster", "Macmillan"]

    with open(filepath, 'w', encoding='utf-8') as f:
        write_sql_header(f, "book_review", truncate_table="book_review")

        batch_count = 0
        for start in range(1, BOOK_REVIEW_COUNT + 1, BATCH_SIZE):
            end = min(start + BATCH_SIZE - 1, BOOK_REVIEW_COUNT)

            if batch_count > 0:
                f.write("\n")

            f.write("INSERT INTO book_review (code, member_id, category_id, title, content, book_title, authors, publisher, isbn, thumbnail_url, rating, views, like_cnt, del_yn, notification_yn, created_by, modified_by, reg_time, update_time, version) VALUES\n")

            for i in range(start, end + 1):
                code = f"BR_{i:012d}"
                # member_id: 1 ~ MEMBER_COUNT (guaranteed to exist)
                member_id = ((i - 1) % MEMBER_COUNT) + 1
                # category_id: 1 ~ 100 (guaranteed to exist)
                category_id = ((i - 1) % total_categories) + 1
                book_title = book_titles[i % len(book_titles)]
                title = f"Book Review #{i}: {book_title}"
                content = ("This is a detailed review of the book. "
                          "The story is compelling and the characters are well-developed. "
                          f"I highly recommend this book to anyone who enjoys {'classic literature.' if i % 2 == 0 else 'modern fiction.'}")
                author = authors[i % len(authors)]
                publisher = publishers[i % len(publishers)]
                isbn = f"978-{i:010d}"
                thumbnail_url = f"https://example.com/thumbnails/book{i}.jpg"
                rating = (i % 5) + 1  # 1~5
                views = random.randint(0, 500)
                like_cnt = 0
                del_yn = 0
                notification_yn = 0
                created_by = "SYSTEM_TEST"
                modified_by = "SYSTEM_TEST"
                dt = random_datetime()
                reg_time = format_datetime(dt)
                update_time = reg_time
                version = 0

                comma = "," if i < end else ";"
                f.write(f"('{code}', {member_id}, {category_id}, '{escape_sql(title)}', '{escape_sql(content)}', "
                       f"'{escape_sql(book_title)}', '{escape_sql(author)}', '{escape_sql(publisher)}', '{isbn}', "
                       f"'{thumbnail_url}', {rating}, {views}, {like_cnt}, {del_yn}, {notification_yn}, "
                       f"'{created_by}', '{modified_by}', '{reg_time}', '{update_time}', {version}){comma}\n")

            batch_count += 1

            if end % 50000 == 0:
                print(f"  Book Reviews: {end}/{BOOK_REVIEW_COUNT}")

        write_sql_footer(f)

    print(f"  Created: {filepath}")

def generate_root_replies():
    """Generate root reply data (no parent)"""
    global root_reply_to_post

    print(f"Generating {ROOT_REPLY_COUNT} root replies...")
    filepath = os.path.join(OUTPUT_DIR, "05_root_replies.sql")

    # Concentrate replies on high-numbered posts for infinite scroll testing
    # Boards: BO_000000975001 ~ BO_000001000000 (top 25,000)
    # BookReviews: BR_000000095001 ~ BR_000000100000 (top 5,000)
    board_start = BOARD_COUNT - REPLY_TARGET_BOARD_COUNT + 1       # 975001
    br_start = BOOK_REVIEW_COUNT - REPLY_TARGET_BR_COUNT + 1       # 95001

    def get_post_code(idx):
        """Get post code by index (0-based), cycling through target posts (high codes)"""
        post_idx = idx % REPLY_TARGET_POST_COUNT
        if post_idx < REPLY_TARGET_BOARD_COUNT:
            board_num = board_start + post_idx  # 975001 ~ 1000000
            return f"BO_{board_num:012d}"
        else:
            br_idx = post_idx - REPLY_TARGET_BOARD_COUNT
            br_num = br_start + br_idx  # 95001 ~ 100000
            return f"BR_{br_num:012d}"

    with open(filepath, 'w', encoding='utf-8') as f:
        write_sql_header(f, "reply (root)", truncate_table="reply")

        batch_count = 0
        for start in range(1, ROOT_REPLY_COUNT + 1, BATCH_SIZE):
            end = min(start + BATCH_SIZE - 1, ROOT_REPLY_COUNT)

            if batch_count > 0:
                f.write("\n")

            f.write("INSERT INTO reply (reply_code, member_id, post_code, parent_reply_code, content, like_cnt, del_yn, created_by, modified_by, reg_time, update_time, version) VALUES\n")

            for i in range(start, end + 1):
                reply_code = f"REP_{i:012d}"
                # member_id: 1 ~ MEMBER_COUNT (guaranteed to exist)
                member_id = ((i - 1) % MEMBER_COUNT) + 1
                # post_code: cycle through all valid posts
                post_code = get_post_code(i - 1)
                parent_reply_code = "NULL"
                content = f"Root reply #{i}: This is a comment on the post. Great content! {'I totally agree.' if i % 3 == 0 else 'Interesting perspective.'}"
                like_cnt = 0
                del_yn = 0
                created_by = "SYSTEM_TEST"
                modified_by = "SYSTEM_TEST"
                dt = random_datetime()
                reg_time = format_datetime(dt)
                update_time = reg_time
                version = 0

                # Store mapping for depth-1 replies
                root_reply_to_post[reply_code] = post_code

                comma = "," if i < end else ";"
                f.write(f"('{reply_code}', {member_id}, '{post_code}', {parent_reply_code}, '{escape_sql(content)}', "
                       f"{like_cnt}, {del_yn}, '{created_by}', '{modified_by}', '{reg_time}', '{update_time}', {version}){comma}\n")

            batch_count += 1

            if end % 100000 == 0:
                print(f"  Root Replies: {end}/{ROOT_REPLY_COUNT}")

        write_sql_footer(f)

    print(f"  Created: {filepath}")
    print(f"  Stored {len(root_reply_to_post)} root reply mappings")

def generate_depth1_replies():
    """Generate depth-1 replies (replies to root replies)"""
    global depth1_reply_to_post

    print(f"Generating {DEPTH1_REPLY_COUNT} depth-1 replies...")
    filepath = os.path.join(OUTPUT_DIR, "06_depth1_replies.sql")

    # Get list of root reply codes
    root_reply_codes = list(root_reply_to_post.keys())
    num_root_replies = len(root_reply_codes)

    if num_root_replies == 0:
        print("  ERROR: No root replies found. Run generate_root_replies first.")
        return

    with open(filepath, 'w', encoding='utf-8') as f:
        write_sql_header(f, "reply (depth-1)")  # No truncate - already done in root replies

        batch_count = 0
        start_idx = ROOT_REPLY_COUNT + 1  # REP_000001000001 ~

        for start in range(1, DEPTH1_REPLY_COUNT + 1, BATCH_SIZE):
            end = min(start + BATCH_SIZE - 1, DEPTH1_REPLY_COUNT)

            if batch_count > 0:
                f.write("\n")

            f.write("INSERT INTO reply (reply_code, member_id, post_code, parent_reply_code, content, like_cnt, del_yn, created_by, modified_by, reg_time, update_time, version) VALUES\n")

            for i in range(start, end + 1):
                reply_idx = start_idx + i - 1
                reply_code = f"REP_{reply_idx:012d}"
                # member_id: 1 ~ MEMBER_COUNT (guaranteed to exist)
                member_id = ((i - 1) % MEMBER_COUNT) + 1

                # Select parent from existing root replies (cycling through)
                parent_idx = (i - 1) % num_root_replies
                parent_reply_code = root_reply_codes[parent_idx]
                # Use the same post_code as parent (data consistency!)
                post_code = root_reply_to_post[parent_reply_code]

                content = f"Depth-1 reply #{i}: {'I agree with your point!' if i % 2 == 0 else 'That is an interesting take.'}"
                like_cnt = 0
                del_yn = 0
                created_by = "SYSTEM_TEST"
                modified_by = "SYSTEM_TEST"
                dt = random_datetime()
                reg_time = format_datetime(dt)
                update_time = reg_time
                version = 0

                # Store mapping for depth-2 replies
                depth1_reply_to_post[reply_code] = post_code

                comma = "," if i < end else ";"
                f.write(f"('{reply_code}', {member_id}, '{post_code}', '{parent_reply_code}', '{escape_sql(content)}', "
                       f"{like_cnt}, {del_yn}, '{created_by}', '{modified_by}', '{reg_time}', '{update_time}', {version}){comma}\n")

            batch_count += 1

            if end % 100000 == 0:
                print(f"  Depth-1 Replies: {end}/{DEPTH1_REPLY_COUNT}")

        write_sql_footer(f)

    print(f"  Created: {filepath}")
    print(f"  Stored {len(depth1_reply_to_post)} depth-1 reply mappings")

def generate_depth2_replies():
    """Generate depth-2 replies (replies to depth-1 replies)"""
    print(f"Generating {DEPTH2_REPLY_COUNT} depth-2 replies...")
    filepath = os.path.join(OUTPUT_DIR, "07_depth2_replies.sql")

    # Get list of depth-1 reply codes
    depth1_reply_codes = list(depth1_reply_to_post.keys())
    num_depth1_replies = len(depth1_reply_codes)

    if num_depth1_replies == 0:
        print("  ERROR: No depth-1 replies found. Run generate_depth1_replies first.")
        return

    with open(filepath, 'w', encoding='utf-8') as f:
        write_sql_header(f, "reply (depth-2)")  # No truncate - already done in root replies

        batch_count = 0
        start_idx = ROOT_REPLY_COUNT + DEPTH1_REPLY_COUNT + 1  # REP_000002500001 ~

        for start in range(1, DEPTH2_REPLY_COUNT + 1, BATCH_SIZE):
            end = min(start + BATCH_SIZE - 1, DEPTH2_REPLY_COUNT)

            if batch_count > 0:
                f.write("\n")

            f.write("INSERT INTO reply (reply_code, member_id, post_code, parent_reply_code, content, like_cnt, del_yn, created_by, modified_by, reg_time, update_time, version) VALUES\n")

            for i in range(start, end + 1):
                reply_idx = start_idx + i - 1
                reply_code = f"REP_{reply_idx:012d}"
                # member_id: 1 ~ MEMBER_COUNT (guaranteed to exist)
                member_id = ((i - 1) % MEMBER_COUNT) + 1

                # Select parent from existing depth-1 replies (cycling through)
                parent_idx = (i - 1) % num_depth1_replies
                parent_reply_code = depth1_reply_codes[parent_idx]
                # Use the same post_code as parent (data consistency!)
                post_code = depth1_reply_to_post[parent_reply_code]

                if i % 3 == 0:
                    content = f"Depth-2 reply #{i}: Thanks for the clarification!"
                elif i % 3 == 1:
                    content = f"Depth-2 reply #{i}: Good discussion here."
                else:
                    content = f"Depth-2 reply #{i}: Makes sense!"

                like_cnt = 0
                del_yn = 0
                created_by = "SYSTEM_TEST"
                modified_by = "SYSTEM_TEST"
                dt = random_datetime()
                reg_time = format_datetime(dt)
                update_time = reg_time
                version = 0

                comma = "," if i < end else ";"
                f.write(f"('{reply_code}', {member_id}, '{post_code}', '{parent_reply_code}', '{escape_sql(content)}', "
                       f"{like_cnt}, {del_yn}, '{created_by}', '{modified_by}', '{reg_time}', '{update_time}', {version}){comma}\n")

            batch_count += 1

            if end % 100000 == 0:
                print(f"  Depth-2 Replies: {end}/{DEPTH2_REPLY_COUNT}")

        write_sql_footer(f)

    print(f"  Created: {filepath}")

def generate_likes():
    """Generate likes data"""
    print(f"Generating {LIKES_COUNT} likes...")
    filepath = os.path.join(OUTPUT_DIR, "08_likes.sql")

    # Build valid code lists
    # Use deterministic approach to avoid duplicates
    all_codes = []

    # Add board codes (first 100K)
    for i in range(1, min(100001, BOARD_COUNT + 1)):
        all_codes.append(f"BO_{i:012d}")

    # Add book review codes (first 50K)
    for i in range(1, min(50001, BOOK_REVIEW_COUNT + 1)):
        all_codes.append(f"BR_{i:012d}")

    # Add reply codes (first 100K)
    total_replies = ROOT_REPLY_COUNT + DEPTH1_REPLY_COUNT + DEPTH2_REPLY_COUNT
    for i in range(1, min(100001, total_replies + 1)):
        all_codes.append(f"REP_{i:012d}")

    print(f"  Available codes for likes: {len(all_codes)}")

    # Generate unique (code, member_id) pairs deterministically
    likes_data = []
    seen = set()

    # Distribute likes across codes and members
    code_idx = 0
    member_offset = 0

    while len(likes_data) < LIKES_COUNT and code_idx < len(all_codes) * 10:
        code = all_codes[code_idx % len(all_codes)]
        # Vary member_id based on code_idx to spread likes
        member_id = ((code_idx + member_offset) % MEMBER_COUNT) + 1

        key = f"{code}_{member_id}"
        if key not in seen:
            seen.add(key)
            dt = random_datetime()
            likes_data.append((code, member_id, format_datetime(dt)))

        code_idx += 1
        if code_idx % len(all_codes) == 0:
            member_offset += 1  # Shift member pattern after each full cycle

    print(f"  Generated {len(likes_data)} unique likes")

    with open(filepath, 'w', encoding='utf-8') as f:
        write_sql_header(f, "likes", truncate_table="likes")

        batch_count = 0
        total = len(likes_data)

        for start in range(0, total, BATCH_SIZE):
            end = min(start + BATCH_SIZE, total)

            if batch_count > 0:
                f.write("\n")

            f.write("INSERT INTO likes (code, member_id, reg_time, update_time, version) VALUES\n")

            for i in range(start, end):
                code, member_id, reg_time = likes_data[i]
                version = 0

                comma = "," if i < end - 1 else ";"
                f.write(f"('{code}', {member_id}, '{reg_time}', '{reg_time}', {version}){comma}\n")

            batch_count += 1

            if end % 100000 == 0:
                print(f"  Likes: {end}/{total}")

        write_sql_footer(f)

    print(f"  Created: {filepath}")

def generate_import_script():
    """Generate shell script for importing all SQL files"""
    filepath = os.path.join(OUTPUT_DIR, "import_all.sh")

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write("#!/bin/bash\n")
        f.write("# Import all dummy data SQL files into MySQL\n")
        f.write("# Usage: ./import_all.sh [database_name] [mysql_user] [mysql_password]\n\n")
        f.write('DB_NAME="${1:-booktalk}"\n')
        f.write('MYSQL_USER="${2:-root}"\n')
        f.write('MYSQL_PWD="${3:-1234}"\n')
        f.write('SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"\n\n')
        f.write('echo "Importing dummy data into database: $DB_NAME"\n')
        f.write('echo "MySQL user: $MYSQL_USER"\n')
        f.write('echo ""\n\n')
        f.write('for sql_file in "$SCRIPT_DIR"/0*.sql; do\n')
        f.write('    if [ -f "$sql_file" ]; then\n')
        f.write('        filename=$(basename "$sql_file")\n')
        f.write('        echo "Importing $filename..."\n')
        f.write('        mysql -u "$MYSQL_USER" -p"$MYSQL_PWD" "$DB_NAME" < "$sql_file"\n')
        f.write('        if [ $? -eq 0 ]; then\n')
        f.write('            echo "  Done!"\n')
        f.write('        else\n')
        f.write('            echo "  Failed!"\n')
        f.write('            exit 1\n')
        f.write('        fi\n')
        f.write('    fi\n')
        f.write('done\n\n')
        f.write('echo ""\n')
        f.write('echo "All imports completed successfully!"\n')

    print(f"  Created: {filepath}")

    # Windows batch file version
    bat_filepath = os.path.join(OUTPUT_DIR, "import_all.bat")
    with open(bat_filepath, 'w', encoding='utf-8') as f:
        f.write("@echo off\n")
        f.write("REM Import all dummy data SQL files into MySQL\n")
        f.write("REM Usage: import_all.bat [database_name] [mysql_user] [mysql_password]\n\n")
        f.write("SET DB_NAME=%1\n")
        f.write("SET MYSQL_USER=%2\n")
        f.write("SET MYSQL_PWD=%3\n")
        f.write("IF \"%DB_NAME%\"==\"\" SET DB_NAME=booktalk\n")
        f.write("IF \"%MYSQL_USER%\"==\"\" SET MYSQL_USER=root\n")
        f.write("IF \"%MYSQL_PWD%\"==\"\" SET MYSQL_PWD=1234\n\n")
        f.write("echo Importing dummy data into database: %DB_NAME%\n")
        f.write("echo MySQL user: %MYSQL_USER%\n")
        f.write("echo.\n\n")
        f.write("FOR %%f IN (%~dp00*.sql) DO (\n")
        f.write("    echo Importing %%~nxf...\n")
        f.write("    mysql -u %MYSQL_USER% -p%MYSQL_PWD% %DB_NAME% < \"%%f\"\n")
        f.write("    IF ERRORLEVEL 1 (\n")
        f.write("        echo   Failed!\n")
        f.write("        exit /b 1\n")
        f.write("    )\n")
        f.write("    echo   Done!\n")
        f.write(")\n\n")
        f.write("echo.\n")
        f.write("echo All imports completed successfully!\n")

    print(f"  Created: {bat_filepath}")

def main():
    print("=" * 60)
    print("BookTalk Dummy Data Generator")
    print("=" * 60)
    print(f"Output directory: {OUTPUT_DIR}")
    print(f"Total records to generate: ~{(MEMBER_COUNT + ROOT_CATEGORY_COUNT + CHILD_CATEGORY_COUNT + BOARD_COUNT + BOOK_REVIEW_COUNT + ROOT_REPLY_COUNT + DEPTH1_REPLY_COUNT + DEPTH2_REPLY_COUNT + LIKES_COUNT):,}")
    print("=" * 60)
    print()
    print("Data Consistency Guarantees:")
    print("  - member_id: 1 ~ 10,000 (cycling)")
    print("  - category_id: 1 ~ 100 (cycling)")
    print("  - board.code: BO_000000000001 ~ BO_000001000000")
    print("  - book_review.code: BR_000000000001 ~ BR_000000100000")
    print("  - reply.post_code: concentrated on top 30K high-numbered posts (~100 replies/post)")
    print("  - reply.parent_reply_code: references existing reply with same post_code")
    print("  - likes.code: references existing board/book_review/reply codes")
    print("=" * 60)
    print()

    generate_members()
    generate_categories()
    generate_boards()
    generate_book_reviews()
    generate_root_replies()
    generate_depth1_replies()
    generate_depth2_replies()
    generate_likes()
    generate_import_script()

    print()
    print("=" * 60)
    print("Generation complete!")
    print("=" * 60)
    print()
    print("To import into MySQL:")
    print("  Windows: import_all.bat [database_name] [mysql_user]")
    print("  Linux/Mac: ./import_all.sh [database_name] [mysql_user]")
    print()
    print("Or import individual files:")
    print("  mysql -u root -p booktalk < 01_members.sql")
    print("  mysql -u root -p booktalk < 02_categories.sql")
    print("  ... (in numerical order)")
    print()

if __name__ == "__main__":
    main()

-- Điểm hài lòng tổng thể của một user với một môn học, dùng làm ma trận user-item R của TriRank.
-- Trước đây R được lấy bằng trung bình 7 điểm thuộc tính lưỡng cực, nhưng các trục đó là mô tả
-- (Lý thuyết ↔ Thực hành) chứ không phải đánh giá tốt/xấu, nên trung bình của chúng bị ghim quanh 3
-- (đo được SD 0.49, 67% giá trị nằm trong [2.5, 3.5]) và hầu như không mang thông tin nào.
CREATE TABLE user_course_satisfaction
(
    id         bigint auto_increment
        primary key,
    user_id    varchar(255) not null,
    course_id  bigint       not null,
    score      double       not null,
    created_at timestamp default CURRENT_TIMESTAMP null,
    constraint uk_user_course_satisfaction
        unique (user_id, course_id)
);

-- Mỗi (algorithm, tenant, user) chỉ có một bản ghi sở thích. Code đã ngầm giả định điều này
-- (findByAlgorithmAndTenantIdAndUserId trả về Optional), nay khai báo tường minh để có thể upsert.
DELETE p1
FROM user_preference p1
         JOIN user_preference p2
              ON p1.algorithm <=> p2.algorithm
                  AND p1.tenant_id <=> p2.tenant_id
                  AND p1.user_id <=> p2.user_id
                  AND p1.id < p2.id;

ALTER TABLE user_preference
    ADD CONSTRAINT uk_user_preference_algorithm_tenant_user
        UNIQUE (algorithm, tenant_id, user_id);

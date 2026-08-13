-- Điểm hài lòng là số sao nguyên 1-5, vì đó là tất cả những gì form đánh giá cho phép nhập.
-- Cột được tạo là double ở V6 vì lúc đó generator sinh giá trị liên tục, với lập luận rằng R càng
-- mịn càng nhiều thông tin. Lập luận đó ngược: dữ liệu sinh ra khi đó không giống dữ liệu mà ứng
-- dụng thực sự thu được, và không có dòng thật nào trông như vậy. Đổi sang int để không thể tái diễn.
--
-- LƯU Ý: câu ALTER dưới đây làm tròn tại chỗ mọi giá trị thập phân đang có (MySQL làm tròn khi
-- chuyển double -> int), nên hãy backup trước nếu muốn giữ lại giá trị cũ.
ALTER TABLE user_course_satisfaction
    MODIFY score int NOT NULL;

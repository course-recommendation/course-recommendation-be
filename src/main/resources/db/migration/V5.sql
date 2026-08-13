-- Thêm nhãn hai cực cho tiêu chí đánh giá: low_label ứng với điểm 1, high_label ứng với điểm 5
ALTER TABLE attribute ADD COLUMN low_label VARCHAR(255) NULL;
ALTER TABLE attribute ADD COLUMN high_label VARCHAR(255) NULL;

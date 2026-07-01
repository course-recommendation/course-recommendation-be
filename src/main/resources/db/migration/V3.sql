ALTER TABLE user_course_rating
    ADD UNIQUE INDEX uk_user_course_attribute (user_id, course_id, attribute_id);

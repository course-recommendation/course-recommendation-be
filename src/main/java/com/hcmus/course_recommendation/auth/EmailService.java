package com.hcmus.course_recommendation.auth;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {
	private final JavaMailSender mailSender;

	public void sendPasswordResetEmail(String to, String resetLink) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(to);
		message.setSubject("Đặt lại mật khẩu CourseHub");
		message.setText("Nhấn vào liên kết sau để đặt lại mật khẩu của bạn:\n\n" + resetLink + "\n\nLink sẽ hết hạn sau 1 giờ.");
		mailSender.send(message);
	}
}

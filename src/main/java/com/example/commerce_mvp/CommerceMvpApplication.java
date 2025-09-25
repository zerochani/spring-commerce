package com.example.commerce_mvp;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication
public class CommerceMvpApplication {

	public static void main(String[] args) {
		// .env 파일을 로드합니다.
		Dotenv dotenv = Dotenv.configure().load();
		//.env의 모든 변수를 자바 시스템 속성으로 설정
		dotenv.entries().forEach(entry -> {
			System.setProperty(entry.getKey(), entry.getValue());
		});

		SpringApplication.run(CommerceMvpApplication.class, args);
	}

}

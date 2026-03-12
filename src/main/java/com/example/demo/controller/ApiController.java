package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.BMI;
import com.example.demo.response.ApiResponse;

//@RestController 可以省去撰寫@RequestBody
@RestController
@RequestMapping("/api")
public class ApiController {
	/*
	 *1.API首頁
	 *路徑: /home
	 *路徑: /welcome
	 *網址: http://localhost:8080/api/home
	 *網址: http://localhost:8080/api/welcome
	 * */
	//produces = "text/plain;charset=utf-8" <<避免網頁出亂碼
	@GetMapping(value = {"/home","/welcome"},produces = "text/plain;charset=utf-8")

	public String home() {
		return "我是API 首頁";
	}
	
	/**
	 * 2. ?帶參數
	 * 路徑1: /greet?name=John&age=18
	 * 網址1: http://localhost:8080/api/greet?name=John&age=18
	 * 結果1: Hi John, 18(成年)
	 * 
	 * 路徑2: /greet?name=Mary
	 * 網址2: http://localhost:8080/api/greet?name=Mary
	 * 結果2: Hi Mary, 0(未成年)
	 * 
	 * 限制: name 為必要參數, age 為可選參數(有初始值 0)
	 * */
	//@GetMapping(value = {"/greet"})
	//@GetMapping(value = "/greet")
	@GetMapping("/greet")
	public String greet(@RequestParam(value = "name", required = true) String username,
						@RequestParam(value = "age", required = false, defaultValue = "0") Integer userage) {
		
		String result = String.format("Hi %s, %d(%s)", username, userage, userage>=18?"成年":"未成年");
		return result;
	}
	
	// 3. 上述 2 的精簡寫法
	// 方法參數名稱與請求參數名相同
	@GetMapping("/greet2")
	public String greet2(@RequestParam String name,
						@RequestParam(required = false, defaultValue = "0") Integer age) {
		
		return greet(name, age);
	}
	
	/** 
	 * 4. Lab 練習 I
	 * 路徑: /bmi?h=170&w=60
	 * 網址: http://localhost:8080/api/bmi?h=170&w=60
	 * 判斷: bmi <= 18 顯示過輕, bmi > 23 顯示過重
	 * 執行結果: 身高:170cm 體重:60kg bmi=20.76(正常)
	*/
	@GetMapping("/bmi")
	public String bmi(@RequestParam Double h, @RequestParam Double w) {
		double bmi = w / Math.pow(h/100, 2);
		String result = bmi <= 18 ? "過輕" : bmi > 23 ? "過重" : "正常";
		return String.format("身高:%.0fcm 體重:%.0fkg bmi=%.2f(%s)", h, w, bmi, result);
	}
	
	/** 
	 * 4. 回傳Json結構
	 * 路徑: /json/bmi?h=170&w=60
	 * 網址: http://localhost:8080/api/json/bmi?h=170&w=60
	 * 判斷: bmi <= 18 顯示過輕, bmi > 23 顯示過重
	 * 執行結果: 身高:170cm 體重:60kg bmi=20.76(正常)
	*/
	@GetMapping(value = "/json/bmi")
	public ResponseEntity<ApiResponse<BMI>> calcBmi(@RequestParam(required = false) Double h, 
													@RequestParam(required = false) Double w) {
		if(h==null||w==null) {
			//badRequest => HTTP 400
			return ResponseEntity.badRequest().body(ApiResponse.error("請輸入身高與體重參數內容"));
		}
		if(h<=0||w<=0) {
			//badRequest => HTTP 400
			//return ResponseEntity.badRequest().body(new ApiResponse<>("身高體重參數錯誤",null));
			return ResponseEntity.badRequest().body(ApiResponse.error("身高或體重參數內容錯誤"));
		}
		double bmiValue = w / Math.pow(h/100, 2);
		BMI bmi = new BMI(h,w,bmiValue);
		/*ApiResponse<BMI> apiResponse = new ApiResponse<BMI>("計算成功", bmi);
		 * return ResponseEntity.ok(apiResponse);
		*/
		//ok. = > HTTP 200
		return ResponseEntity.ok(ApiResponse.success("計算成功", bmi));
	}
}

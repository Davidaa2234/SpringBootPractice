package com.example.demo.controller;

import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.BMI;
import com.example.demo.model.Book;
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
	
	/**
	 * 6. 同名多筆資料
	 * 路徑: /json/age?age=17&age=21&age=20
	 * 網址: http://localhost:8080/api/json/age?age=17&age=21&age=20
	 * 請計算出平均年齡
	 * */
	@GetMapping(value = "/json/age", produces = "application/json;charset=utf-8")
	public ResponseEntity<ApiResponse<Object>> getAverage(@RequestParam(name = "age", required = false) List<Integer> ages) {
		if(ages == null || ages.size() == 0) {
			return ResponseEntity.badRequest().body(ApiResponse.error("請輸入年齡(age)"));
		}
		
		double avg = ages.stream().mapToInt(Integer::valueOf).average().orElseGet(() -> 0);
		Object data = Map.of("年齡", ages, "平均年齡", String.format("%.1f", avg));
		return ResponseEntity.ok(ApiResponse.success("計算成功", data));
	}
	
	/*
	 * 7. Lab 練習: 得到多筆 score 資料
	 * 路徑: "/json/score?score=80&score=100&score=50&score=70&score=30"
	 * 網址: http://localhost:8080/api/json/score?score=80&score=100&score=50&score=70&score=30
	 * 請自行設計一個方法，此方法可以
	 * 印出: 最高分=?、最低分=?、平均=?、總分=?、及格分數列出=?、不及格分數列出=?
	 */
	
	@GetMapping(value = "/json/score", produces = "application/json;charset=utf-8")
	public ResponseEntity<ApiResponse<Object>> getAverageOfScore(@RequestParam(name = "score", required = false) List<Integer> scores) {
		// 統計資料
		IntSummaryStatistics stat = scores.stream().mapToInt(Integer::valueOf).summaryStatistics();
		// 利用 Collectors.partitioningBy 分組
		// key=true 及格分數 | key=false 不及格分數
		Map<Boolean, List<Integer>> resultMap = scores.stream()
				.collect(Collectors.partitioningBy(score -> score >= 60));
		Object data = Map.of(
				"最高分", stat.getMax(),
				"最低分", stat.getMin(),
				"平均", stat.getAverage(),
				"總分", stat.getSum(),
				"及格", resultMap.get(true),
				"不及格", resultMap.get(false)
				);		
		return ResponseEntity.ok(ApiResponse.success("計算成功", data));
	}
	
	/**
	 * 8. 多筆參數轉 Map
	 * name 書名(String), price 價格(Double), amount 數量(Integer), pub 出刊/停刊(Boolean)
	 * 路徑: /json/book?name=Math&price=12.5&amount=10&pub=true
	 * 路徑: /json/book?name=English&price=10.5&amount=20&pub=false
	 * 網址: http://localhost:8080/api/json/book?name=Math&price=12.5&amount=10&pub=true
	 * 網址: http://localhost:8080/api/json/book?name=English&price=10.5&amount=20&pub=false
	 * 讓參數自動轉成 key/value 的 Map 集合
	 * */
	@GetMapping(value = "/json/book" ,produces ="application/json;charset=utf-8")
	public ResponseEntity<ApiResponse<Object>> getBookInfo(
			@RequestParam Map<String, Object> bookMap){
		System.out.printf("bookMap = %s%n",bookMap);
		return ResponseEntity.ok(ApiResponse.success("成功", bookMap));
	}
	
	/*
	 * 9.多筆參數轉model
	 * 路徑: /json/book2?name=Math&price=12.5&amount=10&pub=true
	 * 網址: http://localhost:8080/api/json/book2?name=Math&price=12.5&amount=10&pub=true
	 * */
	@GetMapping(value = "/json/book2" ,produces ="application/json;charset=utf-8")
	public ResponseEntity<ApiResponse<Book>> getBookInfo(
			Book book){
		book.setId(1);
		System.out.printf("bookMap = %s%n",book);
		return ResponseEntity.ok(ApiResponse.success("成功", book));
	}
	
	/**
	 * 10. 路徑參數
	 * 早期設計風格
	 * 路徑: /json/book?id=1 得到 id=1 的書
	 * 路徑: /json/book?id=2 得到 id=2 的書
	 * 
	 * 現代設計風格(REST)
	 * GET /books  查詢所有書籍
	 * GET /book/1 查詢指定書籍
	 * 
	 * 路徑: /json/book/1 得到 id=1 的書
	 * 路徑: /json/book/2 得到 id=2 的書
	 * 網址: http://localhost:8080/api/json/book/1
	 * 網址: http://localhost:8080/api/json/book/2
	 * */
	
	@GetMapping(value = "/json/book/{id}")
	public ResponseEntity<ApiResponse<Book>> getBookById(@PathVariable Integer id){
		//書庫
		List<Book> books = List.of(
				new Book(1,  "小叮噹", 12.5, 25, true),
				new Book(2,  "老夫子", 10.5, 30, true),
				new Book(3,  "好小子", 9.5, 30, true),
				new Book(4, "新樂園", 14.5, 40, true),
				new Book(5, "三國志", 18.5, 50, true)
				);
				
		//根據id 搜尋該筆書籍
				Optional<Book> optBook = books.stream().filter(book->book.getId().equals(id)).findFirst();
				
		//判斷是否有找到
				if(optBook.isEmpty()) {
					return ResponseEntity.badRequest().body(ApiResponse.error("查無此書"));
				}
		Book book = optBook.get();
		return ResponseEntity.ok(ApiResponse.success("查詢成功", book));
	}
	
	/**
	 * Lab
	 * 得到已出版的書籍(pub:true)的書籍
	 * 網址 http://localhost:8080/api/json/book/pub/true
	 * 
	 * 得到未出版的書籍(pub:false)的書籍
	 * 網址 http://localhost:8080/api/json/book/pub/false
	 * */
	
	@GetMapping(value = "/json/books/pub/{isPub}")
	public ResponseEntity<ApiResponse<List<Book>>> queryBook(@PathVariable Boolean isPub){
		//書庫
		List<Book> books = List.of(
				new Book(1,  "小叮噹", 12.5, 25, true),
				new Book(2,  "老夫子", 10.5, 30, true),
				new Book(3,  "好小子", 9.5, 30, true),
				new Book(4, "新樂園", 14.5, 40, true),
				new Book(5, "三國志", 18.5, 50, false)
				);
				
		//根據pub 搜尋該分類書籍
				List<Book>  queryBooks = books.stream().filter(book->book.getPub().equals(isPub)).toList();
				
		//判斷是否有找到
				if(queryBooks.size() ==0) {
					return ResponseEntity.badRequest().body(ApiResponse.error("查無"+(isPub? "出刊":"停刊")+"書籍資料"));
				}
		
		return ResponseEntity.ok(ApiResponse.success("查詢成功:"+(isPub? "出刊":"停刊"), queryBooks));
	}
}

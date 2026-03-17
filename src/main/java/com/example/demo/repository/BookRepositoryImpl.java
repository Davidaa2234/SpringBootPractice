package com.example.demo.repository;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Repository;

import com.example.demo.model.Book;

@Repository //專門負責 "資料存取" 的元件
public class BookRepositoryImpl implements BookRepository{
	//InMemory 的版本
	private List<Book> books = new CopyOnWriteArrayList<>();
	
	//初始資料有四本書
	{
		books.add(new Book(1,  "小叮噹", 12.5, 25, true));
		books.add(new Book(2,  "老夫子", 10.5, 30, true));
		books.add(new Book(3,  "好小子", 9.5, 30, true));
		books.add(new Book(4, "新樂園", 14.5, 40, true));
		books.add(new Book(5, "三國志", 18.5, 50, false));
		
	}
	
	@Override
	public List<Book> findAllBooks() {
		
		return books;
	}

	@Override
	public Optional<Book> getBookById(Integer id) {
		
		return books.stream().filter(book->book.getId().equals(id)).findFirst();
	}

	@Override
	public boolean addBook(Book book) {
		//建立newId
		OptionalInt optMaxId = books.stream().mapToInt(Book::getId).max();//取出目前books 中 id的最大值
		int newId = optMaxId.isEmpty()? 1: optMaxId.getAsInt()+1;
		//將newId 設定給新書 book
		book.setId(newId);
		return books.add(book);
	}

	@Override
	public boolean updateBook(Integer id, Book book) {
		//通過id 找到要修改的書
		Optional<Book> optBook = getBookById(id);
		if(optBook.isEmpty()) {
			return false;
		}
		//得到要修改的書
		Book orginalBook = optBook.get();
		
		//更新欄位資料 
		orginalBook.setAmount(book.getAmount());
		orginalBook.setName(book.getName());
		orginalBook.setPrice(book.getPrice());
		orginalBook.setPub(book.getPub());
		return true;
	}

	@Override
	public boolean deleteBook(Integer id) {
		//通過id 找到要刪除的書
		Optional<Book> optBook = getBookById(id);
		if(optBook.isEmpty()) {
			return false;
			}
		//移除書籍
		return books.remove(optBook.get());
	}
	
}

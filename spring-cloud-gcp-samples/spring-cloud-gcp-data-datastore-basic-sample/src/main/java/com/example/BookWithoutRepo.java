package com.example;

import com.google.cloud.spring.data.datastore.core.mapping.Entity;
import org.springframework.data.annotation.Id;

@Entity(name = "books_no_repo")
public class BookWithoutRepo {
  @Id Long id;

  private final String title;

  private final String author;

  private final int year;

  public BookWithoutRepo(String title, String author, int year) {
    this.title = title;
    this.author = author;
    this.year = year;
  }
}

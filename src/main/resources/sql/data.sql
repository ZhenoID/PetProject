-- data.sql: начальные данные для библиотеки

-- USERS
INSERT INTO users (username, password, role) VALUES
  ('admin', 'admin', 'ADMIN'),
  ('user', 'user', 'USER'),
  ('librarian', 'librarian', 'LIBRARIAN');

-- AUTHORS
INSERT INTO authors (name) VALUES
  ('George Orwell'),
  ('J.K. Rowling'),
  ('J.R.R. Tolkien');

-- GENRES
INSERT INTO genres (name) VALUES
  ('Dystopian'),
  ('Fantasy'),
  ('Adventure'),
  ('Science Fiction');

-- BOOKS
INSERT INTO books (title, author_id, genre_id, year, description, quantity) VALUES
  ('1984', 1, 1, 1949, 'A dystopian novel by George Orwell.', 5),
  ('Animal Farm', 1, 1, 1945, 'An allegorical novella by George Orwell.', 3),
  ('Harry Potter and the Philosopher''s Stone', 2, 2, 1997, 'Первый роман серии о Гарри Поттере.', 10),
  ('The Hobbit', 3, 3, 1937, 'Фэнтези-роман Дж. Р. Р. Толкиена.', 7);

-- BOOK_GENRES (для книг с несколькими жанрами)
INSERT INTO book_genres (book_id, genre_id) VALUES
  (1, 1),
  (2, 1),
  (3, 2),
  (3, 3),
  (4, 2),
  (4, 3);

-- RATINGS
INSERT INTO ratings (user_id, book_id, score) VALUES
  (2, 1, 5),
  (3, 1, 4),
  (2, 4, 5),
  (3, 3, 4);

-- BASKET_ITEMS
INSERT INTO basket_items (user_id, book_id, quantity) VALUES
  (2, 3, 2),
  (3, 2, 1);

-- PURCHASE_HISTORY
INSERT INTO purchase_history (user_id, book_id, quantity, purchase_date) VALUES
  (2, 1, 1, NOW() - INTERVAL '7 days'),
  (3, 4, 1, NOW() - INTERVAL '1 day');

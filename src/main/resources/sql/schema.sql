-- USERS
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL
);

-- AUTHORS
CREATE TABLE authors (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);


-- GENRES
CREATE TABLE genres (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

-- BOOKS
CREATE TABLE books (
  id SERIAL PRIMARY KEY,
  title VARCHAR(200) NOT NULL,
  author_id INT REFERENCES authors(id) ON DELETE SET NULL,
  genre_id  INT REFERENCES genres(id)  ON DELETE SET NULL,
  year INT,
  description TEXT,
  quantity INT NOT NULL DEFAULT 0
);

--BOOK GENRE
CREATE TABLE book_genres (
  book_id  INT REFERENCES books(id)  ON DELETE CASCADE,
  genre_id INT REFERENCES genres(id) ON DELETE CASCADE,
  PRIMARY KEY (book_id, genre_id)
);



-- RATINGS
CREATE TABLE ratings (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    book_id INT REFERENCES books(id) ON DELETE CASCADE,
    score INT CHECK (score >= 1 AND score <= 5)
);

--BASKET
CREATE TABLE basket_items (
  id SERIAL PRIMARY KEY,
  user_id INT    REFERENCES users(id)   ON DELETE CASCADE,
  book_id INT    REFERENCES books(id)   ON DELETE CASCADE,
  quantity INT   NOT NULL
);

--PURCHASE HISTORY
CREATE TABLE purchase_history (
  id SERIAL PRIMARY KEY,
  user_id INT    REFERENCES users(id)   ON DELETE CASCADE,
  book_id INT    REFERENCES books(id)   ON DELETE CASCADE,
  quantity INT   NOT NULL,
  purchase_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

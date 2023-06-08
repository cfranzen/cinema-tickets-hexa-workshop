DROP TABLE IF EXISTS CUSTOMER;
CREATE TABLE CUSTOMER
(
    ID   SERIAL PRIMARY KEY,
    NAME VARCHAR(255) NOT NULL,
    DATA VARCHAR      NOT NULL
);

DROP TABLE IF EXISTS MOVIE;
CREATE TABLE MOVIE
(
    ID          SERIAL PRIMARY KEY,
    TITLE       VARCHAR(255) NOT NULL,
    GENRE       VARCHAR      NOT NULL,
    DESCRIPTION VARCHAR      NOT NULL,
    POSTER_ID   VARCHAR(100) NOT NULL,
    STATE       VARCHAR(100) NOT NULL
);
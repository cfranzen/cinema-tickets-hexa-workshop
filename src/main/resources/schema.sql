create table CUSTOMER
(
    ID   SERIAL PRIMARY KEY,
    DATA VARCHAR NOT NULL
);

create table MOVIE
(
    ID          SERIAL PRIMARY KEY,
    TITLE       VARCHAR(255) NOT NULL,
    GENRE       VARCHAR      NOT NULL,
    DESCRIPTION VARCHAR      NOT NULL,
    poster_id   VARCHAR(100) NOT NULL
);
CREATE TABLE todo (
    todo_id  SERIAL  ,
    user_id  INT ,
    PRIMARY KEY(todo_id)
);

CREATE TABLE TODOELEMENT (
    element_id  SERIAL,
    todo_id  INT NOT NULL,
    title VARCHAR(100) NOT NULL,
    completed BOOLEAN,
    sort_order INT,
    PRIMARY KEY(element_id)
);

CREATE TABLE users (
    id  SERIAL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    PRIMARY KEY (id)
);
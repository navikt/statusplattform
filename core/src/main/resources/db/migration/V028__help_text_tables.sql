
CREATE TABLE help_text
(
    number       INT NOT NULL,
    type         VARCHAR(20)  NOT NULL,
    content text NULL,
    PRIMARY KEY (number, type)
);

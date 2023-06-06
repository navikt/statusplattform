-- oh prefix = opening hours

CREATE TABLE oh_rule
(
    id          UUID         NOT NULL,
    name        VARCHAR(100) NOT NULL,
    rule        VARCHAR(100)  NOT NULL,
    created_at  timestamp with time zone  NOT NULL DEFAULT NOW(),
    updated_at  timestamp with time zone NULL,
    PRIMARY KEY (id)
);


CREATE TABLE oh_group
(
    id                      UUID         NOT NULL,
    name                    VARCHAR(100) NOT NULL,
    rule_group_ids         VARCHAR ARRAY      NOT NULL,
    created_at  timestamp with time zone  NOT NULL DEFAULT NOW(),
    updated_at  timestamp with time zone NULL,
    PRIMARY KEY (id)
);


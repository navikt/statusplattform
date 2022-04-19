CREATE TABLE citizen_user
(
    id UUID NOT NULL,
    firstName VARCHAR(50)  NOT NULL,
    lastName VARCHAR(50) NOT NULL,
    phoneNumber VARCHAR(8),
    email VARCHAR(50),
    created_at timestamp with time zone NOT NULL DEFAULT NOW(),
    updated_at timestamp with time zone NULL,
    PRIMARY KEY (id)
);


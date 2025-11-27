-- =====================================================================
-- /====Enumy====/
create type lang as enum ('PL', 'EN');
create type status as enum ('pending', 'friends','not_friends');


-- =====================================================================
-- /====Tabele====/
CREATE TABLE users
(
    userId   SERIAL PRIMARY KEY,
    username varchar(40) unique not null,
    email    varchar(40) unique not null,
    password varchar(128)       not null
);

CREATE TABLE userPreferences
(
    userId     int not null unique,
    language   lang        default 'PL',                 -- pl,en
    darkMode   boolean     default false,                -- true/false
    userAvatar varchar(30) default 'default_avatar.png', -- nazwa pliku do zdjęcia czy cos

    PRIMARY KEY (userId),
    FOREIGN KEY (userId) REFERENCES users (userId)
);

CREATE TABLE friends
(
    friendshipId SERIAL PRIMARY KEY,
    userId1      int,
    userId2      int,
    status       status default 'not_friends',
    date         date,

    FOREIGN KEY (userId1) REFERENCES users (userId),
    FOREIGN KEY (userId2) REFERENCES users (userId)
);

create table leaderboard
(
    userId     int unique,
    mmr        int default 1000,
    matchesWon int default 0,
    PRIMARY KEY (userId),
    FOREIGN KEY (userId) REFERENCES users (userId)
);


-- =====================================================================
-- /====Funkcje====/
CREATE OR REPLACE FUNCTION add_new_user(
    p_username varchar(40),
    p_email varchar(40),
    p_password varchar(128)
) RETURNS int AS
$$
DECLARE
    v_user_id    int;
    check_result record;
BEGIN
    SELECT *
    INTO check_result
    FROM check_user_unique(p_username, p_email);

    IF check_result.is_username_taken THEN
        RAISE EXCEPTION 'Nazwa użytkownika % jest już zajęta', p_username;
    END IF;

    IF check_result.is_email_taken THEN
        RAISE EXCEPTION 'Email % jest już zajęty', p_email;
    END IF;

    INSERT INTO users (username, email, password)
    VALUES (p_username, p_email, p_password)
    RETURNING userId INTO v_user_id;

    INSERT INTO userPreferences (userId, language, darkMode, userAvatar)
    VALUES (v_user_id,
            'PL',
            false,
            'default_avatar.png');

    INSERT INTO leaderboard (userId, mmr, matchesWon)
    VALUES (v_user_id, 1000, 0);

    RETURN v_user_id;
END;
$$ LANGUAGE plpgsql;

-- ---------------------------------------------------------------------
-- sprawdzanie unikalności podczas rejestracji

--     <<< ZBĘDNE >>>

CREATE OR REPLACE FUNCTION check_user_unique(
    p_username varchar(40),
    p_email varchar(40)
)
    RETURNS TABLE
            (
                is_username_taken boolean,
                is_email_taken    boolean
            )
AS
$$
BEGIN
    RETURN QUERY
        SELECT (EXISTS (SELECT 'Nazwa użytkownika jest zajęta'
                        FROM users
                        WHERE username = p_username)) as is_username_taken,
               (EXISTS (SELECT 'Istnieje konto z tym adresem e-mail'
                        FROM users
                        WHERE email = p_email))       as is_email_taken;
END;
$$ LANGUAGE plpgsql;


-- ---------------------------------------------------------------------
-- Funkcja dodająca znajomego (wysyłająca zaproszenie)
CREATE OR REPLACE FUNCTION add_friend(
    p_user_id1 int, -- użytkownik wysyłający zaproszenie
    p_user_id2 int -- użytkownik zapraszany
) RETURNS int AS
$$
DECLARE
    v_friendship_id int;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM users WHERE userId IN (p_user_id1, p_user_id2)) THEN
        RAISE EXCEPTION 'Jeden lub obaj użytkownicy nie istnieją';
    END IF;

    IF p_user_id1 = p_user_id2 THEN
        RAISE EXCEPTION 'Nie możesz dodać samego siebie do znajomych';
    END IF;

    IF EXISTS (SELECT 1
               FROM friends
               WHERE (userId1 = p_user_id1 AND userId2 = p_user_id2)
                  OR (userId1 = p_user_id2 AND userId2 = p_user_id1)) THEN
        RAISE EXCEPTION 'Zaproszenie już istnieje lub użytkownicy są już znajomymi';
    END IF;

    INSERT INTO friends (userId1, userId2, status, date)
    VALUES (p_user_id1, p_user_id2, 'pending', CURRENT_DATE)
    RETURNING friendshipId INTO v_friendship_id;

    RETURN v_friendship_id;
END;
$$ LANGUAGE plpgsql;

-- ---------------------------------------------------------------------
-- Funkcja akceptująca zaproszenie do znajomych
CREATE OR REPLACE FUNCTION accept_friend_request(
    p_user_id int, -- użytkownik akceptujący zaproszenie
    p_friendship_id int -- ID zaproszenia do znajomych
) RETURNS boolean AS
$$
DECLARE
    v_status         varchar(10);
    v_target_user_id int;
BEGIN
    SELECT status, userId2
    INTO v_status, v_target_user_id
    FROM friends
    WHERE friendshipId = p_friendship_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Zaproszenie nie istnieje';
    END IF;

    IF v_target_user_id != p_user_id THEN
        RAISE EXCEPTION 'Nie możesz zaakceptować tego zaproszenia';
    END IF;

    IF v_status != 'pending' THEN
        RAISE EXCEPTION 'To zaproszenie nie jest w stanie oczekującym';
    END IF;

    UPDATE friends
    SET status = 'friends'
    WHERE friendshipId = p_friendship_id;

    RETURN true;
END;
$$ LANGUAGE plpgsql;


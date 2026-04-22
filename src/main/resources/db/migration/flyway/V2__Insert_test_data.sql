-- Тестовые данные

-- ПОЛЬЗОВАТЕЛИ
INSERT INTO users (id, username, email, password, balance, role, registered_at) VALUES
                                                                                    (1, 'ivan', 'ivan@example.com', '$2a$10$encoded', 15000.00, 'USER', '2024-01-01 10:00:00'),
                                                                                    (2, 'petr', 'petr@example.com', '$2a$10$encoded', 5000.00, 'ARTIST', '2024-01-02 11:00:00'),
                                                                                    (3, 'maria', 'maria@example.com', '$2a$10$encoded', 20000.00, 'STUDIO', '2024-01-03 12:00:00'),
                                                                                    (4, 'admin', 'admin@example.com', '$2a$10$encoded', 0.00, 'ADMIN', '2024-01-04 13:00:00'),
                                                                                    (5, 'anna', 'anna@example.com', '$2a$10$encoded', 3000.00, 'ARTIST', '2024-01-05 14:00:00'),
                                                                                    (6, 'artist', 'artist@example.com', '$2a$10$encoded', 0.00, 'ARTIST', NOW()),
                                                                                    (7, 'user', 'user@example.com', '$2a$10$encoded', 10000.00, 'USER', NOW());


SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));

-- ПРОФИЛИ
INSERT INTO profiles (id, user_id, bio, is_artist, is_studio) VALUES
                                                                  (1, 1, 'Люблю заказывать скины для своего сервера', FALSE, FALSE),
                                                                  (2, 2, 'Художник, создаю скины в стиле пиксель-арт и реализм', TRUE, FALSE),
                                                                  (3, 3, 'Студия "Art Masters"', FALSE, TRUE),
                                                                  (4, 4, 'Администратор платформы', FALSE, FALSE),
                                                                  (5, 5, 'Художница, аниме-стиль', TRUE, FALSE),
                                                                  (6, 6, 'Художник, специализируюсь на фэнтези', TRUE, FALSE),
                                                                  (7, 7, 'Обычный пользователь', FALSE, FALSE);

SELECT setval('profiles_id_seq', (SELECT MAX(id) FROM profiles));

-- ПРОФИЛИ ХУДОЖНИКОВ
INSERT INTO artist_profiles (id, profile_id, styles, min_price, average_time, is_available) VALUES
                                                                                                (1, 2, 'классический, реализм', 1000.00, 3, TRUE),
                                                                                                (2, 5, 'Cel-shading, Flat, аниме', 800.00, 2, TRUE),
                                                                                                (3, 6, 'фэнтези, средневековье', 1200.00, 4, TRUE);

SELECT setval('artist_profiles_id_seq', (SELECT MAX(id) FROM artist_profiles));

-- СТУДИИ
INSERT INTO studios (id, profile_id, name, description, founded_at, manager_id) VALUES
    (1, 3, 'Art Masters', 'Студия профессиональных художников по скинам', '2023-01-15', 3);

SELECT setval('studios_id_seq', (SELECT MAX(id) FROM studios));

-- ЗАКАЗЫ
INSERT INTO orders (id, customer_id, artist_id, status, description, price, created_at, completed_at) VALUES
                                                                                                          (1, 1, 2, 'COMPLETED', 'Скин гнома пивовара', 1500.00, '2024-01-10 10:00:00', '2024-01-15 14:00:00'),
                                                                                                          (2, 1, 2, 'IN_PROGRESS', 'Скин в стиле Америки 60х', 1200.00, '2024-03-01 14:30:00', NULL),
                                                                                                          (3, 7, 5, 'NEW', 'Аниме скин для косплея', 800.00, NOW(), NULL),
                                                                                                          (4, 1, 5, 'REVIEW', 'Скин рыцаря', 1000.00, NOW(), NULL),
                                                                                                          (5, 7, 2, 'NEW', 'Скин дракона', 2000.00, NOW(), NULL);

SELECT setval('orders_id_seq', (SELECT MAX(id) FROM orders));

-- СООБЩЕНИЯ
INSERT INTO messages (id, order_id, sender_id, content, is_preview, sent_at) VALUES
                                                                                 (1, 1, 1, 'Здравствуйте! Хочу заказать гнома пивовара', FALSE, '2024-01-10 10:05:00'),
                                                                                 (2, 1, 2, 'Привет! Какой стиль предпочитаете? Можете скинуть референсы?', FALSE, '2024-01-10 10:10:00'),
                                                                                 (3, 1, 1, 'Вот референс: ссылка на картинку', FALSE, '2024-01-10 10:15:00'),
                                                                                 (4, 1, 2, 'Понял, беру в работу!', FALSE, '2024-01-10 10:20:00'),
                                                                                 (5, 2, 1, 'Ээ...', FALSE, '2024-03-01 15:00:00'),
                                                                                 (6, 2, 2, 'Вот набросок, посмотрите', TRUE, '2024-03-02 11:00:00'),
                                                                                 (7, 2, 1, 'Отлично! Продолжайте!', FALSE, '2024-03-02 11:05:00'),
                                                                                 (8, 4, 7, 'Когда будет готов скин?', FALSE, NOW()),
                                                                                 (9, 4, 5, 'Через 2 дня отправлю на проверку', FALSE, NOW());

SELECT setval('messages_id_seq', (SELECT MAX(id) FROM messages));
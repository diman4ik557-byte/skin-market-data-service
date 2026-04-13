-- Тестовые данные для Skin Market Platform

-- Пользователи
INSERT INTO users (id, username, email, password, balance, role, registered_at) VALUES
                                                                                    (1, 'ivan', 'ivan@example.com', 'password123', 10000.00, 'USER', '2024-01-01 10:00:00'),
                                                                                    (2, 'petr', 'petr@example.com', 'password123', 5000.00, 'ARTIST', '2024-01-02 11:00:00'),
                                                                                    (3, 'maria', 'maria@example.com', 'password123', 20000.00, 'STUDIO', '2024-01-03 12:00:00'),
                                                                                    (4, 'admin', 'admin@example.com', 'admin123', 0.00, 'ADMIN', '2024-01-04 13:00:00'),
                                                                                    (5, 'anna', 'anna@example.com', 'password123', 3000.00, 'ARTIST', '2024-01-05 14:00:00');

-- Профили
INSERT INTO profiles (id, user_id, bio, is_artist, is_studio) VALUES
                                                                  (1, 1, 'Люблю заказывать скины для своего сервера', FALSE, FALSE),
                                                                  (2, 2, 'Художник, создаю скины в стиле пиксель и реализм', TRUE, FALSE),
                                                                  (3, 3, 'Студия "Art Masters"', FALSE, TRUE),
                                                                  (4, 4, 'Администратор платформы', FALSE, FALSE),
                                                                  (5, 5, 'Художница, аниме-стиль', TRUE, FALSE);

-- Социальные ссылки
INSERT INTO social_links (id, profile_id, platform, user_identifier, is_primary) VALUES
                                                                                     (1, 2, 'VK', 'petr_artist', TRUE),
                                                                                     (2, 2, 'DISCORD', 'petr#1234', FALSE),
                                                                                     (3, 3, 'INSTAGRAM', 'art_masters', TRUE);

-- Студии
INSERT INTO studios (id, profile_id, name, description, founded_at, manager_id) VALUES
    (1, 3, 'Art Masters', 'Студия профессиональных художников по скинам', '2023-01-15', 3);

-- Профили художников
INSERT INTO artist_profiles (id, profile_id, studio_id, styles, min_price, average_time, is_available) VALUES
                                                                                                           (1, 2, 1, 'классический, реализм', 1000.00, 3, TRUE),
                                                                                                           (2, 5, NULL, 'Cel-shading, Flat', 800.00, 2, TRUE);

-- Участники студий
INSERT INTO studio_members (id, studio_id, member_id, role, joined_at) VALUES
                                                                           (1, 1, 1, 'MANAGER', '2024-01-01 10:00:00'),
                                                                           (2, 1, 2, 'ARTIST', '2024-01-02 11:00:00');

-- Заказы
INSERT INTO orders (id, customer_id, artist_id, status, description, price, created_at) VALUES
                                                                                            (1, 1, 2, 'COMPLETED', 'Скин гнома пивовара', 1500.00, '2024-01-10 10:00:00'),
                                                                                            (2, 1, 2, 'IN_PROGRESS', 'Скин в стиле Америки 60х', 1200.00, '2024-03-01 14:30:00'),
                                                                                            (3, 5, 2, 'NEW', 'Перерисовка под новый год', 800.00, '2024-03-15 09:00:00');

-- Сообщения
INSERT INTO messages (id, order_id, sender_id, content, is_preview, sent_at) VALUES
                                                                                 (1, 1, 1, 'Здравствуйте! Хочу заказать гнома пивовара', FALSE, '2024-01-10 10:05:00'),
                                                                                 (2, 1, 2, 'Привет! Какой стиль предпочитаете? Можете скинуть референсы?', FALSE, '2024-01-10 10:10:00'),
                                                                                 (3, 2, 1, 'Ээ...', FALSE, '2024-03-01 15:00:00'),
                                                                                 (4, 2, 2, 'Вот набросок, посмотрите', TRUE, '2024-03-02 11:00:00');

-- Сброс последовательностей (чтобы следующие id продолжали с правильного числа)
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('profiles_id_seq', (SELECT MAX(id) FROM profiles));
SELECT setval('social_links_id_seq', (SELECT MAX(id) FROM social_links));
SELECT setval('studios_id_seq', (SELECT MAX(id) FROM studios));
SELECT setval('artist_profiles_id_seq', (SELECT MAX(id) FROM artist_profiles));
SELECT setval('studio_members_id_seq', (SELECT MAX(id) FROM studio_members));
SELECT setval('orders_id_seq', (SELECT MAX(id) FROM orders));
SELECT setval('messages_id_seq', (SELECT MAX(id) FROM messages));
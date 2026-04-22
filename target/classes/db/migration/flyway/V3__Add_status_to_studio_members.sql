-- Добавляем колонку status, если она еще не существует
-- Для PostgreSQL используем DO блок
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='studio_members' AND column_name='status') THEN
        ALTER TABLE studio_members ADD COLUMN status VARCHAR(20) DEFAULT 'PENDING';
    END IF;
END $$;

-- Обновляем существующие записи
UPDATE studio_members SET status = 'APPROVED' WHERE status IS NULL;

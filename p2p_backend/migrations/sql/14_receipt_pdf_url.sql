ALTER TABLE transactions
    ADD COLUMN IF NOT EXISTS receipt_pdf_url TEXT;

ALTER TABLE transactions
    ALTER COLUMN status TYPE VARCHAR(40);

ALTER TABLE vouchers
    ALTER COLUMN status TYPE VARCHAR(40);

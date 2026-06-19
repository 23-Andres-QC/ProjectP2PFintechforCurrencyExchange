-- Aumenta el límite del campo status en transactions y vouchers
-- VARCHAR(20) era insuficiente para valores como 'seller_voucher_uploaded' (23 chars)
ALTER TABLE transactions ALTER COLUMN status TYPE VARCHAR(40);
ALTER TABLE vouchers     ALTER COLUMN status TYPE VARCHAR(40);

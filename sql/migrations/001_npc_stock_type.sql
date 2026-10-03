BEGIN;

ALTER TABLE mobiles ADD COLUMN IF NOT EXISTS stock_type VARCHAR(255);

UPDATE mobiles
SET stock_type = COALESCE(stock_type, behavior->>'stockType', runtime_attr->>'behavior.stockType')
WHERE type = 'N';

COMMIT;

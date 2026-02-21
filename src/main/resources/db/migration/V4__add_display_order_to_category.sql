-- Add display_order column to category table
ALTER TABLE category ADD COLUMN display_order INT NOT NULL DEFAULT 0;

-- Reading material shown to a test-taker for a section: a written passage /
-- instructions ("information") and/or an embedded PDF rendered inline.
ALTER TABLE sections ADD COLUMN content TEXT;
ALTER TABLE sections ADD COLUMN pdf_url TEXT;

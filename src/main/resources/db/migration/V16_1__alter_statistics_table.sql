CREATE PROCEDURE create_statistics() AS $$
 DECLARE
 	cnt INTEGER;
	newId INTEGER;
    temprow  RECORD;
BEGIN
    cnt := (SELECT COUNT(*) FROM bucket);
	FOR temprow IN
        SELECT * FROM bucket
    LOOP
        INSERT INTO statistic (num_of_downloads,num_of_uploads) VALUES (0,0) RETURNING id INTO newId;
		UPDATE bucket SET statistic_id=newId WHERE id=temprow.id;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

CALL create_statistics();

ALTER TABLE "bucket"
ALTER COLUMN statistic_id SET NOT NULL;
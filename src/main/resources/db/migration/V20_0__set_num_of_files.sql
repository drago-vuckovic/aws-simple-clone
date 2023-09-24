CREATE PROCEDURE set_num_of_files() AS $$
DECLARE
    cnt INTEGER;
    temprow RECORD;
BEGIN
    FOR temprow IN SELECT * FROM bucket
        LOOP
            cnt := (SELECT COUNT(*) FROM file WHERE bucket_id=temprow.id AND is_folder=TRUE);
            UPDATE statistic SET num_of_files=cnt WHERE id=temprow.statistic_id;
        END LOOP;
END;
$$ LANGUAGE plpgsql;

CALL set_num_of_files();

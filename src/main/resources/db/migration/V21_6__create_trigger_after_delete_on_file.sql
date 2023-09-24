CREATE OR REPLACE FUNCTION decrement_num_of_files()
   RETURNS TRIGGER
   LANGUAGE PLPGSQL
AS $$
DECLARE
  statistic_id_var INTEGER;
BEGIN
    IF OLD.is_folder IS FALSE THEN
      statistic_id_var:=(SELECT statistic_id FROM bucket WHERE id=OLD.bucket_id);
      UPDATE statistic SET num_of_files=num_of_files-1 WHERE id=statistic_id_var;
    END IF;
    RETURN NULL;
END;
$$;


CREATE TRIGGER decrement_num_of_files
AFTER DELETE ON file
FOR EACH ROW EXECUTE PROCEDURE decrement_num_of_files();
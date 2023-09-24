CREATE OR REPLACE FUNCTION increment_num_of_files()
   RETURNS TRIGGER
   LANGUAGE PLPGSQL
AS $$
DECLARE
  statistic_id_var INTEGER;
BEGIN
    IF NEW.is_folder IS FALSE THEN
      statistic_id_var:=(SELECT statistic_id FROM bucket WHERE id=NEW.bucket_id);
      UPDATE statistic SET num_of_files=num_of_files+1, num_of_uploads=num_of_uploads+1 WHERE id=statistic_id_var;
    END IF;
    RETURN NEW;
END;
$$;


CREATE TRIGGER increment_num_of_files
AFTER INSERT ON file
FOR EACH ROW EXECUTE PROCEDURE increment_num_of_files();
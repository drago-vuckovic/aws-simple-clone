DROP TRIGGER decrement_num_of_files ON file;
DROP TRIGGER increment_num_of_files ON file;

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
      UPDATE bucket SET size=size-OLD.size WHERE id=OLD.bucket_id;
    END IF;
    RETURN NULL;
END;
$$;


CREATE TRIGGER decrement_num_of_files
AFTER DELETE ON file
FOR EACH ROW EXECUTE PROCEDURE decrement_num_of_files();


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
      UPDATE bucket SET size=size+NEW.size WHERE id=NEW.bucket_id;
    END IF;
    RETURN NEW;
END;
$$;


CREATE TRIGGER increment_num_of_files
AFTER INSERT ON file
FOR EACH ROW EXECUTE PROCEDURE increment_num_of_files();
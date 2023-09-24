CREATE PROCEDURE set_subscription_packages() AS $$
 DECLARE
	packageId INTEGER;
  temprow  RECORD;
BEGIN
  packageId:=(SELECT id FROM subscription_package WHERE subscription_type=0);
	FOR temprow IN
        SELECT * FROM tenant
    LOOP
      IF temprow.subscription_package_id IS NULL THEN
          UPDATE tenant SET subscription_package_id=packageId WHERE id=temprow.id;
      END IF;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

CALL set_subscription_packages();
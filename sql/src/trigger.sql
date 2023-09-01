CREATE LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION orderid_trigger()
RETURNS "trigger" AS
$BODY$
BEGIN
NEW.orderid := nextval('orders_orderid_seq');
RETURN NEW;
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

CREATE TRIGGER trigger_orderid BEFORE INSERT
ON Orders FOR EACH ROW
EXECUTE PROCEDURE orderid_trigger();



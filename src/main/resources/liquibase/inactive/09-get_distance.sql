CREATE FUNCTION get_distance(lat1 DOUBLE PRECISION, lon1 DOUBLE PRECISION, lat2 DOUBLE PRECISION, lon2 DOUBLE PRECISION)
   returns DECIMAL
   language plpgsql
  as
$$
declare
-- variable declaration
    phi1 DOUBLE PRECISION;
    phi2 DOUBLE PRECISION;
    delta_phi DOUBLE PRECISION;
    delta_lambda DOUBLE PRECISION;
    a DOUBLE PRECISION;
    c DOUBLE PRECISION;
begin
 -- logic
    phi1 = lat1 * PI() / 180;
    phi2 = lat2 * PI() / 180;
    delta_phi = phi2 - phi1;
    delta_lambda = (lon2 - lon1) * PI() / 180;

    a = SIN(delta_phi / 2) * SIN(delta_phi / 2) + COS(phi1) * COS(phi2) * SIN(delta_lambda / 2) * SIN(delta_lambda / 2);
    c = 2 * ATAN2(SQRT(a), SQRT(1 - a));

    RETURN c * 6371 * 1000;
end;
$$
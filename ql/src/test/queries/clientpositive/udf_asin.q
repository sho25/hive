SELECT asin(null)
FROM src LIMIT 1;

SELECT asin(0)
FROM src LIMIT 1;

SELECT asin(-0.5), asin(0.66)
FROM src LIMIT 1;

SELECT asin(2)
FROM src LIMIT 1;

CREATE DATABASE binance;;
drop table binance.order;

CREATE TABLE binance.order
(
	created_at DateTime,
	e String,
	E UInt64,
	s String,
	U UInt64,
	u UInt64,
	b String, --Array(Tuple(String, String)),
	a String --Array(Tuple(String, String))
) ENGINE = MergeTree order by tuple();;
--PARTITION BY toYYYYMM(created_at)
--todo add PARTITION BY date ORDER BY (time, ad_id) SAMPLE BY ad_id SETTINGS index_granularity = 8192
--todo apply by docker-compose

select *
from binance.order;


/*
Array of T-type items. The T type can be any type, including an array. We don’t recommend using multidimensional arrays,
because they are not well supported (for example, you can’t store multidimensional arrays in tables with engines from
MergeTree family).
*/
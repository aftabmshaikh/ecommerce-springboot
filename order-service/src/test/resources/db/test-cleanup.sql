-- Clean up test data (works for H2 and PostgreSQL)
DELETE FROM order_return_items;
DELETE FROM order_returns;
DELETE FROM order_status_history;
DELETE FROM order_items;
DELETE FROM orders;

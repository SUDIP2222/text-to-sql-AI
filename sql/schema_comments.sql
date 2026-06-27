-- =======================
-- Table Descriptions
-- =======================

COMMENT ON TABLE employees IS
'Company employees including personal details, role, salary, and employment status';

COMMENT ON TABLE projects IS
'Projects owned by departments with budget and lifecycle information';

COMMENT ON TABLE orders IS
'Customer purchase orders including total amount and order status';

-- =======================
-- Column Descriptions
-- =======================

COMMENT ON COLUMN employees.status IS
'Employment status of the employee. Possible values: ACTIVE, EXITED';

COMMENT ON COLUMN employees.salary IS
'Monthly salary of the employee in local currency';

COMMENT ON COLUMN projects.budget IS
'Total approved budget allocated for the project';

COMMENT ON COLUMN orders.total_amount IS
'Total monetary value of the order';

COMMENT ON COLUMN invoices.status IS
'Invoice payment status such as PAID, UNPAID, or OVERDUE';
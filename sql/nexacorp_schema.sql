

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =======================
-- Departments
-- =======================
CREATE TABLE departments (
                             id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                             name        VARCHAR(100) NOT NULL,
                             location    VARCHAR(100),
                             created_at  DATE NOT NULL DEFAULT CURRENT_DATE
);

-- =======================
-- Employees
-- =======================
CREATE TABLE employees (
                           id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                           department_id UUID NOT NULL,
                           first_name    VARCHAR(100) NOT NULL,
                           last_name     VARCHAR(100) NOT NULL,
                           email         VARCHAR(150) UNIQUE NOT NULL,
                           role          VARCHAR(100) NOT NULL,
                           salary        NUMERIC(12,2) NOT NULL,
                           join_date     DATE NOT NULL,
                           status        VARCHAR(20) NOT NULL,
                           CONSTRAINT fk_employee_department
                               FOREIGN KEY (department_id)
                                   REFERENCES departments(id)
);

-- =======================
-- Projects
-- =======================
CREATE TABLE projects (
                          id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          department_id UUID NOT NULL,
                          name          VARCHAR(150) NOT NULL,
                          budget        NUMERIC(14,2) NOT NULL,
                          start_date    DATE NOT NULL,
                          end_date      DATE,
                          status        VARCHAR(30) NOT NULL,
                          CONSTRAINT fk_project_department
                              FOREIGN KEY (department_id)
                                  REFERENCES departments(id)
);

-- =======================
-- Project Assignments
-- =======================
CREATE TABLE project_assignments (
                                     id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                     project_id      UUID NOT NULL,
                                     employee_id     UUID NOT NULL,
                                     allocation_pct  INTEGER NOT NULL CHECK (allocation_pct BETWEEN 1 AND 100),
                                     assigned_date   DATE NOT NULL,
                                     CONSTRAINT fk_assignment_project
                                         FOREIGN KEY (project_id)
                                             REFERENCES projects(id),
                                     CONSTRAINT fk_assignment_employee
                                         FOREIGN KEY (employee_id)
                                             REFERENCES employees(id),
                                     CONSTRAINT uq_project_employee
                                         UNIQUE (project_id, employee_id)
);

-- =======================
-- Customers
-- =======================
CREATE TABLE customers (
                           id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                           name        VARCHAR(150) NOT NULL,
                           industry    VARCHAR(100),
                           country     VARCHAR(100),
                           created_at  DATE NOT NULL DEFAULT CURRENT_DATE
);

-- =======================
-- Orders
-- =======================
CREATE TABLE orders (
                        id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                        customer_id   UUID NOT NULL,
                        order_date    DATE NOT NULL,
                        total_amount  NUMERIC(14,2) NOT NULL,
                        status        VARCHAR(30) NOT NULL,
                        CONSTRAINT fk_order_customer
                            FOREIGN KEY (customer_id)
                                REFERENCES customers(id)
);

-- =======================
-- Invoices
-- =======================
CREATE TABLE invoices (
                          id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          order_id      UUID NOT NULL,
                          invoice_date  DATE NOT NULL,
                          amount        NUMERIC(14,2) NOT NULL,
                          due_date      DATE NOT NULL,
                          status        VARCHAR(30) NOT NULL,
                          CONSTRAINT fk_invoice_order
                              FOREIGN KEY (order_id)
                                  REFERENCES orders(id)
);

-- =======================
-- Payments
-- =======================
CREATE TABLE payments (
                          id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          invoice_id    UUID NOT NULL,
                          payment_date  DATE NOT NULL,
                          amount        NUMERIC(14,2) NOT NULL,
                          method        VARCHAR(50) NOT NULL,
                          CONSTRAINT fk_payment_invoice
                              FOREIGN KEY (invoice_id)
                                  REFERENCES invoices(id)
);
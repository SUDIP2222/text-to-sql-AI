# Complex Test Queries for Text-to-SQL

এই ফাইলটিতে `nexacorp_schema.sql` এর উপর ভিত্তি করে কিছু জটিল (Complex) Natural Language কুয়েরি এবং তাদের সম্ভাব্য SQL দেওয়া হলো। এগুলো প্রজেক্টের validation এবং performance টেস্ট করার জন্য ব্যবহার করা যেতে পারে।

### ১. Multi-table Join ও Aggregation
**প্রশ্ন:** প্রতিটা ডিপার্টমেন্টের নাম এবং সেই ডিপার্টমেন্টে বর্তমানে কতজন 'ACTIVE' এমপ্লয়ি আছে এবং তাদের মোট বেতন কত, তা দেখাও।

**সম্ভাব্য SQL:**
```sql
SELECT d.name, COUNT(e.id) AS employee_count, SUM(e.salary) AS total_salary
FROM departments d
JOIN employees e ON d.id = e.department_id
WHERE e.status = 'ACTIVE'
GROUP BY d.name;
```

### ২. Subquery এবং Filter
**প্রশ্ন:** সেইসব প্রজেক্টের নাম দেখাও যেগুলোর বাজেট গড় (average) প্রজেক্ট বাজেটের চেয়ে বেশি।

**সম্ভাব্য SQL:**
```sql
SELECT name, budget
FROM projects
WHERE budget > (SELECT AVG(budget) FROM projects);
```

### ৩. Complex Conditional Aggregation
**প্রশ্ন:** প্রতিটা কাস্টমারের জন্য তাদের মোট অর্ডারের সংখ্যা এবং কতগুলো অর্ডার 'COMPLETED' স্ট্যাটাসে আছে তা বের করো।

**সম্ভাব্য SQL:**
```sql
SELECT c.name, 
       COUNT(o.id) AS total_orders,
       COUNT(CASE WHEN o.status = 'COMPLETED' THEN 1 END) AS completed_orders
FROM customers c
LEFT JOIN orders o ON c.id = o.customer_id
GROUP BY c.id, c.name;
```

### ৪. Time-based Analysis এবং Join
**প্রশ্ন:** ২০২৪ সালে যেসকল ইনভয়েস জেনারেট হয়েছে সেগুলোর জন্য কাস্টমারের নাম, ইনভয়েসের তারিখ এবং ইনভয়েসের পরিমাণ (amount) দেখাও।

**সম্ভাব্য SQL:**
```sql
SELECT c.name, i.invoice_date, i.amount
FROM customers c
JOIN orders o ON c.id = o.customer_id
JOIN invoices i ON o.id = i.order_id
WHERE i.invoice_date BETWEEN '2024-01-01' AND '2024-12-31';
```

### ৫. Resource Allocation (Many-to-Many Join)
**প্রশ্ন:** এমন এমপ্লয়িদের নাম এবং রোলের তালিকা দাও যারা একাধিক (more than 1) প্রজেক্টে কাজ করছে।

**সম্ভাব্য SQL:**
```sql
SELECT e.first_name, e.last_name, e.role
FROM employees e
JOIN project_assignments pa ON e.id = pa.employee_id
GROUP BY e.id, e.first_name, e.last_name, e.role
HAVING COUNT(pa.project_id) > 1;
```

### ৬. Financial Summary with Multi-level Joins
**প্রশ্ন:** কোন কাস্টমার এ পর্যন্ত সব মিলিয়ে কত টাকা পেমেন্ট করেছে তা বের করো।

**সম্ভাব্য SQL:**
```sql
SELECT c.name, SUM(p.amount) AS total_paid
FROM customers c
JOIN orders o ON c.id = o.customer_id
JOIN invoices i ON o.id = i.order_id
JOIN payments p ON i.id = p.invoice_id
GROUP BY c.id, c.name;
```

### ৭. Department Performance
**প্রশ্ন:** সেই ডিপার্টমেন্টের নাম দেখাও যেখানে সবচেয়ে বেশি বাজেটের প্রজেক্ট রয়েছে।

**সম্ভাব্য SQL:**
```sql
SELECT d.name
FROM departments d
JOIN projects p ON d.id = p.department_id
ORDER BY p.budget DESC
LIMIT 1;
```

### ৮. Industry-wise Revenue
**প্রশ্ন:** কোন ইন্ডাস্ট্রির কাস্টমারদের থেকে সবচেয়ে বেশি রেভিনিউ (total order amount) এসেছে?

**সম্ভাব্য SQL:**
```sql
SELECT c.industry, SUM(o.total_amount) AS total_revenue
FROM customers c
JOIN orders o ON c.id = o.customer_id
GROUP BY c.industry
ORDER BY total_revenue DESC;
```

### ৯. Unpaid Invoices
**প্রশ্ন:** সেইসব কাস্টমারদের নাম এবং ইনভয়েস আইডি দেখাও যাদের পেমেন্ট এখনও 'PENDING' এবং ডিউ ডেট পার হয়ে গেছে।

**সম্ভাব্য SQL:**
```sql
SELECT c.name, i.id AS invoice_id, i.due_date
FROM customers c
JOIN orders o ON c.id = o.customer_id
JOIN invoices i ON o.id = i.order_id
WHERE i.status = 'PENDING' AND i.due_date < CURRENT_DATE;
```

### ১০. Employee Allocation Detail
**প্রশ্ন:** প্রতিটি প্রজেক্টের নাম এবং সেখানে কতজন এমপ্লয়ি কাজ করছে এবং তাদের গড় অ্যালোকেশন পার্সেন্টেজ কত?

**সম্ভাব্য SQL:**
```sql
SELECT p.name AS project_name, 
       COUNT(pa.employee_id) AS total_employees, 
       AVG(pa.allocation_pct) AS avg_allocation
FROM projects p
LEFT JOIN project_assignments pa ON p.id = pa.project_id
GROUP BY p.id, p.name;
```

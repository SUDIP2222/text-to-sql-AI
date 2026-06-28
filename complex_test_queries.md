# AI Model Testing: Complex SQL Scenarios (IVAC Project)

এই ফাইলটিতে কিছু জটিল ন্যাচারাল ল্যাঙ্গুয়েজ প্রশ্ন দেওয়া হয়েছে যা আপনি AI মডেলকে জিজ্ঞেস করে তার দক্ষতা যাচাই করতে পারেন। এই প্রশ্নগুলো জয়েন, এগ্রিগেশন এবং বিজনেস রুলসের সমন্বয়ে তৈরি।

---

### Scenario 1: পেমেন্ট ও অ্যাপয়েন্টমেন্ট রিপোর্ট
**Question:** "Show me the total amount of successful payments for each IVAC center in Dhaka mission for the last 30 days."
*   **Target Logic:** `appointments` -> `ivac_centers` -> `high_commissions` -> `payment_transactions` টেবিলে জয়েন করতে হবে। পেমেন্ট স্ট্যাটাস 'SUCCESS' হতে হবে এবং টাইমস্ট্যাম্প ফিল্টার করতে হবে।

### Scenario 2: ইউজার অ্যাক্টিভিটি ও ব্লকিং চেক
**Question:** "List all users who have registered in the last 7 days but haven't uploaded any files yet, along with their phone numbers."
*   **Target Logic:** `users` এবং `appointment_files` এর মধ্যে `LEFT JOIN` করে যেখানে ফাইল আইডি NULL এবং রেজিস্ট্রেশন ডেট ফিল্টার করতে হবে।

### Scenario 3: হাই-ভলিউম অ্যাপয়েন্টমেন্ট সেন্টার
**Question:** "Which IVAC centers have more than 50 confirmed bookings for the next working day, and who are the high commissions for those centers?"
*   **Target Logic:** `appointments` এবং `ivac_centers` জয়েন করে `COUNT` করতে হবে এবং `HAVING count > 50` ব্যবহার করতে হবে।

### Scenario 4: ভিসা টাইপ ভিত্তিক পেমেন্ট এনালাইসিস
**Question:** "What is the average paid amount for 'Tourist Visa' applicants in the Chittagong center who paid via 'SSL' channel?"
*   **Target Logic:** `appointments_revamp`, `visa_types` এবং `payment_transactions` এর মধ্যে জয়েন। নির্দিষ্ট চ্যানেল এবং ভিসা টাইপ ফিল্টার।

### Scenario 5: কুলিং-অফ পিরিয়ড ভায়োলেশন চেক
**Question:** "Find users who tried to upload a file within 15 days of their last successful booking."
*   **Target Logic:** এটি একটি জটিল কুয়েরি যেখানে `appointment_files` টেবিলের `upload_day` এবং `appointments` টেবিলের ডেট কম্পেয়ার করতে হবে (Self-join বা Sub-query লাগতে পারে)।

### Scenario 6: ডিলেটেড রেকর্ড ও একটিভ স্ট্যাটাস ফিল্টারিং
**Question:** "Get the names and emails of all active system users who have 'ADMIN' roles, excluding any deleted records."
*   **Target Logic:** `system_users`, `system_user_roles` এবং `system_roles` জয়েন। সাথে `is_active = true` এবং `is_deleted = false` ফিল্টার।

### Scenario 7: রিফান্ড ট্রানজাকশন এনালাইসিস
**Question:** "Show the total amount of successfully refunded transactions for the last month, grouped by the original payment channel used."
*   **Target Logic:** `refund_transactions` এবং `payment_transactions` জয়েন করতে হবে। রিফান্ড স্ট্যাটাস 'SUCCESS' হতে হবে এবং `payment_channel` দিয়ে গ্রুপ করতে হবে।

### Scenario 8: মাল্টি-অ্যাপ্লিকেন্ট অ্যাপয়েন্টমেন্ট ট্রেন্ড
**Question:** "Find the average number of applicants per booking for each visa type in the last 6 months, only for confirmed appointments."
*   **Target Logic:** `appointments` (বা `appointments_revamp`) এবং `visa_types` জয়েন। `AVG(number_of_applicants)` এগ্রিগেশন এবং নির্দিষ্ট টাইম পিরিয়ড ফিল্টার।

### Scenario 9: পেমেন্ট চ্যানেল ভিত্তিক ফেইলুর রেট
**Question:** "Identify which payment channel has the highest number of FAILED transactions for appointments in the JFP Dhaka center."
*   **Target Logic:** `payment_transactions`, `appointments` এবং `ivac_centers` জয়েন। স্ট্যাটাস 'FAILED' ফিল্টার এবং `COUNT` করে `ORDER BY` এবং `LIMIT 1` ব্যবহার করতে হবে।

### Scenario 10: স্লট কনফিগারেশন অডিট ট্র্যাকিং
**Question:** "List all changes made to the appointment slot configuration for 'Tourist Visa' during June 2026, including who made the changes."
*   **Target Logic:** `slot_config_audits` এবং `visa_types` জয়েন (যদি অডিটে আইডি থাকে)। তারিখ ফিল্টার এবং ইউজার নেম রিট্রাইভ করা।

### Scenario 11: ডুপ্লিকেট পাসপোর্ট চেক (Security Audit)
**Question:** "Search for any passport numbers that appear in more than one appointment file uploaded in the last 24 hours."
*   **Target Logic:** `appointment_files` টেবিলে `passport` কলামে `GROUP BY` করে `HAVING COUNT(*) > 1` চেক করতে হবে। এটি বিজনেস রুল ২৮-এর একটি ভায়োলেশন চেক।

### Scenario 12: পেমেন্ট চ্যানেল ভিত্তিক মার্কেট শেয়ার ও ট্রেন্ড
**Question:** "Calculate the percentage contribution of each payment channel to the total revenue generated in June 2026."
*   **Target Logic:** `SUM(amount)` কে টোটাল অ্যামাউন্ট দিয়ে ভাগ করতে হবে। এখানে উইন্ডো ফাংশন `SUM(amount) OVER()` ব্যবহার করা যেতে পারে অথবা একটি সাব-কুয়েরি দিয়ে টোটাল ক্যালকুলেট করতে হবে।

### Scenario 13: ইউজার এনগেজমেন্ট লেভেল (Segmentation)
**Question:** "Categorize users into 'Bronze', 'Silver', and 'Gold' based on their total successful application count (Gold: >10, Silver: 5-10, Bronze: <5)."
*   **Target Logic:** `appointments` এবং `users` জয়েন করে `COUNT` করতে হবে এবং `CASE WHEN` স্টেটমেন্ট ব্যবহার করে ক্যাটাগরি তৈরি করতে হবে।

### Scenario 14: কনজিকিউটিভ হলিডে বুকিং এটেম্পট
**Question:** "Identify users who tried to book appointments on more than 3 different public holidays within the same year."
*   **Target Logic:** `appointments` এবং `holidays` টেবিল জয়েন করতে হবে। ইউজারের আইডি এবং হলিডে ডেট কম্পেয়ার করে ফিল্টার এবং এগ্রিগেশন করতে হবে।

### Scenario 15: অপারেটিং আওয়ারের বাইরের পেমেন্ট
**Question:** "List all successful payments that were made outside the operating hours of their respective IVAC centers (refer to business rules for timings)."
*   **Target Logic:** `payment_transactions`, `appointments`, এবং `ivac_centers` জয়েন। `txn_time` এর সাথে বিজনেস রুল ১৫-এ দেওয়া সেন্টারের অপারেটিং আওয়ার কম্পেয়ার করতে হবে। এটি AI-এর জন্য বেশ চ্যালেঞ্জিং কারণ তাকে টেক্সট রুল থেকে টাইম লজিক বের করতে হবে।

### Scenario 16: ডেইলি অ্যাপয়েন্টমেন্ট গ্রোথ রেট (Window Function)
**Question:** "Show the day-over-day growth rate in the number of confirmed appointments for the last 14 days."
*   **Target Logic:** উইন্ডো ফাংশন `LAG()` ব্যবহার করে আগের দিনের অ্যাপয়েন্টমেন্ট সংখ্যা বের করতে হবে এবং বর্তমান দিনের সাথে তুলনা করে গ্রোথ পার্সেন্টেজ বের করতে হবে।

---

### AI-কে যেভাবে প্রশ্ন করবেন:
১. সরাসরি উপরের প্রশ্নগুলো কপি করে চ্যাটবক্সে দিন।
২. দেখুন AI সঠিক টেবিলগুলো (যেমন: `appointments`, `payment_transactions`) জয়েন করছে কিনা।
৩. পেমেন্ট বা ইউজার স্ট্যাটাসের ক্ষেত্রে AI বিজনেস রুলস (is_deleted/status) মেনে চলছে কিনা তা চেক করুন।

**Note:** আপনার ডাটাবেস কানেকশন সচল হলে আপনি এই কুয়েরিগুলো সরাসরি রান করেও রেজাল্ট দেখতে পারবেন।

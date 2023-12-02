-- insert admin (username a, password aa)
INSERT INTO IWUser (id, enabled, name, roles, username, password)
VALUES (1, true, 'admin', 'ADMIN,USER', 'a',
    '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W'),
    (2, true, 'bonito', 'USER', 'b',
    '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W'),
    (3, true, 'Nicoooooo', 'ADMIN,USER', 'Nico',
    '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W'),
    (4, true, 'Tester', 'USER', 'Tester',
    '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W');

-- Generate 10 random users
INSERT INTO IWUser (id, enabled, name, roles, username, password)
SELECT t.id + 4, true, CONCAT('User ', t.id + 3), 'USER', CONCAT('user', t.id + 3), '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W'
FROM (
  SELECT 1 AS id UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
) AS t;

-- Generate 3 random groups
INSERT INTO IWGroup (ID, enabled, CURRENCY, DESC, NAME, NUM_MEMBERS, TOT_BUDGET)
SELECT t.ID, true, FLOOR(RAND() * 2), CONCAT('Group ', t.ID, ' description'), CONCAT('Group ', t.ID, ' name'), 0, 0
FROM (
  SELECT 1 AS ID UNION SELECT 2 UNION SELECT 3
) AS t;

-- insert B into group 2 for Test
INSERT INTO IWMember (GROUP_ID, USER_ID, enabled, BUDGET, ROLE, balance) VALUES (2, 2, true,100, 1, 0);

-- Generate random group memberships for each user
INSERT INTO IWMember (GROUP_ID, USER_ID, enabled, BUDGET, ROLE, balance)
SELECT
    FLOOR(RAND() * 3) + 1, -- choose a random group ID between 1 and 3
    u.id,
    true,
    FLOOR(RAND() * 1000) + 1, -- choose a random budget between 1 and 1000
    FLOOR(RAND() * 1), -- set role to 0 or 1
    0
FROM
    IWUser u WHERE u.id <> 2 AND u.id <> 4;
-- Generate random group memberships for each empty group
/*
INSERT INTO IWMember (GROUP_ID, USER_ID, BUDGET, ROLE)
SELECT
    g.ID,
    FLOOR(RAND() * 13) + 1, -- choose a random user ID between 1 and 13
    FLOOR(RAND() * g.TOT_BUDGET) + 1, -- choose a random budget between 1 and the group's total budget
    0 -- set role to 0
FROM
    IWGroup g
WHERE
    g.ID NOT IN (
        SELECT GROUP_ID FROM IWMember
    );
*/

-- Expenses Type
INSERT INTO IWTYPE (ID, NAME)
VALUES 
  (1, 'Accommodation'),
  (2, 'Entertainment'),
  (3, 'Groceries'),
  (4, 'Bills'),
  (5, 'Transport'),
  (6, 'Restaurants&Bars'),
  (7, 'Others'),
  (8, 'Reimbursement');

-- Generate fixed expense for test
INSERT INTO IWExpense (ID, enabled, AMOUNT, DATE, DESC, NAME, PAID_BY_ID, TYPE_ID) VALUES (99, true, 99, '2023-03-04', 'Expense desc', 'Expense name', 2, 1);

-- Generate 10 random expenses
INSERT INTO IWExpense (ID, enabled, AMOUNT, DATE, DESC, NAME, PAID_BY_ID, TYPE_ID)
SELECT t.ID, true, FLOOR(RAND() * 100), DATEADD('DAY', -FLOOR(RAND() * 30), '2023-03-04'), CONCAT('Expense ', t.ID, ' description'), CONCAT('Expense ', t.ID, ' name'),
  (SELECT id FROM IWUser ORDER BY RAND() LIMIT 1), FLOOR(RAND() * 5) + 1
FROM (
  SELECT 1 AS ID UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
) AS t;

-- Generate fixed
INSERT INTO IWParticipates (USER_ID, EXPENSE_ID, GROUP_ID) VALUES (2, 99, 2);

-- Generate random participates relations
INSERT INTO IWParticipates (USER_ID, EXPENSE_ID, GROUP_ID)
SELECT USER_ID, ID, GROUP_ID
FROM IWExpense e
INNER JOIN IWMember m ON e.PAID_BY_ID = m.USER_ID WHERE e.id <> 99 AND m.GROUP_ID <> 2;

-- Update the value of numMembers for each group in the IWGroup table
UPDATE IWGROUP SET NUM_MEMBERS = (SELECT COUNT(*) FROM IWMEMBER WHERE GROUP_ID=1) WHERE ID = 1;
UPDATE IWGROUP SET NUM_MEMBERS = (SELECT COUNT(*) FROM IWMEMBER WHERE GROUP_ID=2) WHERE ID = 2;
UPDATE IWGROUP SET NUM_MEMBERS = (SELECT COUNT(*) FROM IWMEMBER WHERE GROUP_ID=3) WHERE ID = 3;
-- Update the value of totBudget for each group in the IWGroup table
UPDATE IWGROUP SET TOT_BUDGET = (SELECT SUM(BUDGET) FROM IWMEMBER WHERE GROUP_ID=1) WHERE ID = 1;
UPDATE IWGROUP SET TOT_BUDGET = (SELECT SUM(BUDGET) FROM IWMEMBER WHERE GROUP_ID=2) WHERE ID = 2;
UPDATE IWGROUP SET TOT_BUDGET = (SELECT SUM(BUDGET) FROM IWMEMBER WHERE GROUP_ID=3) WHERE ID = 3;

-- Generate notifications fixed
INSERT INTO IWNotification (ID, DATE_READ, DATE_SENT, MESSAGE, TYPE, GROUP_ID, SENDER_ID  )
VALUES (1, null, DATEADD('DAY', -FLOOR(RAND() * 30), '2023-03-04'), 'Notif 1', 0, 2, 1),
        (2, null, DATEADD('DAY', -FLOOR(RAND() * 30), '2023-03-04'), 'Notif 2', 1, 2, 1);

-- start id numbering from a value that is larger than any assigned above
ALTER SEQUENCE "PUBLIC"."GEN" RESTART WITH 1024;
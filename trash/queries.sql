-- :name set-tsdata! :<!
-- :doc set tsdata
INSERT INTO TSDATA
(name, freq)
VALUES (:name, :xsfreq) 
returning name

-- :name set-tsdata-child! :<!
-- :doc set tsdata-child
INSERT INTO TSDATA_CHILDREN
(parent-name, name)
VALUES (:parent_name, :type)
returning name

-- :name get-tsdata-children :? :n
-- :doc get tsdata children
SELECT name
FROM TSDATA_CHILDREN
WHERE parent_name = :parent_name

-- :name get-tsdata :? :1
-- :doc get tsdata
SELECT *
FROM TSDATA
WHERE name = :name

-- :name update-login! :!
-- :doc update login_time
UPDATE USERS 
SET last_login = :last_login
WHERE name = :name
returning name 

-- :name set-admin! :! :1
-- :doc named user has role 
UPDATE USERS 
SET admin = true 
WHERE name = :name

-- :name remove-admin! :<! :1
-- :doc remove right of administrator
UPDATE USERS
SET admin = false
WHERE name = :name

-- :name set-allowed-data! :<! :1
-- :doc set allowed data for user
INSERT INTO ALLOWED_DATA
(user_name, data_name, data_child_name)
VALUES (:user_name, :data_name, :data_child_name)

-- :name get-tsdata :? :n
-- :doc get allowed tsdata's name
SELECT data_name
FROM ALLOWED_DATA
WHERE (user_name = :user_name)

-- :name get-tsdata-children :? :n
-- :doc get allowed tsdata-children'd names
SELECT data_child_name
FROM ALLOWED_DATA
WHERE (user_name = :user_name)
AND (data_name = :data_name)


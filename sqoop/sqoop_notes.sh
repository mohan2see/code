# NOTES : SQOOP
# Sqoop is a tool designed to transfer data between Hadoop and relational databases or mainframes.
# You can use Sqoop to import data from a relational database management system (RDBMS) such as MySQL or
# Oracle or a mainframe into the Hadoop Distributed File System (HDFS), transform the data in Hadoop MapReduce, and then export the data back into an RDBMS
# Sqoop uses MapReduce to import and export the data, which provides parallel operation as well as fault tolerance

# below tutorial has been worked on this dataset in mysql.

CREATE DATABASE hadoop;
USE hadoop;
CREATE TABLE `emp` (
  `id` int(11) DEFAULT NULL,
  `name` text,
  `dept` text
);
insert into hadoop.emp values (1,"mohan","IT")
insert into hadoop.emp values (2,"manoj","IT")
insert into hadoop.emp values (3,"mohanraj","Farming")
insert into hadoop.emp values (4,NULL,NULL)


# ----------------------------------------------------------------
# sqoop import - a tool to connect to a DB and import data to HDFS
# ----------------------------------------------------------------

# Each row from a table is represented as a separate record in HDFS
# Records can be stored as text files (one record per line), or in binary representation as Avro or SequenceFiles.
sqoop import --connect jdbc:mysql://localhost:3306/ \
--query "select * from mysql.proc where \$CONDITIONS" --username hive --password max \
--num-mappers 1 --target-dir /user/hive/warehouse/import_test

# secure way of passing the password is through --password-file. The file containing the password can either be on the Local FS or HDFS
# the password file should not contain any white space characters. use echo -n "password" > file
sqoop import --connect jdbc:mysql://localhost:3306/ \
--query "select * from mysql.proc where \$CONDITIONS" --username hive  \
--password-file file:///home/max/roadToSuccess/sqoop/password_file.password \
--num-mappers 1 --target-dir /user/hive/warehouse/import_test

# another way of securing password using --password-alias. Hadoop 2.6 above provides credential API to store password with alias in a keystore that's password proteced
# creating the alias
hadoop credential create mysql_password -value max -provider jceks:///user/hive/warehouse/sqoop/

# listing the alias
hadoop credential list -provider jceks:///user/hive/warehouse/sqoop

sqoop import -Dhadoop.security.credential.provider.path=jceks:///user/hive/warehouse/sqoop/password.jceks \
--connect jdbc:mysql://localhost:3306/ --query "select * from mysql.proc where \$CONDITIONS" \
--username hive  --password-alias mysql_password --num-mappers 1 \
--target-dir /user/hive/warehouse/import_test

# sqoop automatically support MySQL, and doesn't require jdbc driver to be installed. for other DB's like MS-SQLServer, install the jdbc driver.
# first, download the appropriate JDBC driver for the type of database you want to import,
# and install the .jar file in the $SQOOP_HOME/lib directory on your client machine. use --driver like below to connect using driver

sqoop import --driver com.microsoft.jdbc.sqlserver.SQLServerDriver \


# MORE SQOOP OPTIONS.
# 1) --append = to append data to existing dataset in HDFS( help prevention of o/p directory already exists error)
# 2) --as-avrodatafile, --as-sequencefile, --as-textfile, --as-parquetfile = to import data in corresponding format
# 3) --num-mappers <mappers> = Use n map tasks to import in parallel
# 4) --split-by <column-name>	= Column of the table used to split work units. if --num-mappers is > 1, use this option to split records. careful when selecting the column, because if certain...
#    ...values are more in the column (for ex, department column where 1 record for `HR` exists, and 10M record for `IT`), the mappers get uneven distribution of records. chances for timeout / failure.
# 5) --compress = to enable compression
# 6) --null-string = string to be written for a null value on string column
# 7) --null-non-string = string to be written for a null value on non-string column
# 8) delete-target-dir = delete the directory in hdfs if exists

# -Dorg prop is required when using --split-by. This will create 2 files (because of --num-mappers 2) which is split by values of ROLE_NAME. if num of mappers > distinct(values in split_by_column)...
# ...the left-over mappers doesn't do any job, yet creates a file with 0 bytes.
sqoop import -Dorg.apache.sqoop.splitter.allow_text_splitter=true \
--connect jdbc:mysql://localhost:3306 --username hive --password max \
--query "select * from hadoop.emp where \$CONDITIONS" --num-mappers 2 \
--split-by emp.id --as-textfile \
--target-dir /user/hive/warehouse/import_test --append \
--null-string "NULL - N/A" --null-non-string "0"


# Controlling type mapping.Sqoop is preconfigured to map most SQL types to appropriate Java or Hive representatives. However the default mapping might not be suitable ...
# ...for everyone and might be overridden by --map-column-java (for changing mapping to Java) or --map-column-hive (for changing Hive mapping)
sqoop import ... --map-column-java id=String,value=Integer

# Incremental Imports = to import only new rows
# --check-column = Specifies the column to be examined when determining which rows to import
# --incremental <mode> = <mode> can be only `append` or `lastmodified`
# --last-value = 	Specifies the maximum value of the check column from the previous import
# You should specify append mode when importing a table where new rows are continually being added with increasing row id values...
# ...You specify the column containing the row’s id with --check-column. Sqoop imports rows where the check column has a value greater than the one specified with --last-value.
# An alternate table update strategy supported by Sqoop is called lastmodified mode. You should use this when rows of the source table may be updated, and each such update...
# ...will set the value of a last-modified column to the current timestamp. Rows where the check column holds a timestamp more recent than the timestamp specified with --last-value are imported.
# At the end of an incremental import, the value which should be specified as --last-value for a subsequent import is printed to the screen. When running a subsequent import, ...
# ...you should specify --last-value in this way to ensure you import only the new or updated data. This is handled automatically by creating an incremental import as a saved job, which is the preferred
# ... mechanism for performing a recurring incremental import. See the section on saved jobs later in this document for more information.

# data in source for incremental import.user/hive/warehouse/import_test
select * from hadoop.emp;
+------+----------+---------+
| id   | name     | dept    |
+------+----------+---------+
|    1 | mohan    | IT      |
|    2 | manoj    | IT      |
|    3 | mohanraj | Farming |
|    4 | NULL     | NULL    |
+------+----------+---------+

# running initial import. this imports 4 records.
sqoop import --connect jdbc:mysql://localhost:3306/ --query "select * from hadoop.emp where \$CONDITIONS" --check-column id \
--incremental append --last-value 0 --target-dir /user/hive/warehouse/import_test --username hive --password max --num-mappers 1


# incremental strategy using `append`. insert a new record (id = 5) and import only that record. use 4 as last value so it imports from 5.
sqoop import --connect jdbc:mysql://localhost:3306/ --query "select * from hadoop.emp where \$CONDITIONS" --check-column id \
--incremental append --last-value 4 --target-dir /user/hive/warehouse/import_test --username hive --password max --append --num-mappers 1

# OUTPUT. similary can perform last modified date strategy and Rows where the check column holds a timestamp more recent than the timestamp specified with --last-value are imported.
hadoop fs -ls /user/hive/warehouse/import_test
Found 2 items
-rw-r--r--   1 max supergroup         53 2020-03-22 21:46 /user/hive/warehouse/import_test/part-m-00000
-rw-r--r--   1 max supergroup         12 2020-03-22 21:48 /user/hive/warehouse/import_test/part-m-00001
max@max:~$ hadoop fs -cat /user/hive/warehouse/import_test/part-m-00000
1,mohan,IT
2,manoj,IT
3,mohanraj,Farming
4,null,null
max@max:~$ hadoop fs -cat /user/hive/warehouse/import_test/part-m-00001
5,chella,IT


# formatting the rows written to HDFS.
# 1) --enclosed-by = specifies the enclosing character for a field
# 2) --escaped-by = specifies the escape character (if enclosed by character is found in the data, the escaped by character will preceed the enclosed by character)
# 3) --fields-terminated-by = sets the character for field(delimiter)
# 4) --lines-terminated-by = sets the end-of-line character
# 5) --optionally-enclosed-by = encloses the fields only for the fields that contains the delimiter characters.

sqoop import --connect jdbc:mysql://localhost:3306/ --username hive --password max \
--query "select * from hadoop.emp where \$CONDITIONS " --fields-terminated-by '\t' --enclosed-by '\"' \
--escaped-by "\\" --lines-terminated-by '\n' --target-dir /user/hive/warehouse/import_test \
--delete-target-dir --num-mappers 1

# OUTPUT
"1"	"mohan"	"IT"

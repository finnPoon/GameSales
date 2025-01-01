My notes
--------
To boost the efficiency of the GET API call, we can implement Redis cache to cache the data instead of calling the database multiple time. But for this assignment, i
take it that the response time requirement are meant for the initial call only. Therefore, i did not implement the Cache.

There are many ways to get pre-aggregated data. I wanted to use Materialised view to generate these pre-aggregated data in task 4. But MySQL does not support materialised view. Hence i decided to use a pre-aggregated
table to store these data with backend computation instead of computing on the fly. For this assignment, i create a daily scheduled event in MySQL to populate the pre-aggregated table.



Prerequisites
--------------
Before you begin, ensure you have met the following requirements:

Java Development Kit (JDK): Version 17
Build Tool: Maven
Git
MySQL server


Preparation
-------------
1) Clone the Repository Open your terminal or command prompt and run:
git clone https://github.com/finnPoon/GameSales.git

2) Navigate to the Project Directory. Example:
cd your-repository

3) Create your own database schema in MySQL
4) Run the scripts in the seeder.sql file under database directory to create the necessary tables and triggers



Configuration
---------------
Before building and running the application, you might need to configure certain settings:

1) Locate the application.properties file in the src/main/resources directory. Update the following configurations:
spring.datasource.url=<your-db-url>?createDatabaseIfNotExist=true&allowLoadLocalInfile=true
spring.datasource.username=<your-db-username>
spring.datasource.password=<your-db-password>
# Example dbURL
jdbc:mysql://localhost:3306/vanguard?createDatabaseIfNotExist=true&allowLoadLocalInfile=true

2) Locate the Constants.java file in the src/main/java/com.vanguard.vanguardapi/config. Update the IMPORT_FILE_PATH variable to the path where your Import CSV file will be stored at. The file i use to test is in
the testFile directory called game_sales_data.csv
(Alternatively can remove this variable and create a temp file and trasnfer the content of the MultiPart file over to get the absolute path. But this might deteriorate the performance.)

3) Run the script in MySQL to expand the max allowed packet size to 100MB:
SET GLOBAL max_allowed_packet = 104857600;


Building and Running the Application
---------------------------------------
1) Navigate to the Project Directory Ensure you are in the root directory of the project.
2) Build the project
3) Run the project
4) Now you can call the endpoint


Extras
--------
1) You can use the /generateCSV endpoint to generate the CSV file with 1million records for testing if you do not have the test file
How-To: Crawler

1- Create a folder called HTMLs under drive D (D:\HTMLs)

2- Create a text file under drive D called links (D:\links.txt) and type in at least one URL. (Each URL is written in a new line)

3- Create a text file under drive D called state (D:\state.txt) and type in 0.

4- All jar files in the Dependancies folder must be added to the project's build path


******

How-To: Indexer

This 'how-to' assume the user is going to use XAMPP for the webserver.

(1) Start XAMPP
	- Start Apache on ports 80, 443 (default)
	- Start MySQL on port 3306 (default)

(2) Create a database title 'search'

(3) Copy the SQL code in file 'search.sql', and then run the SQL code within database 'search' (each at a time; all separated by asterixes)

(4) Create a new project in an IDE, example Eclipse, and title it 'search'

(5) Add file 'Indexer\IndexerV1.java' to the project

(6) Add all dependencies in 'Indexer\Dependencies\' to the project, and add it to the build path

(7) Open up 'IndexerV1.java' within the project, and change the value of:
	- 'state_txt' to the directory of 'Indexer\Crawled\state.txt'
	- 'links_txt' to the directory of 'Indexer\Crawled\links.txt'
	- 'htmls_folder' to the directory of 'Indexer\Crawled\HTMLs\'
	- 'url' to the port number of MySQL (if port is not 3306)
	- 'user' to the root user name of MySQL
	- 'password' to the password set for root at MySQL

NOTE: 
state.txt, links.txt and HTMLs folder are all related to the web crawler, so make sure the directories set
within the indexer and within the web crawler are the same

(8) Run the indexer, and see the results in the database
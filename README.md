# Travel Crawl: A Reddit Based Search Engine
This is a travel related search engine which searches through indexes created using Lucene and Hadoop Mapreduce. The datasets are all Reddit datasets collected from official reddit API. The reddit posts are related to everything about travel.
Deployment Instructions:
1. In “credentials.py” file write the authentication credentials required (client ID, client secret, app name, reddit username, reddit password)
2. In a ‘.txt’ file write the names of subreddits you want to crawl(Travel, solotravel etc)  in new line.
3. Run the crawler using the following command:
 "./crawler.sh <input-file.txt> <numberOfPosts>"
For example:
./crawler.sh subreddits.txt 999
4. MongoDB: Install MongoDB on your local machine and create a database named “IR” and two collections named “Index” and “Dataset”.
5. Importing Data to Mongo: Go to “/mr_index/MongoDBDirectory/” and run “myscript.sh”. This will import our index data into mongo. In similar way import original dataset to “Dataset” collection.
6. Go to “/mr_index/irs.py” and change Line # 2 and Line# 3. Give the path to python site packages of your local PC.
7. Go to “/lucene_index/” and edit the “indexer.sh” file. Uncomment the “mvn” commands.
8. Go to root directory of code folder. Open your terminal and start “apache” and “php” server. If you are using Linux type:
	“sudo apachectl start”
	“sudo php -S localhost:8080”
If you are using Mac, then just run “php -S localhost:8080”. Server will start at “https://localhost:8080”

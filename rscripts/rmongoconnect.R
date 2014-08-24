install.packages("rmongodb")
library(rmongodb)

your connection details will likely differ from these defaults
host <- "dharma.compose.io:10200"
username <- "myuser"
password <- "mypassword"
db <- "testdatabase"

#connect to mongo
#the create function has the following signature
#mongo.create(host="65.254.105.151",name="", username="", password="", db="admin", timeout=0L)

mongo <- mongo.create(host="65.254.105.151:27001" , db="dataset1")

# lets create a string that points to our namespace
# the database we're working with is "testdatabase", and the collection is "example"

collection <- "example"
namespace <- paste(db, collection, sep=".") "testdabase.example"

#get a list of collections within our namespace
mongo.get.database.collections(mongo, db)

# find the number of documents in 'exampleCollection' collection in the the 'testdatabase' database
mongo.count(mongo, namespace, mongo.bson.empty())

#lets create a document to insert
b <- mongo.bson.from.list(list(platform="Compose", language="R", number=1))

#insert the document into our namespace
ok <- mongo.insert(mongo, namespace, b)

# lets insert a few documents
for (i in 2:50 ) {
        b <- mongo.bson.from.list(list(platform="Compose", language="R", number=i))
        mongo.insert(mongo, namespace, b)
}

# build a query to find all "language: R"
buf <- mongo.bson.buffer.create()
mongo.bson.buffer.append(buf, "language", "R")
query <- mongo.bson.from.buffer(buf)

# get the count
count <- mongo.count(mongo, namespace, query)


#and bring them back into a list
numbers <- list()
cursor <- mongo.find(mongo, namespace, query)
while (mongo.cursor.next(cursor)) {
        val <- mongo.cursor.value(cursor)
        numbers[[length(numbers)+1]] <- mongo.bson.value(val, "number")
}


mongo <- mongo.disconnect(mongo)
mongo <- mongo.destroy(mongo)
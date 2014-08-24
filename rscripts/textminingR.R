# Text analysis in R

#init  
install.packages("tm")
library(tm)
options(header=FALSE, stringsAsFactors=FALSE,fileEncoding = "latin1")
setwd("/Users/karenyang/Desktop/hack")

# Read data
data <- system.file("texts", "txt", package = "tm") # for a bunch of files
corpus <- Corpus(VectorSource(data))
# VCorpus provides an implementation with volatile storage semantics.

# clean-up
cleanset <- tm_map(corpus,removeWords,stopwords("english"))
cleanset <- tm_map(cleanset, stripWhitespace)


# Build the document term matrix
dtm <- DocumentTermMatrix(cleanset)
inspect(dtm)
nTerms(dtm)
findAssocs(tdm, c("oil", "opec", "xyz"), c(0.7, 0.75, 0.1))
#findFreqTerms(x, lowfreq = 0, highfreq = Inf)
# TF-IDF
dtm_tfxIdf <- weightTfIdf(dtm)

# clustering
m <- as.matrix(dtm_tfxIdf)
rownames(m) <- 1:nrow(m)

norm_eucl <- function(m) 
        m/apply(m, 1, function(x) sum(x^2)^.5)

m_norm <- norm_eucl(m)

results <- kmeans(m_norm, 12, 30)  # 12 clusters and 30 iterations

clusters <- 1:12

for(i in clusters) {
        cat("Cluster ", i,":",findFreqTerms(dtm_tfxIdf[results$cluster--i], 2,"\n\n"))       
}



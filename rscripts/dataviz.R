# wordcloud idea

# Document term matrix.
dtm <- DocumentTermMatrix(docs)
inspect(dtm[1:5, 1000:1005])
# Explore the corpus.
findFreqTerms(dtm, lowfreq=100)
findAssocs(dtm, "data", corlimit=0.6)
freq <- sort(colSums(as.matrix(dtm)), decreasing=TRUE)
wf   <- data.frame(word=names(freq), freq=freq)
# We can then plot the frequency of those words that occur at least 500 times in the corpus:
library(ggplot2)
p <- ggplot(subset(wf, freq>500), aes(word, freq))
p <- p + geom_bar(stat="identity")
p <- p + theme(axis.text.x=element_text(angle=45, hjust=1))
# Generate a word cloud
library(wordcloud)
wordcloud(names(freq), freq, min.freq=100, colors=brewer.pal(6, "Dark2"))

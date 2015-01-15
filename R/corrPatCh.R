PatCnt <- read.table("corPatCh.txt", sep=",", 
           col.names=c("PatCount","TimeSpanDays"))
attach(PatCnt)
plot(1/TimeSpanDays, PatCount/10, pch=20, xlim=c(0, 0.2), 
     xlab="Change frequency (per day)", ylab="Percentage of top-N count",
     main="Behavior Diversity vs. Change frequency")
grid()
detach(PatCnt)
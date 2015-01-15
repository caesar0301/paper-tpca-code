library(Hmisc)

statDur <- read.table("./statDur.txt", sep=',')
statTrajCount <- read.table("./statTrajCount.txt", sep=',')
statTrajDur <- read.table("./statTrajDur.txt", sep=',')

op <- par()

pdf("statDur.pdf", height=6, width=8)
par(mar=c(5,5,1,1), mgp=c(2.7,0.7,0), cex.axis=1.5, cex.lab=2)
dur <- (statDur$V3-statDur$V2)/3600/24 # hour
Ecdf(sample(dur,3000), xaxt='n', yaxt='n', xlim=c(40,85), lwd=2, subtitles=FALSE,
     xlab="Movement history duration (days)", 
     ylab="Cumulative distribution of users", main="")
axis(1, tcl=0.5)
axis(2, tcl=0.5)
grid()
dev.off()

pdf("statTrajCount.pdf", height=6, width=8)
par(mar=c(5,5,1,1), mgp=c(2.7,0.7,0), cex.axis=1.5, cex.lab=2)
Ecdf(sample(statTrajCount$V2, 3000), xaxt='n', yaxt='n', lwd=2, xlim=c(100, 400),
     subtitles=FALSE,
     xlab="Trajectory count of each user", 
     ylab="Cumulative distribution of users", main="")
axis(1, tcl=0.5)
axis(2, tcl=0.5)
grid()
dev.off()

pdf("statTrajDur.pdf", height=6, width=8)
par(mar=c(5,5,1,1), mgp=c(2.7,0.7,0), cex.axis=1.5, cex.lab=2)
Ecdf(sample(statTrajDur$V2, 3000)/60, xaxt='n', yaxt='n', lwd=2, xlim=c(0, 60*6),
     subtitles=FALSE,
     xlab="Trajectory duration (minutes)", 
     ylab="Cumulative distribution of trajectories", main="")
axis(1, tcl=0.5)
axis(2, tcl=0.5)
grid()
dev.off()
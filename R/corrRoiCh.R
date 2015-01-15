Roi <- read.table("corRoiCh.txt", sep=",", 
                     col.names=c("TimeSpanDays", "Entropy"))
attach(Roi)
selector <- TimeSpanDays>0
plot(Entropy[selector], TimeSpanDays[selector], 
           pch=20, xlim=c(0, 2.0), ylim=c(0,100),
           xlab="Entropy", ylab="Change frequency",
           main="")
grid()
detach(Roi)
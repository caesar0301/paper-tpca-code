pros <- seq(0.4, 0.9, by=0.1)
rlist <- list()
for ( prob in pros ){
  cfs <- c()
  for ( i in seq(length(dataList)) ){
    # get one user's data
    oneu <- dataList[[i]]
    # cal median time of pattern set
    pstime <- (oneu$stime + oneu$etime)/2
    if ( length(pstime) < 3 )
      next
    # get change point timestamps
    cpts <- pstime[oneu$pp>=prob]
    cpts <- cpts[1:length(cpts)-1] # remove last NA
    if (length(cpts) == 0)
      cf <- 365
    else{
      cpts.min <- range(cpts, na.rm=TRUE)[1]
      cpts.max <- range(cpts, na.rm=TRUE)[2]
      # cal change frequency
      cf <- (cpts.max-cpts.min)/3600/24/ length(cpts) # days/change
    }
    cfs[length(cfs)+1] <- cf
  }
  rlist[[length(rlist)+1]] <- cfs
}

library(Hmisc)
pdf("statChangeCDF.pdf", height=6, width=8)
par(mar=c(5,5,1,1), mgp=c(2.7,0.7,0), cex.axis=1.5, cex.lab=2)
ltypes <- seq(1, length(pros))
for( typ in ltypes ){
  if ( typ == 1 ){
    Ecdf(rlist[[typ]], xaxt='n', yaxt='n', 
         xlab="Time interval (days) between two changes", 
         ylab="Cumulative distribution of users",
         subtitles=FALSE,
         xlim=c(1, 40), ylim=c(0.1,0.7), lwd=2, lty=typ)
    axis(1, tcl=0.5)
    axis(2, tcl=0.5)
    grid()
  } else{
    Ecdf(rlist[[typ]], lwd=2, lty=typ, add=TRUE, subtitles=FALSE)
  }
}
exps <- paste("PP=", pros)
legend("topleft", exps, lty=ltypes)
dev.off()

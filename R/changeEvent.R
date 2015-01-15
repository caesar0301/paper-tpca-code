library(xts)
dt <- seq(as.POSIXct("2013-04-25"), as.POSIXct("2013-07-15"), by=3600*24*3)
dt.seq <- xts(rep(NA, length(dt)), order.by=dt)

res <- xts(rep(0, length(dt.seq)), order.by=dt)
for ( i in seq(length(dataList)) ){
  user <- na.omit(dataList[[i]])
  user["atime"] <- (user$stime+user$etime)/2
  user['cf'] <- user$pp>0.8
  atime <- as.POSIXct(user$atime, origin="1970-01-01")
  obs <- cumsum(na.omit(xts(as.numeric(user$cf), atime, na.a)))
  mobs <- na.locf(merge(dt.seq, obs)[,2])
  mobs[is.na(mobs)] <- 0
  r <- diff(merge(dt.seq, mobs, join="inner")[,2])
  r[is.na(r)] <- 0
  res <- res + r
}

fmt <- "%b-%d"
labs <- format(time(res), fmt)
pdf("changeEvent.pdf", width=10, height=6)
par(mar=c(3,5,2,1), mgp=c(2.7,0.7,0), cex.axis=1.5, cex.lab=2)
barplot(res/length(dataList), ylim=c(0,0.06), names.arg=labs, 
        ylab="Proportion of changed users", col="grey78")
dev.off()


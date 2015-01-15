library(cpm)

pdf("changedetection.pdf")
data <- c(11.1460,20.3340,15.8632,12.9731,10.0087,13.0983,9.6927,17.2591,21.4406,35.1026,27.1255,23.0956,18.3701,18.2925,21.3616,24.6519,26.9155,27.2442,26.7576,22.8431,23.6154,25.0763,15.7791,10.3255,19.0832,35.7714,22.6019,12.4564,24.7014,12.0873)
results <- detectChangePointBatch(data, cpmType = "Mann-Whitney", alpha = 0.05)
plot(data, type="l")
if (results$changeDetected) {
  abline(v = results$changePoint, col="red")
}
plot(results$Ds)
abline(h=results$threshold,lty=2)

results2 <- processStream(data, cpmType = "Cramer-von-Mises", ARL0=500, startup=6)
plot(data,type='l')
for (dt in results2$detectionTimes) {
  abline(v=dt, lty=2, col="red")
}

dev.off()
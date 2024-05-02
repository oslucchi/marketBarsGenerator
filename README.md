The application generates bars simultating a market behavior

The package.properties file allows to configure the expected behavior through the following parameters:<br>
<ul>
  <li>
    <b>totalPeriods</b>: the total numerber of periods among which the entire time window will be devided. Each period could be configured singularly
  </li>
  <li>
    <b>volatility</b>: the expected volatility for the period (how we expect the end of the period to set, compared to the first open)
  </li>
  <li>
    <b>duration</b>: how many bars will be generated in the selected period
  </li>
  <li>
    <b>maxIntrabarVol</b>: the max volatility (in absolute value) among 2 bars in the same period 
  </li>
  <li>
    <b>maxVolHighLow</b>: states what is the maximum deviation for the High and Low of the bar. Random modificaitons are applied
  </li>
  <li>
    <b>enbleTrends</b>: configures if random trends will be generated in the course of the period
  </li>
  <li>
    <b>maxTrendsInPeriod</b>: random number of trends that  eventually happen during the period
  </li>
  <li>
    <b>maxBarsInTrend</b>: random number of bars will be in trend for each trend generated during the period
  </li>
  <li>
    <b>initialVolume</b>: the volume of the first trade (the volume has not yet been implemented correctly) 
  </li>
  <li>
    <b>startPrice</b>: the first open price
  </li>
  <li>
    <b>interval</b>: duration of the bar in minutes
  </li>
  <li>
    <b>probabilityToEnterTrend</b>: when the possibility to start a trend is evaluated, do it only if the random number generated is greater than 1 - probabilityToEnterTrend
  </li>
  <li>
    <b>startDate</b>: the timestamp of the first bar 
  </li>
</ul>
<p>
  The algorithm tries to workout bars that will reach the expected close price at the end of the period, implementing the expected volatility for the period.<br>
  Reaching the exact volatility is not granted in order to keep the bars pseudo-random as much as possible.
</p>
<p>
  At each bar, the possibilty to start a trend is evaluated usign the probabilityToEnterTrend as explained above.<br>
  If the trend is enter, the number of the bars for that trend are also randomly calculated starting from the maxBarsInTrend. 40% of those bars are guaranteed. Randomly the system adds up to another 60% reaching at maximum the configured number.
</p>
<p>
  The trend direction is again randomly decided. A shift factor of 30% is used to increase the chances the trend will be in the direction of the period volatility 
</p>

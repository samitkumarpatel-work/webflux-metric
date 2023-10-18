# webflux-metric

**Prometheus metrics**

[reference](https://chronosphere.io/learn/an-introduction-to-the-four-primary-types-of-prometheus-metrics/?cn-reloaded=1)

**Counters**

Counters are a fundamental way to track how often an event occurs within an application or service. 
They are used to track and measure Prometheus metrics with continually – or monotonically – increasing values which get exposed as time series.

**Gauges**

Gauges are used to periodically take measurements or snapshots of a metric at a single point in time. A gauge is similar to a counter, however, their value can arbitrarily increase or decrease over time (e.g. CPU usage and temperature).

Gauges are useful for when you want to query a metric that can go up or down, but don’t need to know the rate of change. Note: the rate() function does not work with gauges as rates can only be applied to metrics that continually increase (i.e. counters).

**Histograms**

Histograms sample observations by their frequency or count, and place the observed values in pre-defined buckets. If you don’t specify buckets, the Prometheus client library will use a set of default buckets (e.g. for the Go client library, it uses .005, .01, .025, .05, .1, .25, .5, 1, 2.5, 5, 10). These buckets are used to track the distribution of an attribute over a number of events (i.e. event latency).

You can override these default buckets if you need more or different values, but note the potential increase in costs and/or cardinality when doing so — each bucket has a corresponding unique time series.

Overall, histograms are known to be highly performant as they only require a count per bucket, and can be accurately aggregated across time series and instances (provided they have the same buckets configured). This means that you can accurately aggregate histograms across multiple instances or regions without having to emit additional time series for aggregate views (unlike computed percentile values with summaries).

**Summaries**

Summaries are similar to histograms in that they track distributions of an attribute over a number of events, but they expose quantile values directly (i.e. on the client side at collection time vs. on the Prometheus monitoring service at query time).

They are most commonly used for monitoring latencies (e.g. P50, P90, P99), and are best for use cases where an accurate latency value or sample is desired without configuration of histogram buckets. 


***When do I use counters?***

When tracking continually increasing counts of events you’d use a counter metric. They are most often queried using the rate() function to view how often an event occurs over a given time period.

***When do I use gauges?***

Use a Gauge metric to represent a single numerical value that can go up or down. Gauges are often used for measuring system metrics like the current memory usage.

***What can histograms show?***

Use a Histogram metric to measure the distribution of values over time. Histograms allow you to collect data on the distribution of response times.

***Why use summaries?***

Use a Summary metric for similar purposes as Histograms. They capture percentiles (e.g., 50th, 90th, 99th) and other statistics. 



[Custom Metrics](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.metrics.registering-custom)
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

To report the current state of a metric that can arbitrarily increase or decrease over time, for example as a metric for CPU utilization.

***What can histograms show?***

These are showing the distribution of observations and putting those observations into pre-defined buckets. They are highly performant, and values can be accurately aggregated across both windows of time and across numerous time series. Note that both quantile and percentile calculations are done on the server side at query time.

***Why use summaries?***

Summaries measure latencies and are best used where an accurate latency value is desired without configuration of histogram buckets. They are limited as they cannot accurately perform aggregations or averages across quantiles and can be costly in terms of required resources. Calculations are done on the application or service client side at metric collection time. 



[Custom Metrics](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.metrics.registering-custom)
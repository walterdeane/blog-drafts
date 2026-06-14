package main

import (
	"fmt"
	"log"
	"net/http"
	"time"

	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"
	"github.com/prometheus/client_golang/prometheus/promhttp"
)

var (
	reqCounter = promauto.NewCounter(prometheus.CounterOpts{
		Name: "demo_http_requests_total",
		Help: "Total number of HTTP requests served by the demo app.",
	})

	reqDuration = promauto.NewHistogram(prometheus.HistogramOpts{
		Name:    "demo_http_request_duration_seconds",
		Help:    "Histogram of request durations for the demo app.",
		Buckets: prometheus.DefBuckets,
	})
)

func withMetrics(h http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		start := time.Now()
		defer func() {
			reqDuration.Observe(time.Since(start).Seconds())
			reqCounter.Inc()
		}()
		h.ServeHTTP(w, r)
	})
}

func rootHandler(w http.ResponseWriter, _ *http.Request) {
	now := time.Now().Format(time.RFC3339)
	_, _ = fmt.Fprintf(w, "demo-app ok: %s\n", now)
}

func main() {
	mux := http.NewServeMux()
	mux.HandleFunc("/", rootHandler)
	mux.Handle("/metrics", promhttp.Handler())

	addr := ":8080"
	log.Printf("starting demo-app on %s", addr)
	if err := http.ListenAndServe(addr, withMetrics(mux)); err != nil {
		log.Fatalf("server error: %v", err)
	}
}
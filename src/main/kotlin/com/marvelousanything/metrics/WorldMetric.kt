package com.marvelousanything.metrics

import io.prometheus.metrics.core.metrics.Metric
import io.prometheus.metrics.model.registry.Collector
import org.bukkit.plugin.java.JavaPlugin

abstract class WorldMetric(val plugin: JavaPlugin, val collector: Collector) : Metric() {
    overide
}
import {Component, OnInit} from '@angular/core';
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {MetricsService} from "../../../services/metrics.service";

@Component({
  selector: 'app-metrics',
  templateUrl: './metrics.component.html',
  styleUrls: ['./metrics.component.scss']
})
export class MetricsComponent implements OnInit {
  metrics: any = {};
  cachesStats: any = {};
  servicesStats: any = {};
  updatingMetrics = true;
  JCACHE_KEY: string;

  constructor(private modalService: NgbModal, private metricsService: MetricsService) {
    this.JCACHE_KEY = 'jcache.statistics';
  }

  ngOnInit() {
    this.refresh();
  }

  refresh() {
    this.updatingMetrics = true;
    this.metricsService.getMetrics().subscribe((metrics) => {
      this.metrics = metrics;
      this.updatingMetrics = false;
      this.servicesStats = {};
      this.cachesStats = {};
      Object.keys(metrics.timers).forEach((key) => {
        const value = metrics.timers[key];
        if (key.includes('web.rest') || key.includes('service')) {
          this.servicesStats[key] = value;
        }
      });
      Object.keys(metrics.gauges).forEach((key) => {
        if (key.includes('jcache.statistics')) {
          const value = metrics.gauges[key].value;
          // remove gets or puts
          const index = key.lastIndexOf('.');
          const newKey = key.substr(0, index);

          // Keep the name of the domain
          this.cachesStats[newKey] = {
            'name': this.JCACHE_KEY.length,
            'value': value
          };
        }
      });
    });
  }

  filterNaN(input) {
    if (isNaN(input)) {
      return 0;
    }
    return input;
  }

}

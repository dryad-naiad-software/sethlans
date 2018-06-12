/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

import {Component, OnInit} from '@angular/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {MetricsService} from '../../../services/metrics.service';

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

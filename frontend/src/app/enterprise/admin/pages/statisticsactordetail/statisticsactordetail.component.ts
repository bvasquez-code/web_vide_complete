import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-statisticsactordetail',
  templateUrl: './statisticsactordetail.component.html'
})
export class StatisticsactordetailComponent implements OnInit {
  actorCod = '';

  constructor(private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.actorCod = this.route.snapshot.paramMap.get('actorCod') || '';
  }
}

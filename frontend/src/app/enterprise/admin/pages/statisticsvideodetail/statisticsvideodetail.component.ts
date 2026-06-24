import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-statisticsvideodetail',
  templateUrl: './statisticsvideodetail.component.html'
})
export class StatisticsvideodetailComponent implements OnInit {
  videoCod = '';

  constructor(private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.videoCod = this.route.snapshot.paramMap.get('videoCod') || '';
  }
}

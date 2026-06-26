import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-publicsearch',
  templateUrl: './publicsearch.component.html'
})
export class PublicsearchComponent implements OnInit, OnDestroy {
  searchQuery = '';
  private querySubscription?: Subscription;

  constructor(private route: ActivatedRoute, private router: Router) {}

  ngOnInit(): void {
    this.querySubscription = this.route.queryParamMap.subscribe(params => {
      this.searchQuery = params.get('q') || '';
    });
  }

  ngOnDestroy(): void {
    this.querySubscription?.unsubscribe();
  }

  search(): void {
    const query = this.searchQuery.trim();
    if (!query) {
      this.clear();
      return;
    }
    this.router.navigate(['/'], { queryParams: { q: query } });
  }

  clear(): void {
    this.searchQuery = '';
    this.router.navigate(['/']);
  }
}

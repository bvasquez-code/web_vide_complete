import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AdminVideoService } from '../../../video/service/AdminVideoService';
import { PaginationDto } from '../../../shared/model/dto/PaginationDto';

@Component({
  selector: 'app-statisticsactors',
  templateUrl: './statisticsactors.component.html'
})
export class StatisticsactorsComponent implements OnInit {
  Sort = 'views';
  rows: any[] = [];
  Page = 1;
  Limit = 20;
  TotalRows = 0;
  pagination = new PaginationDto({ Limit: this.Limit, ItemLabel: 'actores' });
  loading = false;
  errorMessage = '';

  constructor(private adminVideoService: AdminVideoService, private route: ActivatedRoute, private router: Router) {}

  async ngOnInit(): Promise<void> {
    const params = this.route.snapshot.queryParamMap;
    this.Sort = params.get('Sort') || 'views';
    this.Page = Number(params.get('Page') || '1');
    this.Limit = Number(params.get('Limit') || '20');
    await this.findAll(this.Page, false);
  }

  async findAll(page: number = this.Page, syncUrl: boolean = true): Promise<void> {
    this.Page = page < 1 ? 1 : page;
    this.loading = true;
    this.errorMessage = '';
    try {
      const rpt = await this.adminVideoService.findActorStatistics(this.Sort, this.Page, this.Limit);
      if (rpt.ErrorStatus) {
        this.errorMessage = rpt.Message;
        return;
      }
      this.rows = rpt.Data?.Data || [];
      this.TotalRows = rpt.Data?.TotalRows || 0;
      this.Page = rpt.Data?.Page || this.Page;
      this.Limit = rpt.Data?.Limit || this.Limit;
      this.pagination = new PaginationDto({ Page: this.Page, Limit: this.Limit, TotalRows: this.TotalRows, ItemLabel: 'actores' });
      if (syncUrl) await this.syncQueryParams();
    } finally {
      this.loading = false;
    }
  }

  async changeSort(): Promise<void> {
    await this.findAll(1);
  }

  private async syncQueryParams(): Promise<void> {
    await this.router.navigate([], {
      relativeTo: this.route,
      replaceUrl: true,
      queryParams: { Sort: this.Sort, Page: this.Page, Limit: this.Limit }
    });
  }

  formatNumber(value: any): string {
    return Math.round(Number(value) || 0).toLocaleString();
  }

  formatDuration(value: any): string {
    const totalSeconds = Math.max(0, Math.round(Number(value) || 0));
    const seconds = (totalSeconds % 60).toString().padStart(2, '0');
    const minutesNumber = Math.floor(totalSeconds / 60);
    const minutes = (minutesNumber % 60).toString().padStart(2, '0');
    const hours = Math.floor(minutesNumber / 60);
    return hours > 0 ? `${hours}:${minutes}:${seconds}` : `${minutes}:${seconds}`;
  }
}

import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { AdminVideoService } from '../../../video/service/AdminVideoService';

@Component({
  selector: 'app-videostatisticsdetail',
  templateUrl: './videostatisticsdetail.component.html'
})
export class VideostatisticsdetailComponent implements OnChanges {
  @Input() videoCod = '';
  @Input() embedded = false;

  loading = false;
  errorMessage = '';
  summary: any = null;
  dailyViews: any[] = [];

  constructor(private adminVideoService: AdminVideoService) {}

  async ngOnChanges(changes: SimpleChanges): Promise<void> {
    if (changes['videoCod']) {
      await this.load();
    }
  }

  async load(): Promise<void> {
    this.summary = null;
    this.dailyViews = [];
    this.errorMessage = '';
    if (!this.videoCod) return;
    this.loading = true;
    try {
      const rpt = await this.adminVideoService.findVideoStatisticsDetail(this.videoCod);
      if (rpt.ErrorStatus) {
        this.errorMessage = rpt.Message;
        return;
      }
      this.summary = rpt.Data?.Summary || null;
      this.dailyViews = rpt.Data?.DailyViews || [];
    } catch {
      this.errorMessage = 'No se pudieron cargar las estadisticas del video.';
    } finally {
      this.loading = false;
    }
  }

  numberValue(value: any): number {
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : 0;
  }

  formatNumber(value: any): string {
    return Math.round(this.numberValue(value)).toLocaleString();
  }

  formatDecimal(value: any, digits: number = 1): string {
    return this.numberValue(value).toLocaleString(undefined, { minimumFractionDigits: digits, maximumFractionDigits: digits });
  }

  formatDuration(value: any): string {
    const totalSeconds = Math.max(0, Math.round(this.numberValue(value)));
    const seconds = (totalSeconds % 60).toString().padStart(2, '0');
    const minutesNumber = Math.floor(totalSeconds / 60);
    const minutes = (minutesNumber % 60).toString().padStart(2, '0');
    const hours = Math.floor(minutesNumber / 60);
    return hours > 0 ? `${hours}:${minutes}:${seconds}` : `${minutes}:${seconds}`;
  }
}

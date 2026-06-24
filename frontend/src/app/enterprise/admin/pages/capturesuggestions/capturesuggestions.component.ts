import { Component, OnInit } from '@angular/core';
import { AdminVideoService } from '../../../video/service/AdminVideoService';

@Component({
  selector: 'app-capturesuggestions',
  templateUrl: './capturesuggestions.component.html'
})
export class CapturesuggestionsComponent implements OnInit {
  suggestions: any[] = [];
  errorMessage = '';
  successMessage = '';

  constructor(private adminVideoService: AdminVideoService) {}

  async ngOnInit(): Promise<void> {
    await this.load();
  }

  async load(): Promise<void> {
    const rpt = await this.adminVideoService.findCaptureSuggestions();
    if (rpt.ErrorStatus) {
      this.errorMessage = rpt.Message;
      return;
    }
    this.suggestions = rpt.Data || [];
  }

  async approve(item: any): Promise<void> {
    await this.review(item, true);
  }

  async reject(item: any): Promise<void> {
    await this.review(item, false);
  }

  private async review(item: any, approve: boolean): Promise<void> {
    this.errorMessage = '';
    this.successMessage = '';
    const rpt = approve
      ? await this.adminVideoService.approveCaptureSuggestion(item.SuggestionId, item.ReviewComment || '')
      : await this.adminVideoService.rejectCaptureSuggestion(item.SuggestionId, item.ReviewComment || '');
    if (rpt.ErrorStatus) {
      this.errorMessage = rpt.Message;
      return;
    }
    this.successMessage = approve ? 'Sugerencia aprobada.' : 'Sugerencia rechazada.';
    await this.load();
  }
}
